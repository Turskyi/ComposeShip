package com.composeship

import com.composeship.core.data.SettingsRepositoryImpl
import com.composeship.core.domain.SettingsRepository
import com.composeship.core.theme.ThemeRepository
import com.russhwolf.settings.Settings

class AppContainer {
    private val settings: Settings = Settings()
    val themeRepository: ThemeRepository = ThemeRepository(settings)
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(settings)
    
    fun createMainViewModel(): MainViewModel {
        return MainViewModel(themeRepository, settingsRepository)
    }
}
