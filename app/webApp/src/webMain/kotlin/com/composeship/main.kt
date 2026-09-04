package com.composeship

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.composeship.core.data.service.PlaceholderFileSystemService
import com.composeship.core.domain.model.Feature
import com.composeship.feature.cloudrundeploy.data.service.PlaceholderGcloudService
import com.composeship.feature.cloudrundeploy.di.CloudRunDeployContainer
import com.composeship.feature.cloudrundeploy.ui.CloudRunDeployScreen
import kotlinx.browser.window

enum class WebPage {
    Home, Support, Privacy
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val appContainer = AppContainer()

    // Use placeholders for web
    val gcloudService = PlaceholderGcloudService()
    val fileSystemService = PlaceholderFileSystemService()
    val cloudRunContainer =
        CloudRunDeployContainer(gcloudService, fileSystemService)
    val cloudRunViewModel = cloudRunContainer.createViewModel()

    ComposeViewport("ComposeShip") {
        var currentPage by remember {
            val path = window.location.hash.removePrefix("#")
            mutableStateOf(
                when {
                    path.contains("support") -> WebPage.Support
                    path.contains("privacy") -> WebPage.Privacy
                    else -> WebPage.Home
                }
            )
        }

        App(appContainer = appContainer) { feature, modifier ->
            when (feature) {
                Feature.CLOUD_RUN_DEPLOY -> CloudRunDeployScreen(
                    cloudRunViewModel,
                    modifier
                )

                Feature.MACOS_RELEASE -> Text("Not supported on Web", modifier)
            }
        }
    }
}
