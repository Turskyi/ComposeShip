import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.featureMacosRelease)
    implementation(projects.featureCloudRunDeploy)
    implementation(projects.featureFirebaseHostingDeploy)
    implementation(projects.featureWearRelease)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.composeship.MainKt"

        buildTypes {
            release {
                proguard {
                    configurationFiles.from(
                        project.file("proguard-desktop-rules.pro"),
                    )
                }
            }
        }

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Pkg,
                TargetFormat.Msi,
                TargetFormat.Deb,
            )
            packageName = libs.versions.dockName.get()
            packageVersion = libs.versions.versionName.get()

            macOS {
                iconFile.set(
                    project.file(
                        "src/main/resources/icon.icns",
                    )
                )
                bundleID = libs.versions.applicationId.get()
                dockName = libs.versions.dockName.get()
                appStore = false
                infoPlist {
                    extraKeysRawXml =
                        "<key>ITSAppUsesNonExemptEncryption</key><false/>" +
                                "<key>LSApplicationCategoryType</key>" +
                                "<string>public.app-category.developer-tools</string>"
                }
            }
        }
    }
}