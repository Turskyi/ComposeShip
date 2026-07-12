package com.composeship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.AppLocale
import com.composeship.core.domain.SettingsRepository
import com.composeship.core.theme.ThemeMode
import com.composeship.core.theme.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val appLocale: AppLocale
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode
    
    private val _languageCode = MutableStateFlow(appLocale.getLanguageCode())

    private val _selectedLanguageCode = MutableStateFlow(if (appLocale.hasUserSetLanguage()) appLocale.getLanguageCode() else null)
    val selectedLanguageCode: StateFlow<String?> = _selectedLanguageCode.asStateFlow()

    private val _showOnboarding = MutableStateFlow(settingsRepository.shouldShowOnboarding())
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

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
}
