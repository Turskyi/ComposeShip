package com.composeship

import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.composeship.di.AppContainer

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appContainer = AppContainer()
    ComposeViewport("ComposeShip") {
        App(appContainer = appContainer) {
            Text("Web App Content")
        }
    }
}
