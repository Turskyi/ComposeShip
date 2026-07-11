package com.composeship

import androidx.compose.material3.Text
import androidx.compose.ui.window.ComposeUIViewController

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
    App(appContainer = appContainer) {
        Text("iOS App Content")
    }
}
