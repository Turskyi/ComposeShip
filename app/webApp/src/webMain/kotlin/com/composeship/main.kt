package com.composeship

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.composeship.ui.web.LandingPage
import com.composeship.ui.web.PrivacyPage
import com.composeship.ui.web.SupportPage
import kotlinx.browser.window

enum class WebPage {
    Home, Support, Privacy
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appContainer = AppContainer()
    
    ComposeViewport("ComposeShip") {
        var currentPage by remember { 
            val path = window.location.hash.removePrefix("#")
            mutableStateOf(when {
                path.contains("support") -> WebPage.Support
                path.contains("privacy") -> WebPage.Privacy
                else -> WebPage.Home
            })
        }
        
        App(appContainer = appContainer) { _ ->
            when (currentPage) {
                WebPage.Home -> LandingPage(
                    onNavigateToSupport = { 
                        currentPage = WebPage.Support
                        window.location.hash = "support"
                    },
                    onNavigateToPrivacy = { 
                        currentPage = WebPage.Privacy
                        window.location.hash = "privacy"
                    }
                )
                WebPage.Support -> SupportPage(onBack = { 
                    currentPage = WebPage.Home
                    window.location.hash = ""
                })
                WebPage.Privacy -> PrivacyPage(onBack = { 
                    currentPage = WebPage.Home
                    window.location.hash = ""
                })
            }
        }
    }
}
