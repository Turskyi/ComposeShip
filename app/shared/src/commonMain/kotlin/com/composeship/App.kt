package com.composeship

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.composeship.ui.MainScaffold
import com.composeship.ui.onboarding.OnboardingScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun App(
    appContainer: AppContainer,
    content: @Composable (Modifier) -> Unit
) {
    val mainViewModel = remember { appContainer.createMainViewModel() }
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()

    MainScaffold(viewModel = mainViewModel) { modifier ->
        if (showOnboarding) {
            OnboardingScreen(onDismiss = { mainViewModel.dismissOnboarding() })
        } else {
            content(modifier)
        }
    }
}
