package com.composeship

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.domain.model.Feature
import com.composeship.core.domain.rememberAppLocale
import com.composeship.ui.MainScaffold
import com.composeship.ui.onboarding.OnboardingScreen
import com.composeship.ui.web.SupportPage

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import com.composeship.core.domain.platform.PlatformType
import com.composeship.core.domain.platform.getPlatform

@Composable
fun App(
    appContainer: AppContainer,
    featureContent: @Composable (Feature, Modifier) -> Unit
) {
    val platform = remember { getPlatform() }
    val initialSupport = remember {
        platform.initialPath?.trim('/')?.equals("support", ignoreCase = true) == true
    }
    val appLocale = rememberAppLocale()
    val mainViewModel = remember(appLocale, initialSupport) { 
        appContainer.createMainViewModel(appLocale, initialSupport) 
    }
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
    val showSupport by mainViewModel.showSupport.collectAsStateWithLifecycle()
    val selectedLanguageCode by mainViewModel.selectedLanguageCode.collectAsStateWithLifecycle()
    val currentFeature by mainViewModel.currentFeature.collectAsStateWithLifecycle()

    LaunchedEffect(showSupport) {
        if (platform.type == PlatformType.WEB) {
            val targetPath = if (showSupport) "/support" else "/"
            platform.updateUrl(targetPath)
        }
    }

    DisposableEffect(platform) {
        if (platform.type == PlatformType.WEB) {
            platform.observePathChanges { path ->
                val trimmed = path.trim('/').lowercase()
                if (trimmed == "support") {
                    mainViewModel.showSupportPage()
                } else {
                    mainViewModel.hideSupportPage()
                }
            }
        }
        onDispose {}
    }

    key(selectedLanguageCode) {
        MainScaffold(viewModel = mainViewModel, showOnboarding = showOnboarding) { modifier ->
            if (showOnboarding) {
                OnboardingScreen(onDismiss = { mainViewModel.dismissOnboarding() })
            } else if (showSupport) {
                SupportPage(modifier)
            } else {
                AnimatedContent(
                    targetState = currentFeature,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally { it / 8 }) togetherWith
                            (fadeOut() + slideOutHorizontally { -it / 8 })
                    },
                    label = "featureTransition"
                ) { feature ->
                    featureContent(feature, modifier)
                }
            }
        }
    }
}
