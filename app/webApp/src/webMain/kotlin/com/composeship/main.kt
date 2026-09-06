package com.composeship

import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.composeship.core.data.service.PlaceholderFileSystemService
import com.composeship.core.domain.model.Feature
import com.composeship.feature.cloudrundeploy.data.service.PlaceholderGcloudService
import com.composeship.feature.cloudrundeploy.di.CloudRunDeployContainer
import com.composeship.feature.cloudrundeploy.ui.CloudRunDeployScreen
import com.composeship.feature.firebasedeploy.data.service.PlaceholderFirebaseService
import com.composeship.feature.firebasedeploy.di.FirebaseDeployContainer
import com.composeship.feature.firebasedeploy.ui.FirebaseDeployScreen
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.not_supported_on_web
import org.jetbrains.compose.resources.stringResource

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
    val firebaseService = PlaceholderFirebaseService()
    val firebaseDeployContainer = FirebaseDeployContainer(firebaseService, fileSystemService)
    val firebaseViewModel = firebaseDeployContainer.createViewModel()

    ComposeViewport("ComposeShip") {
        App(appContainer = appContainer) { feature, modifier ->
            when (feature) {
                Feature.CLOUD_RUN_DEPLOY -> CloudRunDeployScreen(
                    cloudRunViewModel,
                    modifier
                )

                Feature.MACOS_RELEASE -> Text(
                    stringResource(Res.string.not_supported_on_web),
                    modifier
                )

                Feature.FIREBASE_HOSTING -> FirebaseDeployScreen(
                    firebaseViewModel,
                    modifier
                )
                
            }
        }
    }
}
