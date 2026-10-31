package com.composeship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.AppLocale
import com.composeship.core.domain.SettingsRepository
import com.composeship.core.domain.model.Feature
import com.composeship.core.domain.platform.getPlatform
import com.composeship.core.theme.ThemeMode
import com.composeship.core.theme.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val appLocale: AppLocale,
    initialShowSupport: Boolean = false,
    initialShowPrivacy: Boolean = false,
    initialShowLanding: Boolean = false,
    initialFeature: Feature? = null,
    initialBase64Tab: Int = 0,
    bypassOnboarding: Boolean = false
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode
    
    private val _languageCode = MutableStateFlow(appLocale.getLanguageCode())

    private val _selectedLanguageCode = MutableStateFlow(if (appLocale.hasUserSetLanguage()) appLocale.getLanguageCode() else null)
    val selectedLanguageCode: StateFlow<String?> = _selectedLanguageCode.asStateFlow()

    private val _showOnboarding = MutableStateFlow(
        if (bypassOnboarding) {
            settingsRepository.setOnboardingShown()
            false
        } else {
            settingsRepository.shouldShowOnboarding()
        }
    )
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    private val _showSupport = MutableStateFlow(initialShowSupport)
    val showSupport: StateFlow<Boolean> = _showSupport.asStateFlow()

    private val _showPrivacy = MutableStateFlow(initialShowPrivacy)
    val showPrivacy: StateFlow<Boolean> = _showPrivacy.asStateFlow()

    private val _showLanding = MutableStateFlow(initialShowLanding)
    val showLanding: StateFlow<Boolean> = _showLanding.asStateFlow()

    private val _currentFeature = MutableStateFlow(
        initialFeature ?: if (getPlatform().isMac) Feature.MACOS_RELEASE else Feature.CLOUD_RUN_DEPLOY
    )
    val currentFeature: StateFlow<Feature> = _currentFeature.asStateFlow()

    private val _base64Tab = MutableStateFlow(initialBase64Tab)
    val base64Tab: StateFlow<Int> = _base64Tab.asStateFlow()

    fun selectBase64Tab(tabIndex: Int) {
        _base64Tab.value = tabIndex
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(code: String?) {
        appLocale.setLanguageCode(code)
        _selectedLanguageCode.value = code
        _languageCode.value = appLocale.getLanguageCode()
    }

    fun dismissOnboarding() {
        settingsRepository.setOnboardingShown()
        _showOnboarding.value = false
    }

    fun showSupportPage() {
        _showLanding.value = false
        _showPrivacy.value = false
        _showSupport.value = true
    }

    fun hideSupportPage() {
        _showSupport.value = false
    }

    fun showPrivacyPage() {
        _showLanding.value = false
        _showSupport.value = false
        _showPrivacy.value = true
    }

    fun hidePrivacyPage() {
        _showPrivacy.value = false
    }

    fun showLandingPage() {
        _showSupport.value = false
        _showPrivacy.value = false
        _showLanding.value = true
    }

    fun hideLandingPage() {
        _showLanding.value = false
    }

    fun hideOverlayPages() {
        _showSupport.value = false
        _showPrivacy.value = false
        _showLanding.value = false
    }

    fun toggleSupport() {
        _showLanding.value = false
        _showPrivacy.value = false
        _showSupport.value = !_showSupport.value
    }

    fun togglePrivacy() {
        _showLanding.value = false
        _showSupport.value = false
        _showPrivacy.value = !_showPrivacy.value
    }

    fun selectFeature(feature: Feature) {
        _showLanding.value = false
        _showSupport.value = false
        _showPrivacy.value = false
        _currentFeature.value = feature
    }
}
