package com.composeship

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    // Use the real AppContainer (it uses Settings() like the shared AppContainer does).
    val previewContainer = AppContainer()

    App(
        appContainer = previewContainer,
        content = { modifier: Modifier ->
            // Simple preview content shown when onboarding is dismissed
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Preview content")
            }
        }
    )
}

