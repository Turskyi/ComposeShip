package com.composeship.feature.macosrelease.ui

import com.composeship.core.domain.service.FileSystemService
import com.composeship.core.domain.service.ProcessOutput
import com.composeship.core.domain.service.ProcessService
import com.composeship.feature.macosrelease.domain.service.AppStoreConnectService
import com.composeship.feature.macosrelease.domain.service.CredentialService
import com.composeship.feature.macosrelease.domain.service.GradleService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacOsReleaseViewModelTest {

    @Test
    fun detectsTasksForNestedDesktopSubprojectWithoutIncludingFeatureModules() {
        val files = mapOf(
            "/root/gradlew" to "",
            "/root/settings.gradle.kts" to """
                include(":app:androidApp")
                include(":app:desktopApp")
                include(":feature-macos-release")
                include(":feature-cloud-run-deploy")
            """.trimIndent(),
            "/root/app/androidApp/build.gradle.kts" to """
                plugins { alias(libs.plugins.androidApplication) }
            """.trimIndent(),
            "/root/app/desktopApp/build.gradle.kts" to """
                plugins { alias(libs.plugins.composeMultiplatform) }
                compose.desktop {
                    application {
                        mainClass = "com.composeship.MainKt"
                    }
                }
            """.trimIndent(),
            "/root/feature-macos-release/build.gradle.kts" to """
                plugins { alias(libs.plugins.composeMultiplatform) }
                kotlin {
                    sourceSets {
                        val jvmMain by getting {
                            dependencies { implementation(compose.desktop.currentOs) }
                        }
                    }
                }
            """.trimIndent(),
            "/root/feature-cloud-run-deploy/build.gradle.kts" to """
                plugins { alias(libs.plugins.composeMultiplatform) }
            """.trimIndent()
        )

        val viewModel = createViewModel(files)
        viewModel.onProjectRootChanged("/root")

        val state = viewModel.state.value
        assertTrue(state.isProjectValid)
        assertEquals(
            listOf(":app:desktopApp:packageReleasePkg", ":app:desktopApp:packagePkg"),
            state.availableTasks
        )
        assertEquals(":app:desktopApp:packageReleasePkg", state.selectedTask)
    }

    @Test
    fun detectsTasksForSingleModuleComposeApp() {
        val files = mapOf(
            "/root/gradlew" to "",
            "/root/settings.gradle.kts" to """
                include(":composeApp")
            """.trimIndent(),
            "/root/composeApp/build.gradle.kts" to """
                plugins { alias(libs.plugins.composeMultiplatform) }
                compose.desktop {
                    application {
                        mainClass = "com.example.MainKt"
                    }
                }
            """.trimIndent()
        )

        val viewModel = createViewModel(files)
        viewModel.onProjectRootChanged("/root")

        val state = viewModel.state.value
        assertEquals(
            listOf(":composeApp:packageReleasePkg", ":composeApp:packagePkg"),
            state.availableTasks
        )
        assertEquals(":composeApp:packageReleasePkg", state.selectedTask)
    }

    @Test
    fun onTaskSelectedNormalizesSlashesToColons() {
        val viewModel = createViewModel(emptyMap())
        viewModel.onTaskSelected(":app/desktopApp:packageReleasePkg")

        assertEquals(":app:desktopApp:packageReleasePkg", viewModel.state.value.selectedTask)
    }

    private fun createViewModel(files: Map<String, String>): MacOsReleaseViewModel {
        return MacOsReleaseViewModel(
            processService = FakeProcessService(),
            gradleService = FakeGradleService(),
            fileSystemService = FakeFileSystemService(files),
            credentialService = FakeCredentialService(),
            appStoreConnectService = FakeAppStoreConnectService()
        )
    }

    private class FakeFileSystemService(
        private val files: Map<String, String>
    ) : FileSystemService {
        override fun exists(path: String): Boolean = files.containsKey(path)
        override fun length(path: String): Long = files[path]?.length?.toLong() ?: 0L
        override fun listFiles(path: String): List<String> =
            files.keys.filter { it.startsWith(path) }
        override fun getUserHome(): String = "/user/home"
        override fun readFile(path: String): String? = files[path]
        override suspend fun pickDirectory(): String? = null
        override suspend fun pickFile(extension: String?): String? = null
    }

    private class FakeProcessService : ProcessService {
        override fun execute(
            command: List<String>,
            directory: String?,
            env: Map<String, String>
        ): Flow<ProcessOutput> = emptyFlow()
    }

    private class FakeGradleService : GradleService {
        override fun runTask(
            projectRoot: String,
            taskName: String,
            arguments: List<String>
        ): Flow<ProcessOutput> = emptyFlow()

        override fun findAvailableTasks(projectRoot: String): List<String> = emptyList()
    }

    private class FakeCredentialService : CredentialService {
        private val storage = mutableMapOf<String, String>()
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
