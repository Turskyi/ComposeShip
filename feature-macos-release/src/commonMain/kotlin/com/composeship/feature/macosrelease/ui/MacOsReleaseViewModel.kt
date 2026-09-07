package com.composeship.feature.macosrelease.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.model.LogEntry
import com.composeship.core.domain.model.LogType
import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.macosrelease.domain.service.AppStoreConnectService
import com.composeship.feature.macosrelease.domain.service.CredentialService
import com.composeship.feature.macosrelease.domain.service.GradleService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MacOsReleaseViewModel(
    private val processService: ProcessService,
    private val gradleService: GradleService,
    private val fileSystemService: FileSystemService,
    private val credentialService: CredentialService,
    private val appStoreConnectService: AppStoreConnectService
) : ViewModel() {

    private val _state = MutableStateFlow(MacOsReleaseState())
    val state: StateFlow<MacOsReleaseState> = _state.asStateFlow()

    private var releaseJob: Job? = null

    init {
        viewModelScope.launch {
            loadSavedCredentials()
            autoDetectApiKey()
        }
    }

    private suspend fun loadSavedCredentials() {
        val issuerId =
            credentialService.getCredential("appstore_issuer_id") ?: ""
        val keyId = credentialService.getCredential("appstore_key_id") ?: ""
        val keyPath = credentialService.getCredential("appstore_key_path") ?: ""

        _state.update {
            it.copy(
                apiIssuerId = issuerId,
                apiKeyId = keyId,
                apiKeyPath = keyPath
            )
        }
        onCredentialsChanged(issuerId, keyId, keyPath)
    }

    private fun autoDetectApiKey() {
        val home = fileSystemService.getUserHome() ?: return
        val searchRoots = mutableListOf(
            "$home/.appstoreconnect/private_keys",
            "$home/private_keys",
            "$home/Downloads",
            "$home/development"
        )

        val devDir = "$home/development"
        if (fileSystemService.exists(devDir)) {
            try {
                val subDirs = fileSystemService.listFiles(devDir)
                searchRoots.addAll(subDirs)
            } catch (e: Exception) {
                /* ignore */
                println("Error listing files in $devDir: ${e.message}")
            }
        }

        val detectedFiles = mutableSetOf<String>()
        for (dir in searchRoots) {
            try {
                if (fileSystemService.exists(dir)) {
                    val files = fileSystemService.listFiles(dir)
                    detectedFiles.addAll(files.filter { it.endsWith(".p8") })
                }
            } catch (e: Exception) {
                /* ignore */
                println("Error listing files in $dir: ${e.message}")
            }
        }

        if (detectedFiles.isNotEmpty()) {
            val fileList = detectedFiles.toList()
            _state.update { it.copy(detectedApiKeyFiles = fileList) }

            if (fileList.size == 1 && _state.value.apiKeyPath.isEmpty()) {
                val p8File = fileList.first()
                val filename = p8File.substringAfterLast("/")
                val keyIdMatch =
                    Regex("AuthKey_([A-Z0-9]{10})\\.p8").find(filename)
                val detectedKeyId =
                    keyIdMatch?.groupValues?.get(1) ?: _state.value.apiKeyId

                onCredentialsChanged(
                    _state.value.apiIssuerId,
                    detectedKeyId,
                    p8File
                )
            }
        }
    }

    fun onProjectRootChanged(path: String) {
        _state.update {
            it.copy(
                projectRoot = path,
                projectValidationError = null
            )
        }
        validateProject(path)
    }

    fun onBrowseProjectRoot() {
        viewModelScope.launch {
            val path = fileSystemService.pickDirectory()
            if (path != null) {
                onProjectRootChanged(path)
            }
        }
    }

    private fun validateProject(path: String) {
        if (path.isEmpty()) return

        val settingsExists =
            fileSystemService.exists("$path/settings.gradle.kts") ||
                    fileSystemService.exists("$path/settings.gradle")
        val buildExists = fileSystemService.exists("$path/build.gradle.kts") ||
                fileSystemService.exists("$path/build.gradle")

        if (!settingsExists || !buildExists) {
            _state.update {
                it.copy(
                    isProjectValid = false,
                    projectValidationError = "Not a Gradle project (missing settings/build files)"
                )
            }
            return
        }

        val gradlewExists = fileSystemService.exists("$path/gradlew")
        if (!gradlewExists) {
            _state.update {
                it.copy(
                    isProjectValid = false,
                    projectValidationError = "Missing gradlew wrapper"
                )
            }
            return
        }

        _state.update {
            it.copy(
                isProjectValid = true,
                projectValidationError = null
            )
        }
        detectTasks(path)
        detectCategory(path)
    }

    private fun detectCategory(path: String) {
        val possibleSubprojects =
            listOf("", "composeApp", "app/desktopApp", "desktop")

        for (sub in possibleSubprojects) {
            val subPath = if (sub.isEmpty()) path else "$path/$sub"

            // 1. Check build.gradle.kts or build.gradle
            val buildFiles = listOf("$subPath/build.gradle.kts", "$subPath/build.gradle")
            for (buildFilePath in buildFiles) {
                if (fileSystemService.exists(buildFilePath)) {
                    val content = fileSystemService.readFile(buildFilePath) ?: ""
                    
                    // Try direct property: category = "..."
                    val categoryMatch =
                        Regex("""category\s*=\s*["']([^"']+)["']""").find(content)
                    if (categoryMatch != null) {
                        val category = categoryMatch.groupValues[1]
                        _state.update { it.copy(selectedCategory = category) }
                        return
                    }

                    // Try extraKeysRawXml containing LSApplicationCategoryType
                    val extraKeysMatch = Regex(
                        """LSApplicationCategoryType</key>\s*<string>([^<]+)</string>""",
                        RegexOption.IGNORE_CASE
                    ).find(content)
                    if (extraKeysMatch != null) {
                        val category = extraKeysMatch.groupValues[1].trim()
                        _state.update { it.copy(selectedCategory = category) }
                        return
                    }
                }
            }

            // 2. Check for Info.plist in common locations
            val plistLocations = listOf(
                "$subPath/src/desktopMain/resources/Info.plist",
                "$subPath/src/main/resources/Info.plist",
                "$subPath/Info.plist"
            )
            for (plistPath in plistLocations) {
                if (fileSystemService.exists(plistPath)) {
                    val content = fileSystemService.readFile(plistPath) ?: ""
                    val categoryMatch = Regex(
                        """<key>LSApplicationCategoryType</key>\s*<string>([^<]+)</string>""",
                        RegexOption.IGNORE_CASE
                    ).find(content)
                    if (categoryMatch != null) {
                        val category = categoryMatch.groupValues[1].trim()
                        _state.update { it.copy(selectedCategory = category) }
                        return
                    }
                }
            }
        }
    }

    private fun detectTasks(path: String) {
        val possibleSubprojects =
            listOf("", "composeApp", "app/desktopApp", "desktop")
        val foundTasks = mutableListOf<String>()

        for (sub in possibleSubprojects) {
            val prefix = if (sub.isEmpty()) ":" else ":$sub:"
            val buildFilePath =
                if (sub.isEmpty()) "$path/build.gradle.kts" else "$path/$sub/build.gradle.kts"

            if (fileSystemService.exists(buildFilePath)) {
                val content = fileSystemService.readFile(buildFilePath) ?: ""
                val isComposeApplied =
                    (content.contains("org.jetbrains.compose") || content.contains(
                        "compose.desktop"
                    )) &&
                            !content.contains("apply false")

                if (isComposeApplied) {
                    foundTasks.add("${prefix}packageReleasePkg")
                }
            }
        }

        if (foundTasks.isEmpty()) {
            foundTasks.add(":packageReleasePkg")
        }

        val allTasks = mutableListOf<String>()
        foundTasks.forEach { releaseTask ->
            allTasks.add(releaseTask)
            allTasks.add(releaseTask.replace("ReleasePkg", "Pkg"))
        }

        _state.update {
            it.copy(
                availableTasks = allTasks.distinct(),
                selectedTask = allTasks.firstOrNull { t -> t.contains("Release") }
                    ?: allTasks.first()
            )
        }
    }

    fun onTaskSelected(task: String) {
        _state.update { it.copy(selectedTask = task) }
    }

    fun onCategorySelected(category: String) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun nextStep() {
        val next = when (_state.value.step) {
            ReleaseStep.SelectProject -> ReleaseStep.SelectTask
            ReleaseStep.SelectTask -> {
                loadAvailableCategories()
                ReleaseStep.AppCategory
            }

            ReleaseStep.AppCategory -> {
                loadSigningIdentities()
                ReleaseStep.SigningIdentity
            }

            ReleaseStep.SigningIdentity -> ReleaseStep.AppStoreCredentials
            ReleaseStep.AppStoreCredentials -> {
                val icon = findSourceIcon(_state.value.projectRoot)
                if (icon != null) {
                    ReleaseStep.Process
                } else {
                    ReleaseStep.SelectIcon
                }
            }

            ReleaseStep.SelectIcon -> ReleaseStep.Process
            ReleaseStep.Process -> ReleaseStep.Process
        }
        _state.update { it.copy(step = next) }
    }

    fun previousStep() {
        val prev = when (_state.value.step) {
            ReleaseStep.SelectProject -> ReleaseStep.SelectProject
            ReleaseStep.SelectTask -> ReleaseStep.SelectProject
            ReleaseStep.AppCategory -> ReleaseStep.SelectTask
            ReleaseStep.SigningIdentity -> ReleaseStep.AppCategory
            ReleaseStep.AppStoreCredentials -> ReleaseStep.SigningIdentity
            ReleaseStep.SelectIcon -> ReleaseStep.AppStoreCredentials
            ReleaseStep.Process -> {
                val icon = findSourceIcon(_state.value.projectRoot)
                if (icon != null) ReleaseStep.AppStoreCredentials else ReleaseStep.SelectIcon
            }
        }
        _state.update { it.copy(step = prev) }
    }

    private fun loadAvailableCategories() {
        val categories = listOf(
            "public.app-category.business",
            "public.app-category.developer-tools",
            "public.app-category.education",
            "public.app-category.entertainment",
            "public.app-category.finance",
            "public.app-category.games",
            "public.app-category.graphics-design",
            "public.app-category.healthcare-fitness",
            "public.app-category.lifestyle",
            "public.app-category.medical",
            "public.app-category.music",
            "public.app-category.news",
            "public.app-category.photography",
            "public.app-category.productivity",
            "public.app-category.reference",
            "public.app-category.social-networking",
            "public.app-category.sports",
            "public.app-category.travel",
            "public.app-category.utilities",
            "public.app-category.video",
            "public.app-category.weather"
        )

        val currentSelected = _state.value.selectedCategory
        val finalCategories = if (currentSelected.isNotEmpty() && !categories.contains(currentSelected)) {
            (listOf(currentSelected) + categories).distinct()
        } else {
            categories
        }

        _state.update { it.copy(availableCategories = finalCategories) }
    }

    fun startOver() {
        _state.update {
            MacOsReleaseState(
                projectRoot = it.projectRoot,
                isProjectValid = it.isProjectValid
            )
        }
        detectTasks(_state.value.projectRoot)
    }

    fun loadSigningIdentities() {
        _state.update {
            it.copy(
                signingIdentities = emptyList(),
                installerIdentities = emptyList(),
                isLoadingIdentities = true
            )
        }
        viewModelScope.launch {
            processService.execute(listOf("security", "find-identity", "-v"))
                .collect { output ->
                    when (output) {
                        is ProcessOutput.Stdout -> {
                            val identity = parseIdentity(output.line)
                            if (identity != null) {
                                _state.update { s ->
                                    if (identity.contains("Application")) {
                                        val newIdentities =
                                            s.signingIdentities + identity
                                        val currentSelected = s.selectedIdentity
                                        val shouldUpdateSelection =
                                            currentSelected.isEmpty() ||
                                                    (currentSelected.contains("Developer ID") && identity.contains(
                                                        "3rd Party Mac Developer"
                                                    ))

                                        s.copy(
                                            signingIdentities = newIdentities,
                                            selectedIdentity = if (shouldUpdateSelection) identity else currentSelected
                                        )
                                    } else if (identity.contains("Installer") || identity.contains(
                                            "Distribution"
                                        )
                                    ) {
                                        val newIdentities =
                                            s.installerIdentities + identity
                                        s.copy(
                                            installerIdentities = newIdentities,
                                            selectedInstallerIdentity = s.selectedInstallerIdentity.ifEmpty { identity }
                                        )
                                    } else {
                                        s
                                    }
                                }
                            }
                        }

                        is ProcessOutput.Complete -> _state.update {
                            it.copy(
                                isLoadingIdentities = false
                            )
                        }

                        is ProcessOutput.Error -> _state.update {
                            it.copy(
                                isLoadingIdentities = false
                            )
                        }

                        else -> {}
                    }
                }
        }
    }

    fun onInstallerIdentitySelected(identity: String) {
        _state.update { it.copy(selectedInstallerIdentity = identity) }
    }

    private fun parseIdentity(line: String): String? {
        val regex = Regex("\\d+\\)\\s+[A-Z0-9]+\\s+\"(.+)\"")
        return regex.find(line)?.groupValues?.get(1)
    }

    fun onIdentitySelected(identity: String) {
        _state.update { it.copy(selectedIdentity = identity) }
    }

    fun onCredentialsChanged(issuerId: String, keyId: String, keyPath: String) {
        val uuidRegex =
            Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        val keyIdRegex = Regex("^[A-Z0-9]{10}$")

        _state.update {
            it.copy(
                apiIssuerId = issuerId,
                isIssuerIdValid = issuerId.isEmpty() || uuidRegex.matches(
                    issuerId
                ),
                apiKeyId = keyId,
                isKeyIdValid = keyId.isEmpty() || keyIdRegex.matches(keyId),
                apiKeyPath = keyPath
            )
        }
    }

    fun onIconPathChanged(path: String) {
        _state.update { it.copy(customIconPath = path, isIconValid = true) }
    }

    fun onBrowseIcon() {
        viewModelScope.launch {
            val path = fileSystemService.pickFile("png")
            if (path != null) {
                onIconPathChanged(path)
            }
        }
    }

    fun onBrowseApiKey() {
        viewModelScope.launch {
            val path = fileSystemService.pickFile("p8")
            if (path != null) {
                val filename = path.substringAfterLast("/")
                val keyIdMatch =
                    Regex("AuthKey_([A-Z0-9]{10})\\.p8").find(filename)
                val detectedKeyId =
                    keyIdMatch?.groupValues?.get(1) ?: _state.value.apiKeyId

                onCredentialsChanged(
                    _state.value.apiIssuerId,
                    detectedKeyId,
                    path
                )
            }
        }
    }

    fun startRelease() {
        releaseJob?.cancel()
        releaseJob = viewModelScope.launch {
            try {
                val issuerId = _state.value.apiIssuerId
                val keyId = _state.value.apiKeyId
                val keyPath = _state.value.apiKeyPath

                if (issuerId.isEmpty() || keyId.isEmpty() || keyPath.isEmpty()) {
                    _state.update { it.copy(releaseError = "App Store Connect credentials are required") }
                    return@launch
                }

                if (!fileSystemService.exists(keyPath)) {
                    _state.update { it.copy(releaseError = "API Key file not found at $keyPath") }
                    return@launch
                }

                saveCredentials()
                _state.update {
                    it.copy(
                        isReleasing = true,
                        releaseLogs = emptyList(),
                        releaseError = null
                    )
                }

                val root = _state.value.projectRoot

                // 1. Icon Regeneration (MUST happen before build)
                appendLog("Regenerating icons...")
                if (!regenerateIcons(root)) {
                    _state.update { it.copy(isReleasing = false) }
                    return@launch
                }

                // 2. Clean build (ensure no stale artifacts)
                appendLog("Cleaning build artifacts...")
                val cleanTask =
                    _state.value.selectedTask.substringBeforeLast(":") + ":clean"
                executeCommand(listOf("./gradlew", cleanTask), logCommand = true)

                // 3. Build
                appendLog("Starting build: ${_state.value.selectedTask}...")
                var buildExit = -1
                gradleService.runTask(root, _state.value.selectedTask)
                    .collect { output ->
                        when (output) {
                            is ProcessOutput.Stdout -> appendLog(output.line)
                            is ProcessOutput.Stderr -> appendLog(
                                output.line,
                                LogType.Error
                            )

                            is ProcessOutput.Complete -> buildExit = output.exitCode
                            is ProcessOutput.Error -> appendLog(
                                output.throwable.message ?: "Unknown error",
                                LogType.Error
                            )
                        }
                    }

                if (buildExit != 0) {
                    val appPath = findAppBundle(root)
                    if (appPath != null) {
                        appendLog(
                            "Build finished with errors (exit code $buildExit), but an .app bundle was found at $appPath.",
                            LogType.Error
                        )
                        appendLog(
                            "Proceeding with manual fixes and signing as per the recovery flow...",
                            LogType.Info
                        )
                    } else {
                        _state.update {
                            it.copy(
                                isReleasing = false,
                                releaseError = "Build failed with exit code $buildExit and no .app bundle was found."
                            )
                        }
                        releaseJob = null
                        return@launch
                    }
                } else {
                    appendLog("Build successful!", LogType.Success)
                }

                proceedToReleaseFlow()
            } catch (e: Exception) {
                appendLog("Unexpected error: ${e.message}", LogType.Error)
                _state.update {
                    it.copy(
                        isReleasing = false,
                        releaseError = e.message ?: "Unknown error"
                    )
                }
            } finally {
                releaseJob = null
                _state.update { it.copy(isReleasing = false) }
            }
        }
    }

    fun stopRelease() {
        releaseJob?.cancel()
        releaseJob = null
        _state.update {
            it.copy(
                isReleasing = false,
                releaseError = "Release process cancelled by user"
            )
        }
        appendLog("Release process stopped by user.", LogType.Error)
    }

    private suspend fun saveCredentials() {
        credentialService.saveCredential(
            "appstore_issuer_id",
            _state.value.apiIssuerId
        )
        credentialService.saveCredential(
            "appstore_key_id",
            _state.value.apiKeyId
        )
        credentialService.saveCredential(
            "appstore_key_path",
            _state.value.apiKeyPath
        )
    }

    private fun findSourceIcon(root: String): String? {
        if (_state.value.customIconPath.isNotEmpty() &&
            fileSystemService.exists(_state.value.customIconPath) &&
            fileSystemService.length(_state.value.customIconPath) > 0
        ) {
            return _state.value.customIconPath
        }

        val iconPaths = listOf(
            "$root/app/desktopApp/src/main/resources/icon.png",
            "$root/composeApp/src/desktopMain/icons/icon.png",
            "$root/composeApp/src/desktopMain/resources/icon.png",
            "$root/src/main/resources/icon.png",
            // Fallbacks to Android icons
            "$root/composeApp/src/androidMain/res/mipmap-xxxhdpi/ic_launcher.png",
            "$root/composeApp/src/androidMain/res/mipmap-xxxhdpi/ic_launcher.webp",
            "$root/app/androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            "$root/app/androidApp/src/main/res/mipmap-xxxhdpi/ic_launcher.webp"
        )

        return iconPaths.firstOrNull {
            fileSystemService.exists(it) && fileSystemService.length(it) > 0
        }
    }

    private suspend fun regenerateIcons(root: String): Boolean {
        val iconPng = findSourceIcon(root)
        if (iconPng == null) {
            val errorMsg =
                "Could not locate a valid icon.png or fallback icon in the project. Please select one manually."
            appendLog(errorMsg, LogType.Error)
            _state.update {
                it.copy(
                    isReleasing = false,
                    releaseError = errorMsg
                )
            }
            return false
        }

        appendLog("Regenerating icon.icns from $iconPng...")
        val resourcesDir = iconPng.substringBeforeLast("/")
        val iconsetDir = "$resourcesDir/icon.iconset"
        executeCommand(listOf("mkdir", "-p", iconsetDir), logCommand = true)

        val sizes = listOf(16, 32, 128, 256, 512)
        for (size in sizes) {
            val exit1 = executeCommand(
                listOf(
                    "sips",
                    "-s",
                    "format",
                    "png",
                    "-z",
                    "$size",
                    "$size",
                    iconPng,
                    "--out",
                    "$iconsetDir/icon_${size}x$size.png"
                ),
                logCommand = true
            )
            if (exit1 != 0) return false

            val doubleSize = size * 2
            val exit2 = executeCommand(
                listOf(
                    "sips",
                    "-s",
                    "format",
                    "png",
                    "-z",
                    "$doubleSize",
                    "$doubleSize",
                    iconPng,
                    "--out",
                    "$iconsetDir/icon_${size}x$size@2x.png"
                ),
                logCommand = true
            )
            if (exit2 != 0) return false
        }

        // Verify dimensions of the generated files
        appendLog("Verifying .iconset dimensions...")
        executeCommand(
            listOf(
                "sips",
                "-g",
                "pixelWidth",
                "-g",
                "pixelHeight",
                "$iconsetDir/icon_512x512@2x.png"
            ),
            logCommand = true
        )

        val targetIcns = "$resourcesDir/icon.icns"
        val exitCode = executeCommand(
            listOf(
                "iconutil",
                "-c",
                "icns",
                iconsetDir,
                "-o",
                targetIcns
            ),
            logCommand = true
        )
        executeCommand(listOf("rm", "-rf", iconsetDir), logCommand = true)

        if (exitCode == 0) {
            appendLog("Icon successfully generated at $targetIcns")
            // Verify final .icns by extracting it back to a temp folder
            val verifyDir = "/tmp/verify.iconset"
            executeCommand(listOf("rm", "-rf", verifyDir), logCommand = true)
            executeCommand(listOf("mkdir", "-p", verifyDir), logCommand = true)
            executeCommand(
                listOf(
                    "iconutil",
                    "-c",
                    "iconset",
                    targetIcns,
                    "-o",
                    verifyDir
                ),
                logCommand = true
            )
            appendLog("Verification: Extracting 512x512@2x layer from final .icns...")
            executeCommand(
                listOf(
                    "sips",
                    "-g",
                    "pixelWidth",
                    "-g",
                    "pixelHeight",
                    "$verifyDir/icon_512x512@2x.png"
                ),
                logCommand = true
            )
            executeCommand(listOf("rm", "-rf", verifyDir), logCommand = true)
        }

        return exitCode == 0
    }

    private fun findGeneratedIcons(root: String): String? {
        val iconPaths = listOf(
            "$root/app/desktopApp/src/main/resources/icon.icns",
            "$root/composeApp/src/desktopMain/icons/icon.icns",
            "$root/composeApp/src/desktopMain/resources/icon.icns",
            "$root/src/main/resources/icon.icns",
            // Also check Android resource folders if we regenerated there
            "$root/composeApp/src/androidMain/res/mipmap-xxxhdpi/icon.icns",
            "$root/app/androidApp/src/main/res/mipmap-xxxhdpi/icon.icns"
        )
        return iconPaths.firstOrNull { fileSystemService.exists(it) }
    }

    private suspend fun proceedToReleaseFlow() {
        val root = _state.value.projectRoot
        val identity = _state.value.selectedIdentity
        val installerIdentity = _state.value.selectedInstallerIdentity

        appendLog("Locating app bundle...")
        val appPath = findAppBundle(root)
        if (appPath == null) {
            val errorMsg =
                "Could not locate .app bundle in any expected directory under $root"
            _state.update {
                it.copy(
                    isReleasing = false,
                    releaseError = errorMsg
                )
            }
            return
        }
        val appName =
            appPath.substringAfterLast("/").substringBeforeLast(".app")
        appendLog("Found app bundle: $appPath")

        // Fix: Ensure the icon in the bundle is the one we regenerated and has all sizes
        val infoPlist = "$appPath/Contents/Info.plist"
        
        // Remove problematic subcomponents that break codesigning
        appendLog("Cleaning bundle subcomponents...")
        executeCommand(listOf("rm", "-f", "$appPath/Contents/app.provisionprofile"), logCommand = true)
        executeCommand(listOf("rm", "-rf", "$appPath/Contents/_CodeSignature"), logCommand = true)

        var bundledIconName = ""
        processService.execute(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Print :CFBundleIconFile",
                infoPlist
            )
        ).collect { output ->
            if (output is ProcessOutput.Stdout) bundledIconName =
                output.line.trim()
        }

        if (bundledIconName.isNotEmpty()) {
            if (!bundledIconName.endsWith(".icns")) bundledIconName += ".icns"
            val sourceIcons = findGeneratedIcons(root)
            if (sourceIcons != null) {
                appendLog("Replacing bundled icon $bundledIconName with high-quality version from $sourceIcons...")
                executeCommand(
                    listOf(
                        "cp",
                        sourceIcons,
                        "$appPath/Contents/Resources/$bundledIconName"
                    ),
                    logCommand = true
                )

                // Final Checkpoint 3: Verify the icon in the bundle
                appendLog("Verifying icon inside .app bundle...")
                val bundleVerifyDir = "/tmp/verify_bundle_icon.iconset"
                executeCommand(listOf("rm", "-rf", bundleVerifyDir), logCommand = true)
                executeCommand(listOf("mkdir", "-p", bundleVerifyDir), logCommand = true)
                executeCommand(
                    listOf(
                        "iconutil",
                        "-c",
                        "iconset",
                        "$appPath/Contents/Resources/$bundledIconName",
                        "-o",
                        bundleVerifyDir
                    ),
                    logCommand = true
                )
                executeCommand(
                    listOf(
                        "sips",
                        "-g",
                        "pixelWidth",
                        "-g",
                        "pixelHeight",
                        "$bundleVerifyDir/icon_512x512@2x.png"
                    ),
                    logCommand = true
                )
                executeCommand(listOf("rm", "-rf", bundleVerifyDir), logCommand = true)
            }
        }

        appendLog("Adjusting Info.plist...")
        executeCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Set :LSMinimumSystemVersion 12.0",
                infoPlist
            ),
            logCommand = true
        )

        // Sync CFBundleVersion with versionCode from project if possible
        val detectedVersionCode = getVersionCode(root)
        if (detectedVersionCode != null) {
            appendLog("Syncing CFBundleVersion with detected versionCode: $detectedVersionCode")
            executeCommand(
                listOf(
                    "/usr/libexec/PlistBuddy",
                    "-c",
                    "Set :CFBundleVersion $detectedVersionCode",
                    infoPlist
                ),
                logCommand = true
            )
        }

        executeCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Delete :ITSAppUsesNonExemptEncryption",
                infoPlist
            ),
            logCommand = true
        )
        executeCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Add :ITSAppUsesNonExemptEncryption bool false",
                infoPlist
            ),
            logCommand = true
        )

        // Apply selected category
        appendLog("Setting application category: ${_state.value.selectedCategory}")
        val categoryExit = executeCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Set :LSApplicationCategoryType ${_state.value.selectedCategory}",
                infoPlist
            ),
            logCommand = true
        )
        if (categoryExit != 0) {
            executeCommand(
                listOf(
                    "/usr/libexec/PlistBuddy",
                    "-c",
                    "Add :LSApplicationCategoryType string ${_state.value.selectedCategory}",
                    infoPlist
                ),
                logCommand = true
            )
        }

        val subproject = appPath.removePrefix(root).substringBefore("/build/")
        val provProfile =
            "$root$subproject/src/desktopMain/entitlements/app.provisionprofile"

        if (fileSystemService.exists(provProfile)) {
            appendLog("Cleaning quarantine from source profile $provProfile...")
            executeCommand(
                listOf(
                    "xattr",
                    "-d",
                    "com.apple.quarantine",
                    provProfile
                ),
                logCommand = true
            )
            appendLog("Embedding provisioning profile from $provProfile...")
            executeCommand(
                listOf(
                    "cp",
                    provProfile,
                    "$appPath/Contents/embedded.provisionprofile"
                ),
                logCommand = true
            )
        }

        appendLog("Removing quarantine attributes from $appPath...")
        executeCommand(
            listOf(
                "xattr",
                "-r",
                "-d",
                "com.apple.quarantine",
                appPath
            ),
            logCommand = true
        )

        appendLog("Deep signing subcomponents...")
        executeCommand(
            listOf(
                "find",
                appPath,
                "-type",
                "f",
                "(",
                "-name",
                "*.dylib",
                "-o",
                "-name",
                "*.so",
                "-o",
                "-name",
                "*.jnilib",
                ")",
                "-exec",
                "codesign",
                "-s",
                identity,
                "-vvvv",
                "--timestamp",
                "--options",
                "runtime",
                "--force",
                "{}",
                "+"
            ),
            logCommand = true
        )

        appendLog("Signing main executable...")
        val entitlementsPath =
            "$root$subproject/src/desktopMain/entitlements/entitlements.plist"
        val childEntitlementsPath =
            "$root$subproject/src/desktopMain/entitlements/child-entitlements.plist"

        val jspawnhelper =
            "$appPath/Contents/runtime/Contents/Home/lib/jspawnhelper"
        if (fileSystemService.exists(jspawnhelper)) {
            val cmd = mutableListOf(
                "codesign",
                "-s",
                identity,
                "-vvvv",
                "--timestamp",
                "--options",
                "runtime",
                "--force"
            )
            if (fileSystemService.exists(childEntitlementsPath)) cmd.addAll(
                listOf("--entitlements", childEntitlementsPath)
            )
            cmd.add(jspawnhelper)
            executeCommand(cmd, logCommand = true)
        }

        if (fileSystemService.exists(entitlementsPath)) {
            executeCommand(
                listOf(
                    "codesign",
                    "-s",
                    identity,
                    "-vvvv",
                    "--timestamp",
                    "--options",
                    "runtime",
                    "--entitlements",
                    entitlementsPath,
                    "--force",
                    "$appPath/Contents/MacOS/$appName"
                ),
                logCommand = true
            )
            executeCommand(
                listOf(
                    "codesign",
                    "-s",
                    identity,
                    "-vvvv",
                    "--timestamp",
                    "--options",
                    "runtime",
                    "--entitlements",
                    entitlementsPath,
                    "--force",
                    appPath
                ),
                logCommand = true
            )
        } else {
            executeCommand(
                listOf(
                    "codesign",
                    "-s",
                    identity,
                    "-vvvv",
                    "--timestamp",
                    "--options",
                    "runtime",
                    "--force",
                    appPath
                ),
                logCommand = true
            )
        }

        appendLog("Verifying signature and packaging...")
        executeCommand(
            listOf(
                "codesign",
                "--verify",
                "--deep",
                "--strict",
                "--verbose=4",
                appPath
            ),
            logCommand = true
        )

        val pkgDir = appPath.substringBeforeLast("/app") + "/pkg"
        val pkgOutput = "$pkgDir/$appName-manual.pkg"
        executeCommand(listOf("mkdir", "-p", pkgDir), logCommand = true)
        val pkgExit = executeCommand(
            listOf(
                "productbuild",
                "--component",
                appPath,
                "/Applications",
                "--sign",
                installerIdentity,
                pkgOutput
            ),
            logCommand = true
        )

        if (pkgExit != 0) {
            _state.update {
                it.copy(
                    isReleasing = false,
                    releaseError = "Packaging failed"
                )
            }
            return
        }

        appendLog("Submitting to App Store...")

        val home = fileSystemService.getUserHome() ?: ""
        val currentKeyPath = _state.value.apiKeyPath
        val keyFilename = currentKeyPath.substringAfterLast("/")

        val targetDir = "$home/private_keys"
        val targetPath = "$targetDir/$keyFilename"
        executeCommand(listOf("mkdir", "-p", targetDir))
        executeCommand(listOf("cp", currentKeyPath, targetPath))

        var lastErrorLine = ""
        processService.execute(
            listOf(
                "xcrun",
                "altool",
                "--upload-app",
                "-f",
                pkgOutput,
                "-t",
                "macos",
                "--apiKey",
                _state.value.apiKeyId,
                "--apiIssuer",
                _state.value.apiIssuerId
            ),
            directory = _state.value.projectRoot
        ).collect { output ->
            when (output) {
                is ProcessOutput.Stdout -> appendLog(output.line)
                is ProcessOutput.Stderr -> {
                    appendLog(output.line, LogType.Error)
                    if (output.line.contains(
                            "Error:",
                            ignoreCase = true
                        ) || output.line.contains(
                            "message",
                            ignoreCase = true
                        ) || output.line.contains(
                            "description",
                            ignoreCase = true
                        )
                    ) {
                        lastErrorLine = output.line
                    }
                }

                is ProcessOutput.Complete -> {
                    if (output.exitCode == 0) {
                        appendLog("All steps completed successfully!", LogType.Success)
                        _state.update { it.copy(releaseSuccess = true) }

                        // Try to find App Store ID
                        val bundleId = getBundleId(appPath)
                        if (bundleId != null) {
                            appendLog("Resolving App Store ID for bundle: $bundleId...")
                            val appId = appStoreConnectService.findAppId(
                                bundleId = bundleId,
                                issuerId = _state.value.apiIssuerId,
                                keyId = _state.value.apiKeyId,
                                keyPath = _state.value.apiKeyPath,
                                onLog = { appendLog(it, LogType.Info) }
                            )
                            if (appId != null) {
                                appendLog("Resolved App Store ID: $appId", LogType.Success)
                                _state.update { it.copy(appStoreId = appId) }
                            } else {
                                appendLog("Could not resolve App Store ID for bundle: $bundleId. Check API logs if possible.", LogType.Error)
                            }
                        } else {
                            appendLog("Could not determine bundle ID from Info.plist", LogType.Error)
                        }
                        _state.update { it.copy(isReleasing = false) }
                    } else {
                        val errorMsg = when {
                            lastErrorLine.contains("90249") || lastErrorLine.contains(
                                "LSApplicationCategoryType"
                            ) -> "Invalid Application Category."

                            lastErrorLine.isNotEmpty() -> lastErrorLine
                            else -> "Upload failed."
                        }
                        _state.update {
                            it.copy(
                                isReleasing = false,
                                releaseError = errorMsg
                            )
                        }
                    }
                }

                is ProcessOutput.Error -> _state.update {
                    it.copy(
                        isReleasing = false,
                        releaseError = output.throwable.message
                    )
                }
            }
        }
    }

    private fun getVersionCode(root: String): String? {
        val libsVersionsFile = "$root/gradle/libs.versions.toml"
        if (fileSystemService.exists(libsVersionsFile)) {
            val content = fileSystemService.readFile(libsVersionsFile) ?: ""
            val match = Regex("""versionCode\s*=\s*["']([^"']+)["']""").find(content)
            if (match != null) return match.groupValues[1]
            
            // Try without quotes just in case
            val matchNoQuotes = Regex("""versionCode\s*=\s*(\d+)""").find(content)
            if (matchNoQuotes != null) return matchNoQuotes.groupValues[1]
        }
        return null
    }

    private suspend fun getBundleId(appPath: String): String? {
        var bundleId: String? = null
        processService.execute(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Print :CFBundleIdentifier",
                "$appPath/Contents/Info.plist"
            ),
            directory = _state.value.projectRoot
        ).collect { output ->
            if (output is ProcessOutput.Stdout) bundleId = output.line.trim()
        }
        return bundleId
    }

    private suspend fun executeCommand(
        command: List<String>,
        directory: String? = null,
        logCommand: Boolean = false
    ): Int {
        if (logCommand) {
            appendLog("Executing: ${command.joinToString(" ")}")
        }
        var exitCode = -1
        processService.execute(command, directory ?: _state.value.projectRoot)
            .collect { output ->
                when (output) {
                    is ProcessOutput.Stdout -> {
                        if (logCommand) appendLog(output.line)
                    }

                    is ProcessOutput.Stderr -> appendLog(
                        output.line,
                        LogType.Error
                    )

                    is ProcessOutput.Complete -> exitCode = output.exitCode
                    is ProcessOutput.Error -> appendLog(
                        output.throwable.message ?: "Unknown error",
                        LogType.Error
                    )
                }
            }
        return exitCode
    }

    private fun findAppBundle(root: String): String? {
        val searchPaths = listOf(
            "/build/compose/binaries/main-release/app",
            "/composeApp/build/compose/binaries/main-release/app",
            "/app/desktopApp/build/compose/binaries/main-release/app",
            "/desktop/build/compose/binaries/main-release/app"
        )
        for (relPath in searchPaths) {
            val buildDir = "$root$relPath"
            if (fileSystemService.exists(buildDir)) {
                val files = fileSystemService.listFiles(buildDir)
                val app = files.firstOrNull { it.endsWith(".app") }
                if (app != null) return app
            }
        }
        return null
    }

    private fun appendLog(message: String, type: LogType = LogType.Info) {
        _state.update {
            it.copy(
                releaseLogs = it.releaseLogs + LogEntry(
                    message,
                    type
                )
            )
        }
    }
}
