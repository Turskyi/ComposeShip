package com.composeship

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.domain.model.Feature
import com.composeship.core.domain.platform.PlatformType
import com.composeship.core.domain.platform.getPlatform
import com.composeship.core.domain.rememberAppLocale
import com.composeship.ui.MainScaffold
import com.composeship.ui.onboarding.OnboardingScreen
import com.composeship.ui.web.PrivacyPage
import com.composeship.ui.web.SupportPage

@Composable
fun App(
    appContainer: AppContainer,
    featureContent: @Composable (Feature, Modifier) -> Unit
) {
    val platform = remember { getPlatform() }
    val initialPath = remember { platform.initialPath?.trim('/')?.lowercase() }
    val initialSupport = remember { initialPath == "support" }
    val initialPrivacy = remember { initialPath == "privacy" || initialPath == "privacypolicy" }
    val appLocale = rememberAppLocale()
    val mainViewModel = remember(appLocale, initialSupport, initialPrivacy) {
        appContainer.createMainViewModel(appLocale, initialSupport, initialPrivacy)
    }
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
    val showSupport by mainViewModel.showSupport.collectAsStateWithLifecycle()
    val showPrivacy by mainViewModel.showPrivacy.collectAsStateWithLifecycle()
    val selectedLanguageCode by mainViewModel.selectedLanguageCode.collectAsStateWithLifecycle()
    val currentFeature by mainViewModel.currentFeature.collectAsStateWithLifecycle()

    LaunchedEffect(showSupport, showPrivacy) {
        if (platform.type == PlatformType.WEB) {
            val targetPath = when {
                showSupport -> "/support"
                showPrivacy -> "/privacy"
                else -> "/"
            }
            platform.updateUrl(targetPath)
        }
    }

    DisposableEffect(platform) {
        if (platform.type == PlatformType.WEB) {
            platform.observePathChanges { path ->
                val trimmed = path.trim('/').lowercase()
                when (trimmed) {
                    "support" -> mainViewModel.showSupportPage()
                    "privacy", "privacypolicy" -> mainViewModel.showPrivacyPage()
                    else -> mainViewModel.hideOverlayPages()
                }
            }
        }
        onDispose {}
    }

    key(selectedLanguageCode) {
        MainScaffold(viewModel = mainViewModel, showOnboarding = showOnboarding) { modifier ->
            if (showSupport) {
                SupportPage(modifier)
            } else if (showPrivacy) {
                PrivacyPage(modifier)
            } else if (showOnboarding) {
                OnboardingScreen(onDismiss = { mainViewModel.dismissOnboarding() })
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
