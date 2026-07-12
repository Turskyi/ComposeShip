package com.composeship.core.domain

import androidx.compose.runtime.Composable

interface AppLocale {
    fun getLanguageCode(): String
    fun setLanguageCode(code: String?)
    fun hasUserSetLanguage(): Boolean
}

@Composable
expect fun rememberAppLocale(): AppLocale
