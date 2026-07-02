package com.composeship

import androidx.compose.material3.Text
import androidx.compose.ui.window.ComposeUIViewController
import com.composeship.di.AppContainer

fun MainViewController() = ComposeUIViewController { 
    val appContainer = AppContainer()
    App(appContainer = appContainer) {
        Text("iOS App Content")
    }
}
