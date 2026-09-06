package com.composeship.feature.wearrelease.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.wearrelease.data.service.DesktopCredentialService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WearReleaseViewModel(
    private val processService: ProcessService,
    private val fileSystemService: FileSystemService,
    private val credentialService: DesktopCredentialService
) : ViewModel() {

    private val _state = MutableStateFlow(WearReleaseState())
    val state: StateFlow<WearReleaseState> = _state.asStateFlow()

    fun onProjectRootChanged(path: String) {
        _state.update { it.copy(projectRoot = path) }
        validateProject(path)
    }

    fun onBrowseProjectRoot() {
        viewModelScope.launch {
            val path = fileSystemService.pickDirectory()
            if (path != null) {
                _state.update { it.copy(lastOutputLines = it.lastOutputLines + "Selected project root: $path") }
                onProjectRootChanged(path)
            } else {
                _state.update { it.copy(lastOutputLines = it.lastOutputLines + "Project root selection canceled.") }
            }
        }
    }

    private fun validateProject(path: String) {
        if (path.isEmpty()) return
        val settingsExists = fileSystemService.exists("$path/pubspec.yaml") || fileSystemService.exists("$path/android")
        _state.update { it.copy(isProjectValid = settingsExists) }
        // If project looks valid and packageName not provided, try to auto-detect it
        if (settingsExists && _state.value.packageName.isEmpty()) {
            try {
                val detected = detectPackageName(path)
                if (!detected.isNullOrEmpty()) {
                    _state.update { it.copy(packageName = detected, lastOutputLines = it.lastOutputLines + "Auto-detected package: $detected") }
                }
            } catch (e: Exception) {
                // ignore detection errors
            }
        }
    }

    private fun detectPackageName(projectRoot: String): String? {
        // 1) AndroidManifest.xml
        val manifestPaths = listOf(
            "$projectRoot/android/app/src/main/AndroidManifest.xml",
            "$projectRoot/android/src/main/AndroidManifest.xml",
            "$projectRoot/android/app/src/main/AndroidManifest.xml"
        )

        for (p in manifestPaths) {
            try {
                val content = fileSystemService.readFile(p) ?: continue
                val m = Regex("<manifest[^>]*\\bpackage\\s*=\\s*\"(.*?)\"").find(content)
                if (m != null) return m.groupValues[1]
            } catch (_: Exception) {}
        }

        // 2) build.gradle (applicationId) or build.gradle.kts (namespace)
        val gradlePaths = listOf(
            "$projectRoot/android/app/build.gradle",
            "$projectRoot/android/app/build.gradle.kts",
            "$projectRoot/android/build.gradle",
            "$projectRoot/android/build.gradle.kts"
        )

        for (p in gradlePaths) {
            try {
                val content = fileSystemService.readFile(p) ?: continue
                // applicationId 'com.example.app' or "com.example.app"
                Regex("applicationId\\s+['\"](.*?)['\"]").find(content)?.let { return it.groupValues[1] }
                // namespace = "com.example.app"
                Regex("namespace\\s*=\\s*['\"](.*?)['\"]").find(content)?.let { return it.groupValues[1] }
            } catch (_: Exception) {}
        }

        return null
    }

    fun buildWear(projectRoot: String) {
        if (!_state.value.isProjectValid) {
            _state.update { it.copy(lastOutputLines = (it.lastOutputLines + "Invalid project root. Select a Flutter project (pubspec.yaml or android/ present).")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(buildInProgress = true, lastOutputLines = listOf("Starting flutter build...")) }
            processService.execute(listOf("flutter", "build", "appbundle", "--flavor", "wear", "--release"), projectRoot)
                .collect { output ->
                    when (output) {
                        is ProcessOutput.Stdout -> _state.update { it.copy(lastOutputLines = (it.lastOutputLines + output.line).takeLast(200)) }
                        is ProcessOutput.Stderr -> _state.update { it.copy(lastOutputLines = (it.lastOutputLines + output.line).takeLast(200)) }
                        is ProcessOutput.Complete -> _state.update { it.copy(buildInProgress = false) }
                        is ProcessOutput.Error -> _state.update { it.copy(buildInProgress = false, lastOutputLines = it.lastOutputLines + "ERROR: ${output.throwable.message}") }
                    }
                }
        }
    }

    fun generateReleaseNotesFromGit(projectRoot: String) {
        viewModelScope.launch {
            val lastTagCmd = listOf("git", "describe", "--tags", "--abbrev=0")
            var fromRef = ""
            try {
                processService.execute(lastTagCmd, projectRoot).collect { o ->
                    if (o is ProcessOutput.Stdout) fromRef = o.line.trim()
                }
            } catch (e: Exception) {
                // no tag
                fromRef = ""
            }

            val logCmd = if (fromRef.isNotEmpty()) listOf("git", "log", "$fromRef..HEAD", "--pretty=format:- %s (%an)")
            else listOf("git", "log", "--pretty=format:- %s (%an)")

            val sb = StringBuilder()
            processService.execute(logCmd, projectRoot).collect { o ->
                if (o is ProcessOutput.Stdout) {
                    sb.appendLine(o.line)
                }
            }

            val notes = sb.toString().trim()
            _state.update { it.copy(releaseNotes = notes) }
        }
    }

    fun onReleaseNotesChanged(text: String) {
        _state.update { it.copy(releaseNotes = text) }
    }

    init {
        viewModelScope.launch {
            loadSavedCredentials()
        }
    }

    private suspend fun loadSavedCredentials() {
        val svc = credentialService
        val sa = svc.getCredential("wear_service_account") ?: ""
        val pkg = svc.getCredential("wear_package_name") ?: ""
        val ks = svc.getCredential("wear_keystore_path") ?: ""
        val alias = svc.getCredential("wear_keystore_alias") ?: ""

        _state.update { it.copy(serviceAccountPath = sa, packageName = pkg, keystorePath = ks, keystoreAlias = alias) }
    }

    fun saveCredentials() {
        viewModelScope.launch {
            credentialService.saveCredential("wear_service_account", _state.value.serviceAccountPath)
            credentialService.saveCredential("wear_package_name", _state.value.packageName)
            credentialService.saveCredential("wear_keystore_path", _state.value.keystorePath)
            credentialService.saveCredential("wear_keystore_alias", _state.value.keystoreAlias)
            _state.update { it.copy(lastOutputLines = it.lastOutputLines + "Credentials saved.") }
        }
    }

    fun onServiceAccountPathChanged(path: String) {
        _state.update { it.copy(serviceAccountPath = path) }
    }

    fun onPackageNameChanged(pkg: String) {
        _state.update { it.copy(packageName = pkg) }
    }

    fun onKeystorePathChanged(path: String) {
        _state.update { it.copy(keystorePath = path) }
    }

    fun onKeystoreAliasChanged(alias: String) {
        _state.update { it.copy(keystoreAlias = alias) }
    }

    fun onTrackChanged(track: String) {
        _state.update { it.copy(track = track) }
    }

    fun onBrowseServiceAccount() {
        viewModelScope.launch {
            val p = fileSystemService.pickFile("json")
            if (p != null) {
                _state.update { it.copy(serviceAccountPath = p, lastOutputLines = it.lastOutputLines + "Selected service account: $p") }
            } else {
                _state.update { it.copy(lastOutputLines = it.lastOutputLines + "Service account selection canceled.") }
            }
        }
    }

    fun onBrowseKeystore() {
        viewModelScope.launch {
            val p = fileSystemService.pickFile("jks")
            if (p != null) {
                _state.update { it.copy(keystorePath = p, lastOutputLines = it.lastOutputLines + "Selected keystore: $p") }
            } else {
                _state.update { it.copy(lastOutputLines = it.lastOutputLines + "Keystore selection canceled.") }
            }
        }
    }

    private fun chooseAab(projectRoot: String): String? {
        val preferred = listOf(
            "$projectRoot/build/app/outputs/bundle/wearRelease/app-wear-release.aab",
            "$projectRoot/build/app/outputs/bundle/wearRelease/app-wear-release.aab",
            "$projectRoot/build/outputs/apk/wearRelease/app-wear-release.aab",
            "$projectRoot/build/app/outputs/bundle/wearRelease",
            "$projectRoot/build/app/outputs/bundle"
        )

        for (p in preferred) {
            try {
                if (fileSystemService.exists(p)) return p
            } catch (_: Exception) {}
        }

        val found = try {
            fileSystemService.listFiles("$projectRoot").firstOrNull { it.endsWith(".aab") }
        } catch (e: Exception) { null }

        return found?.let { if (it.startsWith("/")) it else "$projectRoot/$it" }
    }

    private suspend fun signAabIfNeeded(projectRoot: String, aabPath: String?): String? {
        if (aabPath == null) return null
        try {
            val verifyCmd = listOf("jarsigner", "-verify", "-verbose", "-certs", aabPath)
            var alreadySigned = false
            processService.execute(verifyCmd, projectRoot).collect { o ->
                if (o is ProcessOutput.Stdout && o.line.contains("jar signed.")) {
                    alreadySigned = true
                }
            }

            if (alreadySigned) return aabPath

            val ks = _state.value.keystorePath.ifEmpty { null }
            if (ks == null) {
                _state.update { it.copy(lastOutputLines = it.lastOutputLines + "No keystore provided; cannot sign AAB.") }
                return null
            }

            val kp = "$projectRoot/android/key.properties"
            var storepass = ""
            var keypass = ""
            var alias = _state.value.keystoreAlias
            if (fileSystemService.exists(kp)) {
                val content = fileSystemService.readFile(kp) ?: ""
                storepass = Regex("SIGNING_KEY_RELEASE_PASSWORD=(.*)").find(content)?.groupValues?.get(1) ?: ""
                keypass = Regex("SIGNING_KEY_RELEASE_KEY_PASSWORD=(.*)").find(content)?.groupValues?.get(1) ?: ""
                alias = alias.ifEmpty { Regex("SIGNING_KEY_RELEASE_KEY=(.*)").find(content)?.groupValues?.get(1) ?: alias }
            }

            val cmd = mutableListOf("jarsigner", "-keystore", ks)
            if (storepass.isNotEmpty()) cmd.addAll(listOf("-storepass", storepass))
            if (keypass.isNotEmpty()) cmd.addAll(listOf("-keypass", keypass))
            cmd.addAll(listOf(aabPath, alias.ifEmpty { "release" }))

            processService.execute(cmd, projectRoot).collect { o ->
                when (o) {
                    is ProcessOutput.Stdout -> _state.update { it.copy(lastOutputLines = (it.lastOutputLines + o.line).takeLast(200)) }
                    is ProcessOutput.Stderr -> _state.update { it.copy(lastOutputLines = (it.lastOutputLines + o.line).takeLast(200)) }
                    else -> {}
                }
            }

            return aabPath
        } catch (e: Exception) {
            _state.update { it.copy(lastOutputLines = it.lastOutputLines + "Signing error: ${e.message}") }
            return null
        }
    }

    fun prepareAndUpload(projectRoot: String) {
        viewModelScope.launch {
            _state.update { it.copy(lastOutputLines = it.lastOutputLines + "Preparing upload...") }
            val aab = chooseAab(projectRoot)
            if (aab == null) {
                _state.update { it.copy(lastOutputLines = it.lastOutputLines + "No .aab found. Build first.") }
                return@launch
            }

            if (_state.value.track == "production" && !_state.value.needsConfirmation) {
                _state.update { it.copy(needsConfirmation = true, lastOutputLines = it.lastOutputLines + "Production selected — press Upload again to confirm.") }
                return@launch
            }

            val signed = signAabIfNeeded(projectRoot, aab)
            if (signed == null) return@launch

            saveCredentials()

            uploadToPlayFastlane(projectRoot, signed, _state.value.serviceAccountPath, _state.value.packageName, _state.value.track)
        }
    }

    fun findAabPath(projectRoot: String): String? {
        val candidates = listOf(
            "$projectRoot/build/app/outputs/bundle/release",
            "$projectRoot/build/app/outputs/bundle/wearRelease",
            "$projectRoot/android/app/build/outputs/bundle/release",
            "$projectRoot/build/outputs"
        )

        for (base in candidates) {
            try {
                if (fileSystemService.exists(base)) {
                    val files = fileSystemService.listFiles(base)
                    val aab = files.firstOrNull { it.endsWith(".aab") }
                    if (aab != null) return if (aab.startsWith("/")) aab else "$base/$aab"
                }
            } catch (e: Exception) {
                /* ignore */
            }
        }
        return null
    }

    fun uploadToPlayFastlane(projectRoot: String, aabPath: String?, jsonKeyPath: String, packageName: String, track: String = "production") {
        viewModelScope.launch {
            val actualAab = aabPath ?: findAabPath(projectRoot)
            if (actualAab == null) {
                _state.update { it.copy(lastOutputLines = it.lastOutputLines + "No .aab found. Build first or provide path.") }
                return@launch
            }
            _state.update { it.copy(buildInProgress = true, lastOutputLines = listOf("Starting fastlane supply...") ) }
            val cmd = listOf(
                "fastlane",
                "supply",
                "--aab", actualAab,
                "--json_key", jsonKeyPath,
                "--package_name", packageName,
                "--track", track,
                "--skip_upload_metadata", "true",
                "--skip_upload_images", "true",
                "--skip_upload_screenshots", "true"
            )

            processService.execute(cmd, projectRoot).collect { output ->
                when (output) {
                    is ProcessOutput.Stdout -> _state.update { it.copy(lastOutputLines = (it.lastOutputLines + output.line).takeLast(200)) }
                    is ProcessOutput.Stderr -> _state.update { it.copy(lastOutputLines = (it.lastOutputLines + output.line).takeLast(200)) }
                    is ProcessOutput.Complete -> _state.update { it.copy(buildInProgress = false) }
                    is ProcessOutput.Error -> _state.update { it.copy(buildInProgress = false, lastOutputLines = it.lastOutputLines + "ERROR: ${output.throwable.message}") }
                }
            }
        }
    }
}
