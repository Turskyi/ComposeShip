package com.composeship

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.composeship.core.domain.model.Feature
import com.composeship.feature.cloudrundeploy.di.CloudRunDeployContainer
import com.composeship.feature.cloudrundeploy.ui.CloudRunDeployScreen
import com.composeship.feature.macosrelease.data.service.DesktopFileSystemService
import com.composeship.feature.macosrelease.data.service.DesktopProcessService
import com.composeship.feature.macosrelease.di.DesktopAppContainer
import com.composeship.feature.macosrelease.ui.MacOsReleaseScreen

fun main() {
    val appContainer = AppContainer()

    // Shared services
    val processService = DesktopProcessService()
    val fileSystemService = DesktopFileSystemService()

    val macOsReleaseContainer = DesktopAppContainer(processService, fileSystemService)
    val macOsViewModel = macOsReleaseContainer.createMacOsReleaseViewModel()

    val cloudRunDeployContainer = CloudRunDeployContainer(processService, fileSystemService)
    val cloudRunViewModel = cloudRunDeployContainer.createViewModel()
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ComposeShip",
        ) {
            App(appContainer = appContainer) { feature, modifier ->
                when (feature) {
                    Feature.MACOS_RELEASE -> MacOsReleaseScreen(macOsViewModel, modifier)
                    Feature.CLOUD_RUN_DEPLOY -> CloudRunDeployScreen(cloudRunViewModel, modifier)
                }
            }
        }
    }
}
