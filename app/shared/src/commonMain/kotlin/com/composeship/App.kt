package com.composeship

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composeship.di.AppContainer
import com.composeship.ui.MainScaffold
import org.jetbrains.compose.resources.painterResource

@Composable
@Preview
fun App(
    appContainer: AppContainer,
    content: @Composable (Modifier) -> Unit
) {
    val mainViewModel = remember { appContainer.createMainViewModel() }
    
    MainScaffold(viewModel = mainViewModel) { modifier ->
        content(modifier)
    }
}
