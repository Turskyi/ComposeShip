package com.composeship

import androidx.compose.material3.Text
import androidx.compose.ui.window.ComposeUIViewController
import com.composeship.core.data.service.PlaceholderFileSystemService
import com.composeship.core.domain.model.Feature
import com.composeship.feature.cloudrundeploy.data.service.PlaceholderGcloudService
import com.composeship.feature.cloudrundeploy.di.CloudRunDeployContainer
import com.composeship.feature.cloudrundeploy.ui.CloudRunDeployScreen

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

    // Use placeholders for mobile
    val gcloudService = PlaceholderGcloudService()
    val fileSystemService = PlaceholderFileSystemService()
    val cloudRunContainer = CloudRunDeployContainer(gcloudService, fileSystemService)
    val cloudRunViewModel = cloudRunContainer.createViewModel()

    App(appContainer = appContainer) { feature, modifier ->
        when (feature) {
            Feature.CLOUD_RUN_DEPLOY -> CloudRunDeployScreen(cloudRunViewModel, modifier)
            Feature.MACOS_RELEASE -> Text("Not supported on iOS", modifier)
            else -> Text("Not supported on iOS", modifier)
        }
    }
}
