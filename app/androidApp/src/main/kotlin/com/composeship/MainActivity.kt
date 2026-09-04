package com.composeship

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeship.core.data.service.PlaceholderFileSystemService
import com.composeship.core.domain.model.Feature
import com.composeship.feature.cloudrundeploy.data.service.PlaceholderGcloudService
import com.composeship.feature.cloudrundeploy.di.CloudRunDeployContainer
import com.composeship.feature.cloudrundeploy.ui.CloudRunDeployScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appContainer = AppContainer()

        // Use placeholders for mobile
        val gcloudService = PlaceholderGcloudService()
        val fileSystemService = PlaceholderFileSystemService()
        val cloudRunContainer = CloudRunDeployContainer(gcloudService, fileSystemService)
        val cloudRunViewModel = cloudRunContainer.createViewModel()

        setContent {
            App(appContainer = appContainer) { feature, modifier ->
                when (feature) {
                    Feature.CLOUD_RUN_DEPLOY -> CloudRunDeployScreen(cloudRunViewModel, modifier)
                    Feature.MACOS_RELEASE -> Text("Not supported on Android", modifier)
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    // Basic preview stub
}
