package com.composeship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.composeship.core.theme.ThemeMode
import com.composeship.core.theme.ThemeRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val themeRepository: ThemeRepository) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }
}
