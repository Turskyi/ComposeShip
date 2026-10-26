package com.composeship

import androidx.compose.material3.Text
import androidx.compose.ui.window.ComposeUIViewController
import com.composeship.core.domain.model.Feature
import com.composeship.ui.password.PasswordGeneratorScreen
import com.composeship.ui.base64.Base64EncoderScreen

/**
 * Entry point for the iOS application. 
 * This function is called from Swift code (typically in `iOSApp.swift`) 
 * to initialize the Compose Multiplatform UI.
 * 
 * Note: This function may appear unused in the Kotlin IDE because its 
 * usage is in the Swift/native part of the project.
 */
@Suppress("FunctionName")
fun MainViewController() = ComposeUIViewController {
    val appContainer = AppContainer()

    App(appContainer = appContainer) { feature, modifier ->
        when (feature) {
            Feature.PASSWORD_GENERATOR -> PasswordGeneratorScreen(modifier)
            Feature.FILE_TO_BASE64 -> Base64EncoderScreen(modifier)
            Feature.MACOS_RELEASE -> Text("Not supported on iOS", modifier)
            else -> Text("Not supported on iOS", modifier)
        }
    }
}
