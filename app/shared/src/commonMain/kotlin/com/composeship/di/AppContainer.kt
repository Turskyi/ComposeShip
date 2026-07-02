package com.composeship.di

import com.composeship.MainViewModel
import com.composeship.core.theme.ThemeRepository
import com.russhwolf.settings.Settings

class AppContainer {
    private val settings: Settings = Settings()
    val themeRepository: ThemeRepository = ThemeRepository(settings)
    
    fun createMainViewModel(): MainViewModel {
        return MainViewModel(themeRepository)
    }
}
