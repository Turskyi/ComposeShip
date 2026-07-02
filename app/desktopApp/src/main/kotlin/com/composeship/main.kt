package com.composeship

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.composeship.App
import com.composeship.di.AppContainer
import com.composeship.feature.macosrelease.di.DesktopAppContainer
import com.composeship.feature.macosrelease.ui.MacOsReleaseScreen

fun main() {
    val appContainer = AppContainer()
    val macOsReleaseContainer = DesktopAppContainer()
    val viewModel = macOsReleaseContainer.createMacOsReleaseViewModel()
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ComposeShip - macOS Release Tool",
        ) {
            App(appContainer = appContainer) { modifier ->
                MacOsReleaseScreen(viewModel, modifier)
            }
        }
    }
}

