package com.composeship

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.composeship.ui.MainScaffold
import com.composeship.ui.onboarding.OnboardingScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.domain.rememberAppLocale

@Composable
fun App(
    appContainer: AppContainer,
    content: @Composable (Modifier) -> Unit
) {
    val appLocale = rememberAppLocale()
    val mainViewModel = remember(appLocale) { appContainer.createMainViewModel(appLocale) }
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
    val selectedLanguageCode by mainViewModel.selectedLanguageCode.collectAsStateWithLifecycle()

    key(selectedLanguageCode) {
        MainScaffold(viewModel = mainViewModel) { modifier ->
            if (showOnboarding) {
                OnboardingScreen(onDismiss = { mainViewModel.dismissOnboarding() })
            } else {
                content(modifier)
            }
        }
    }
}
