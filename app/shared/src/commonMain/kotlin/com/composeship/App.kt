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
import com.composeship.ui.web.LandingPage
import com.composeship.ui.web.PrivacyPage
import com.composeship.ui.web.SupportPage

@Composable
fun App(
    appContainer: AppContainer,
    featureContent: @Composable (feature: Feature, viewModel: MainViewModel, modifier: Modifier) -> Unit
) {
    val platform = remember { getPlatform() }
    val initialRoute = remember {
        AppRoute.parse(
            path = platform.initialPath,
            isMac = platform.isMac,
            isWeb = platform.type == PlatformType.WEB
        )
    }
    val appLocale = rememberAppLocale()
    val mainViewModel = remember(appLocale, initialRoute) {
        appContainer.createMainViewModel(
            appLocale = appLocale,
            initialShowSupport = initialRoute.showSupport,
            initialShowPrivacy = initialRoute.showPrivacy,
            initialShowLanding = initialRoute.showLanding,
            initialFeature = initialRoute.feature,
            initialBase64Tab = initialRoute.base64Tab,
            bypassOnboarding = initialRoute.bypassOnboarding
        )
    }
    val showOnboarding by mainViewModel.showOnboarding.collectAsStateWithLifecycle()
    val showSupport by mainViewModel.showSupport.collectAsStateWithLifecycle()
    val showPrivacy by mainViewModel.showPrivacy.collectAsStateWithLifecycle()
    val showLanding by mainViewModel.showLanding.collectAsStateWithLifecycle()
    val selectedLanguageCode by mainViewModel.selectedLanguageCode.collectAsStateWithLifecycle()
    val currentFeature by mainViewModel.currentFeature.collectAsStateWithLifecycle()
    val base64Tab by mainViewModel.base64Tab.collectAsStateWithLifecycle()

    LaunchedEffect(showSupport, showPrivacy, showLanding, currentFeature, base64Tab) {
        if (platform.type == PlatformType.WEB) {
            val route = AppRoute(
                feature = currentFeature,
                base64Tab = base64Tab,
                showSupport = showSupport,
                showPrivacy = showPrivacy,
                showLanding = showLanding
            )
            platform.updateUrl(route.toPath())
        }
    }

    DisposableEffect(platform) {
        if (platform.type == PlatformType.WEB) {
            platform.observePathChanges { path ->
                val route = AppRoute.parse(path, platform.isMac, platform.type == PlatformType.WEB)
                when {
                    route.showSupport -> mainViewModel.showSupportPage()
                    route.showPrivacy -> mainViewModel.showPrivacyPage()
                    route.showLanding -> mainViewModel.showLandingPage()
                    else -> {
                        mainViewModel.hideOverlayPages()
                        mainViewModel.selectFeature(route.feature)
                        if (route.feature == Feature.FILE_TO_BASE64) {
                            mainViewModel.selectBase64Tab(route.base64Tab)
                        }
                    }
                }
            }
        }
        onDispose {}
    }

    key(selectedLanguageCode) {
        when {
            showSupport -> SupportPage(onHomeClick = { mainViewModel.hideSupportPage() })
            showPrivacy -> PrivacyPage(onHomeClick = { mainViewModel.hidePrivacyPage() })
            showLanding -> LandingPage(
                onEnterApp = { mainViewModel.hideLandingPage() },
                onNavigateToSupport = { mainViewModel.showSupportPage() },
                onNavigateToPrivacy = { mainViewModel.showPrivacyPage() },
                isWeb = platform.type == PlatformType.WEB
            )
            showOnboarding -> OnboardingScreen(onDismiss = { mainViewModel.dismissOnboarding() })
            else -> MainScaffold(viewModel = mainViewModel, showOnboarding = showOnboarding) { modifier ->
                AnimatedContent(
                    targetState = currentFeature,
                    transitionSpec = {
                        (fadeIn() + slideInHorizontally { it / 8 }) togetherWith
                            (fadeOut() + slideOutHorizontally { -it / 8 })
                    },
                    label = "featureTransition"
                ) { feature ->
                    featureContent(feature, mainViewModel, modifier)
                }
            }
        }
    }
}

@Composable
fun App(
    appContainer: AppContainer,
    featureContent: @Composable (feature: Feature, modifier: Modifier) -> Unit
) {
    App(appContainer = appContainer) { feature, _, modifier ->
        featureContent(feature, modifier)
    }
}
