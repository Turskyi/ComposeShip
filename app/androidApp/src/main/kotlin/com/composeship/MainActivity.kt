package com.composeship

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeship.di.AppContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appContainer = AppContainer()

        setContent {
            App(appContainer = appContainer) {
                Text("Android App Content")
            }
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}