package com.composeship

import com.composeship.core.domain.model.Feature

data class AppRoute(
    val feature: Feature,
    val base64Tab: Int = 0,
    val showSupport: Boolean = false,
    val showPrivacy: Boolean = false,
    val showLanding: Boolean = false,
    val bypassOnboarding: Boolean = false
) {
    fun toPath(): String {
        return when {
            showSupport -> "/support"
            showPrivacy -> "/privacy"
            showLanding -> "/landing"
            else -> when (feature) {
                Feature.FILE_TO_BASE64 -> if (base64Tab == 1) "/filetobase64" else "/decodebase64"
                Feature.PASSWORD_GENERATOR -> "/password"
                Feature.CLOUD_RUN_DEPLOY -> "/cloudrun"
                Feature.FIREBASE_HOSTING -> "/firebase"
                Feature.WEAR_RELEASE -> "/wear"
                Feature.MACOS_RELEASE -> "/macos"
            }
        }
    }

    companion object {
        fun parse(path: String?, isMac: Boolean, isWeb: Boolean): AppRoute {
            val trimmed = path?.trim('/')?.lowercase() ?: ""
            val defaultFeature = if (isMac) Feature.MACOS_RELEASE else Feature.CLOUD_RUN_DEPLOY
            return when (trimmed) {
                "support" -> AppRoute(feature = defaultFeature, showSupport = true)
                "privacy", "privacypolicy" -> AppRoute(feature = defaultFeature, showPrivacy = true)
                "landing" -> AppRoute(feature = defaultFeature, showLanding = isWeb)
                "decodebase64", "base64totext", "base64/decode", "base64" -> AppRoute(
                    feature = Feature.FILE_TO_BASE64,
                    base64Tab = 0,
                    bypassOnboarding = true
                )
                "filetobase64", "base64/encode" -> AppRoute(
                    feature = Feature.FILE_TO_BASE64,
                    base64Tab = 1,
                    bypassOnboarding = true
                )
                "password", "passwordgenerator" -> AppRoute(feature = Feature.PASSWORD_GENERATOR)
                "cloudrun", "cloudrundeploy" -> AppRoute(feature = Feature.CLOUD_RUN_DEPLOY)
                "firebase", "firebasehosting" -> AppRoute(feature = Feature.FIREBASE_HOSTING)
                "wear", "wearrelease" -> AppRoute(feature = Feature.WEAR_RELEASE)
                "macos", "macosrelease" -> AppRoute(feature = Feature.MACOS_RELEASE)
                else -> AppRoute(feature = defaultFeature)
            }
        }
    }
}
