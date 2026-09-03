package com.composeship

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.domain.model.Feature
import com.composeship.core.domain.rememberAppLocale
import com.composeship.ui.MainScaffold
import com.composeship.ui.onboarding.OnboardingScreen

@Composable
fun App(
    appContainer: AppContainer,
    featureContent: @Composable (Feature, Modifier) -> Unit
) {
    val appLocale = rememberAppLocale()
    val mainViewModel = remember(appLocale) { appContainer.createMainViewModel(appLocale) }
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
    val selectedLanguageCode by mainViewModel.selectedLanguageCode.collectAsStateWithLifecycle()
    val currentFeature by mainViewModel.currentFeature.collectAsStateWithLifecycle()

    key(selectedLanguageCode) {
        MainScaffold(viewModel = mainViewModel) { modifier ->
            if (showOnboarding) {
                OnboardingScreen(onDismiss = { mainViewModel.dismissOnboarding() })
            } else {
                featureContent(currentFeature, modifier)
            }
        }
    }
}
