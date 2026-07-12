package com.composeship.feature.macosrelease.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.feature.macosrelease.domain.service.CredentialService
import com.composeship.feature.macosrelease.domain.service.FileSystemService
import com.composeship.feature.macosrelease.domain.service.GradleService
import com.composeship.feature.macosrelease.domain.service.ProcessOutput
import com.composeship.feature.macosrelease.domain.service.ProcessService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MacOsReleaseViewModel(
    private val processService: ProcessService,
    private val gradleService: GradleService,
    private val fileSystemService: FileSystemService,
    private val credentialService: CredentialService
) : ViewModel() {

    private val _state = MutableStateFlow(MacOsReleaseState())
    val state: StateFlow<MacOsReleaseState> = _state.asStateFlow()

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
        val home = try {
            System.getProperty("user.home")
        } catch (e: Exception) {
            println("Error getting user.home: ${e.message}")
            null
        } ?: return
        val searchRoots = mutableListOf(
            "$home/.appstoreconnect/private_keys",
            "$home/private_keys",
            "$home/Downloads",
            "$home/development"
        )

        // Add first-level subdirectories of ~/development if they exist
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
                println("Error accessing $dir: ${e.message}")
            }
        }

        if (detectedFiles.isNotEmpty()) {
            val fileList = detectedFiles.toList()
            _state.update { it.copy(detectedApiKeyFiles = fileList) }

            // Autofill only if nothing was loaded from credentials storage
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
                // Only add tasks for modules that actually use the compose plugin AND apply it
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

        // Fallback if no subprojects found or if it's a simple structure
        if (foundTasks.isEmpty()) {
            foundTasks.add(":packageReleasePkg")
        }

        // Add debug counterparts for the discovered release tasks
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

    fun nextStep() {
        val next = when (_state.value.step) {
            ReleaseStep.SelectProject -> ReleaseStep.SelectTask
            ReleaseStep.SelectTask -> {
                loadSigningIdentities()
                ReleaseStep.SigningIdentity
            }

            ReleaseStep.SigningIdentity -> ReleaseStep.AppStoreCredentials
            ReleaseStep.AppStoreCredentials -> ReleaseStep.Process
            ReleaseStep.Process -> ReleaseStep.Process
        }
        _state.update { it.copy(step = next) }
    }

    fun previousStep() {
        val prev = when (_state.value.step) {
            ReleaseStep.SelectProject -> ReleaseStep.SelectProject
            ReleaseStep.SelectTask -> ReleaseStep.SelectProject
            ReleaseStep.SigningIdentity -> ReleaseStep.SelectTask
            ReleaseStep.AppStoreCredentials -> ReleaseStep.SigningIdentity
            ReleaseStep.Process -> ReleaseStep.AppStoreCredentials
        }
        _state.update { it.copy(step = prev) }
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
        viewModelScope.launch {
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

            // 1. Build
            appendLog("Step 1/8: Starting build: ${_state.value.selectedTask}...")
            var buildExit = -1
            gradleService.runTask(
                _state.value.projectRoot,
                _state.value.selectedTask
            )
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
                _state.update {
                    it.copy(
                        isReleasing = false,
                        releaseError = "Build failed with exit code $buildExit"
                    )
                }
                return@launch
            }
            appendLog("Build successful!", LogType.Success)

            proceedToReleaseFlow()
        }
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

    private suspend fun proceedToReleaseFlow() {
        val root = _state.value.projectRoot
        val identity = _state.value.selectedIdentity
        val installerIdentity = _state.value.selectedInstallerIdentity

        appendLog("Step 2/8: Locating app bundle...")
        val appPath = findAppBundle(root)
        if (appPath == null) {
            val errorMsg =
                "Could not locate .app bundle in any expected directory under $root"
            val tip =
                "\nTip: Ensure your project has 'packageName' configured in compose.desktop.nativeDistributions and the build task produced a .app bundle."
            _state.update {
                it.copy(
                    isReleasing = false,
                    releaseError = errorMsg + tip
                )
            }
            return
        }
        val appName =
            appPath.substringAfterLast("/").substringBeforeLast(".app")
        appendLog("Found app bundle: $appPath")

        appendLog("Step 3/8: Removing quarantine attributes from $appPath...")
        executeCommand(
            listOf(
                "xattr",
                "-r",
                "-d",
                "com.apple.quarantine",
                appPath
            )
        )

        appendLog("Adjusting Info.plist...")
        val infoPlist = "$appPath/Contents/Info.plist"
        executeCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Set :LSMinimumSystemVersion 12.0",
                infoPlist
            )
        )
        executeCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Delete :ITSAppUsesNonExemptEncryption",
                infoPlist
            )
        )
        executeCommand(
            listOf(
                "/usr/libexec/PlistBuddy",
                "-c",
                "Add :ITSAppUsesNonExemptEncryption bool false",
                infoPlist
            )
        )

        val subproject = appPath.removePrefix(root).substringBefore("/build/")
        val provProfile =
            "$root$subproject/src/desktopMain/entitlements/app.provisionprofile"

        if (fileSystemService.exists(provProfile)) {
            appendLog("Step 4/8: Embedding provisioning profile from $provProfile...")
            executeCommand(
                listOf(
                    "cp",
                    provProfile,
                    "$appPath/Contents/embedded.provisionprofile"
                )
            )
        } else {
            appendLog(
                "Step 4/8: Warning: Provisioning profile not found at $provProfile. Skipping manual embed.",
                LogType.Error
            )
        }

        appendLog("Step 5/8: Deep signing subcomponents...")
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
            )
        )

        appendLog("Step 6/8: Signing main executable...")
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
            if (fileSystemService.exists(childEntitlementsPath)) {
                cmd.addAll(listOf("--entitlements", childEntitlementsPath))
            }
            cmd.add(jspawnhelper)
            executeCommand(cmd)
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
                )
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
                )
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
                )
            )
        }

        appendLog("Step 7/8: Verifying signature and packaging...")
        executeCommand(
            listOf(
                "codesign",
                "--verify",
                "--deep",
                "--strict",
                "--verbose=4",
                appPath
            )
        )

        val pkgDir = appPath.substringBeforeLast("/app") + "/pkg"
        val pkgOutput = "$pkgDir/$appName-manual.pkg"
        executeCommand(listOf("mkdir", "-p", pkgDir))
        val pkgExit = executeCommand(
            listOf(
                "productbuild",
                "--component",
                appPath,
                "/Applications",
                "--sign",
                installerIdentity,
                pkgOutput
            )
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

        appendLog("Step 8/8: Submitting to App Store...")
        
        // Fix: altool requires the key to be in specific locations.
        val home = System.getProperty("user.home") ?: ""
        val approvedLocations = listOf(
            "$home/.appstoreconnect/private_keys",
            "$home/private_keys",
            "$home/.private_keys"
        )
        
        val currentKeyPath = _state.value.apiKeyPath
        val keyFilename = currentKeyPath.substringAfterLast("/")
        val isApproved = approvedLocations.any { currentKeyPath.startsWith(it) }
        
        if (!isApproved) {
            val targetDir = "$home/private_keys"
            val targetPath = "$targetDir/$keyFilename"
            appendLog("Copying API key to approved location: $targetPath")
            executeCommand(listOf("mkdir", "-p", targetDir))
            executeCommand(listOf("cp", currentKeyPath, targetPath))
        }

        val uploadExit = executeCommand(
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
            )
        )

        if (uploadExit == 0) {
            appendLog("All steps completed successfully!", LogType.Success)
            _state.update {
                it.copy(
                    isReleasing = false,
                    releaseSuccess = true
                )
            }
        } else {
            val errorMsg = "Upload failed. This usually means your API Key, Issuer ID, or Team ID is incorrect, or your account lacks permissions."
            _state.update {
                it.copy(
                    isReleasing = false,
                    releaseError = errorMsg
                )
            }
        }
    }

    private suspend fun executeCommand(command: List<String>): Int {
        var exitCode = -1
        processService.execute(command, directory = _state.value.projectRoot)
            .collect { output ->
                when (output) {
                    is ProcessOutput.Stdout -> appendLog(output.line)
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
                releaseLogs = it.releaseLogs + LogEntry(message, type)
            )
        }
    }
}
