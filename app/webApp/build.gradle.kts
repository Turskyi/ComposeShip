import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                // ensure webpack-dev-server opens Chrome by name
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    open = mapOf("app" to mapOf("name" to "google chrome"))
                }
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                // mirror MalaKnyzhka: open Chrome explicitly when server is ready
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    open = mapOf("app" to mapOf("name" to "google chrome"))
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.shared)
            implementation(projects.featureCloudRunDeploy)
            implementation(projects.featureFirebaseHostingDeploy)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
    }
}
