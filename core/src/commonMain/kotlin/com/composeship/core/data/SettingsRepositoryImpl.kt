package com.composeship.core.data

import com.composeship.core.domain.SettingsRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {
    override fun shouldShowOnboarding(): Boolean {
        return settings.getBoolean(KEY_ONBOARDING_SHOWN, true)
    }

    override fun setOnboardingShown() {
        settings[KEY_ONBOARDING_SHOWN] = false
    }

    companion object {
        private const val KEY_ONBOARDING_SHOWN = "key_onboarding_shown"
    }
}
