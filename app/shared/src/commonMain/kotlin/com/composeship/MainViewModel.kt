package com.composeship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.domain.SettingsRepository
import com.composeship.core.theme.ThemeMode
import com.composeship.core.theme.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode

    private val _showOnboarding = MutableStateFlow(settingsRepository.shouldShowOnboarding())
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }

    fun dismissOnboarding() {
        settingsRepository.setOnboardingShown()
        _showOnboarding.value = false
    }
}
