package com.composeship.core.theme

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepository(private val settings: Settings) {
    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        settings[KEY_THEME_MODE] = mode.name
        _themeMode.value = mode
    }

    private fun loadThemeMode(): ThemeMode {
        val name = settings.get<String>(KEY_THEME_MODE)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
    }
}
