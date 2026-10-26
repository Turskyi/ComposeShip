import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleGmsGoogleServices)
}

val keyPropertiesFile: File = rootProject.file(
    "app/androidApp/src/main/key.properties",
)

val keyProperties: Properties = Properties()

if (keyPropertiesFile.exists()) {
    keyProperties.load(keyPropertiesFile.inputStream())
} else {
    logger.warn(
        "key.properties file is missing. Android signing will not work.",
    )
}

// Debug environment variables.
val signingKeyDebugPath: String = keyProperties.getProperty(
    "dev.SIGNING_KEY_DEBUG_PATH",
    "",
)

val signingKeyDebugPassword: String = keyProperties.getProperty(
    "dev.SIGNING_KEY_DEBUG_PASSWORD",
    "",
)

val signingKeyDebugKey: String = keyProperties.getProperty(
    "dev.SIGNING_KEY_DEBUG_KEY",
    "",
)

val signingKeyDebugKeyPassword: String = keyProperties.getProperty(
    "dev.SIGNING_KEY_DEBUG_KEY_PASSWORD",
    "",
)

// Release environment variables.
val signingKeyReleasePath: String = keyProperties.getProperty(
    "production.SIGNING_KEY_RELEASE_PATH",
    "",
)

val signingKeyReleasePassword: String = keyProperties.getProperty(
    "production.SIGNING_KEY_RELEASE_PASSWORD",
    "",
)

val signingKeyReleaseKey: String = keyProperties.getProperty(
    "production.SIGNING_KEY_RELEASE_KEY",
    "",
)

val signingKeyReleaseKeyPassword: String = keyProperties.getProperty(
    "production.SIGNING_KEY_RELEASE_KEY_PASSWORD",
    "",
)

fun resolveSigningKey(path: String): File {
    val moduleRelativeFile = file(path)
    return if (moduleRelativeFile.exists()) {
        moduleRelativeFile
    } else {
        rootProject.file("app").resolve(path).normalize()
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(libs.firebase.analytics)
    implementation(projects.app.shared)
    implementation(projects.featureCloudRunDeploy)

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = libs.versions.applicationId.get()
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = libs.versions.applicationId.get()
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("dev") {
            val cmBuildId = System.getenv("FCI_BUILD_ID")
            val cmKeystorePath = System.getenv("CM_KEYSTORE_PATH")
            val cmKeystorePassword = System.getenv("CM_KEYSTORE_PASSWORD")
            val cmKeyAlias = System.getenv("CM_KEY_ALIAS")
            val cmKeyPassword = System.getenv("CM_KEY_PASSWORD")

            if (!cmBuildId.isNullOrBlank() && !cmKeystorePath.isNullOrBlank()) {
                storeFile = file(cmKeystorePath)
                storePassword = cmKeystorePassword
                keyAlias = cmKeyAlias
                keyPassword = cmKeyPassword
            } else if (signingKeyDebugPath.isNotEmpty()) {
                storeFile = resolveSigningKey(signingKeyDebugPath)
                storePassword = signingKeyDebugPassword
                keyAlias = signingKeyDebugKey
                keyPassword = signingKeyDebugKeyPassword
            } else {
                logger.warn(
                    "Android debug signing keys are not configured; " +
                            "skipping dev signing.",
                )
            }
        }

        create("production") {
            val cmBuildId = System.getenv("FCI_BUILD_ID")
            val cmKeystorePath = System.getenv("CM_KEYSTORE_PATH")
            val cmKeystorePassword = System.getenv("CM_KEYSTORE_PASSWORD")
            val cmKeyAlias = System.getenv("CM_KEY_ALIAS")
            val cmKeyPassword = System.getenv("CM_KEY_PASSWORD")

            if (!cmBuildId.isNullOrBlank() && !cmKeystorePath.isNullOrBlank()) {
                storeFile = file(cmKeystorePath)
                storePassword = cmKeystorePassword
                keyAlias = cmKeyAlias
                keyPassword = cmKeyPassword
            } else if (signingKeyReleasePath.isNotEmpty()) {
                storeFile = resolveSigningKey(signingKeyReleasePath)
                storePassword = signingKeyReleasePassword
                keyAlias = signingKeyReleaseKey
                keyPassword = signingKeyReleaseKeyPassword
            } else {
                logger.warn("Android release signing keys are not configured; skipping production signing.")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            val hasDevSigning =
                signingConfigs.getByName("dev").storeFile != null
            if (hasDevSigning) {
                signingConfig = signingConfigs.getByName("dev")
            }
        }
        getByName("release") {
            isMinifyEnabled = false
            val hasProductionSigning =
                signingConfigs.getByName("production").storeFile != null
            if (hasProductionSigning) {
                signingConfig = signingConfigs.getByName("production")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}