package com.composeship.feature.macosrelease.ui

import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.macosrelease.domain.service.AppStoreConnectService
import com.composeship.feature.macosrelease.domain.service.CredentialService
import com.composeship.feature.macosrelease.domain.service.GradleService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class MacOsReleaseViewModelJvmTest {

    @Test
    fun startReleaseExtractsXmlEntitlementsAndSyncsVersionCode() = runBlocking {
        val files = mapOf(
            "/root/gradlew" to "",
            "/root/build.gradle.kts" to "plugins { alias(libs.plugins.composeMultiplatform) apply false }",
            "/root/settings.gradle.kts" to "include(\":app:desktopApp\")",
            "/root/gradle/libs.versions.toml" to """
                [versions]
                versionName = "0.0.5"
                versionCode = "5"
            """.trimIndent(),
            "/root/app/desktopApp/build.gradle.kts" to """
                plugins { alias(libs.plugins.composeMultiplatform) }
                compose.desktop {
                    application { mainClass = "com.composeship.MainKt" }
                }
            """.trimIndent(),
            "/root/app/desktopApp/src/main/resources/icon.png" to "png_bytes",
            "/root/app/desktopApp/src/desktopMain/entitlements/comcomposeshipapp_Distribution_Profile.provisionprofile" to "profile",
            "/root/app/desktopApp/src/desktopMain/entitlements/entitlements.plist" to "<plist></plist>",
            "/root/app/desktopApp/build/compose/binaries/main-release/app/com.composeship.app/Contents/Info.plist" to "<plist></plist>",
            "/root/private_keys/AuthKey_ABC1234567.p8" to "key_content",
            "/tmp/composeship_runtime_entitlements.plist" to "<plist></plist>"
        )

        val credentialService = FakeCredentialService(
            mapOf(
                "appstore_issuer_id" to "12345678-1234-1234-1234-123456789012",
                "appstore_key_id" to "ABC1234567",
                "appstore_key_path" to "/root/private_keys/AuthKey_ABC1234567.p8"
            )
        )
        val fakeProcessService = FakeProcessService()
        val viewModel = MacOsReleaseViewModel(
            processService = fakeProcessService,
            gradleService = FakeGradleService(),
            fileSystemService = FakeFileSystemService(files),
            credentialService = credentialService,
            appStoreConnectService = FakeAppStoreConnectService()
        )

        // Wait briefly for init block's loadSavedCredentials to complete
        delay(200)

        viewModel.onProjectRootChanged("/root")
        viewModel.onIdentitySelected("3rd Party Mac Developer Application: Test Identity")
        viewModel.onInstallerIdentitySelected("3rd Party Mac Developer Installer: Test Identity")

        viewModel.startRelease()

        // Wait for release coroutine to start and finish
        delay(1000)

        val state = viewModel.state.value
        val logs = state.releaseLogs.joinToString("\n") { "${it.type}: ${it.message}" }
        val commands = fakeProcessService.executedCommands.joinToString("\n") { it.joinToString(" ") }

        val versionCodeCmd = fakeProcessService.executedCommands.firstOrNull { cmd ->
            cmd.contains("Set :CFBundleVersion 5")
        }
        assertTrue(versionCodeCmd != null, "Error: ${state.releaseError}\nLogs:\n$logs\nCommands:\n$commands")

        val scriptCmd = fakeProcessService.executedCommands.firstOrNull { cmd ->
            cmd.joinToString(" ").contains("PlistBuddy -x -c \"Print :Entitlements\"")
        }
        assertTrue(scriptCmd != null, "Should have generated XML entitlements using PlistBuddy -x")

        val codesignCmd = fakeProcessService.executedCommands.firstOrNull { cmd ->
            cmd.contains("codesign") && cmd.contains("/tmp/composeship_runtime_entitlements.plist")
        }
        assertTrue(codesignCmd != null, "Should have signed with runtime entitlements file /tmp/composeship_runtime_entitlements.plist")
    }

    private class FakeFileSystemService(
        private val files: Map<String, String>
    ) : FileSystemService {
        override fun exists(path: String): Boolean {
            if (files.containsKey(path)) return true
            val dirPrefix = if (path.endsWith("/")) path else "$path/"
            return files.keys.any { it.startsWith(dirPrefix) }
        }
        override fun length(path: String): Long = files[path]?.length?.toLong() ?: 100L
        override fun listFiles(path: String): List<String> {
            val normalizedPath = if (path.endsWith("/")) path else "$path/"
            return files.keys
                .filter { it.startsWith(normalizedPath) }
                .map { relative ->
                    val suffix = relative.removePrefix(normalizedPath)
                    val firstSegment = suffix.substringBefore("/")
                    normalizedPath + firstSegment
                }
                .distinct()
        }
        override fun getUserHome(): String = "/user/home"
        override fun readFile(path: String): String? = files[path]
        override suspend fun pickDirectory(): String? = null
        override suspend fun pickFile(extension: String?): String? = null
    }

    private class FakeProcessService : ProcessService {
        val executedCommands = mutableListOf<List<String>>()

        override fun execute(
            command: List<String>,
            directory: String?,
            env: Map<String, String>
        ): Flow<ProcessOutput> {
            executedCommands.add(command)
            val stdout = if (command.contains("Print :CFBundleIconFile")) {
                ProcessOutput.Stdout("icon.icns")
            } else {
                null
            }
            return if (stdout != null) {
                flowOf(stdout, ProcessOutput.Complete(0))
            } else {
                flowOf(ProcessOutput.Complete(0))
            }
        }
    }

    private class FakeGradleService : GradleService {
        override fun runTask(
            projectRoot: String,
            taskName: String,
            arguments: List<String>
        ): Flow<ProcessOutput> = flowOf(ProcessOutput.Complete(0))

        override fun findAvailableTasks(projectRoot: String): List<String> = emptyList()
    }

    private class FakeCredentialService(
        initialStorage: Map<String, String> = emptyMap()
    ) : CredentialService {
        private val storage = initialStorage.toMutableMap()
        override suspend fun saveCredential(key: String, value: String) { storage[key] = value }
        override suspend fun getCredential(key: String): String? = storage[key]
        override suspend fun deleteCredential(key: String) { storage.remove(key) }
    }

    private class FakeAppStoreConnectService : AppStoreConnectService {
        override suspend fun findAppId(
            bundleId: String,
            issuerId: String,
            keyId: String,
            keyPath: String,
            onLog: (String) -> Unit
        ): String? = null
    }
}
