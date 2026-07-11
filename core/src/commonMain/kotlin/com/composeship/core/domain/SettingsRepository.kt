package com.composeship.core.domain

interface SettingsRepository {
    fun shouldShowOnboarding(): Boolean
    fun setOnboardingShown()
}
