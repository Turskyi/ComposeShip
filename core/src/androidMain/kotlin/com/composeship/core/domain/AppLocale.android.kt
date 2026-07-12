package com.composeship.core.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class AndroidAppLocale : AppLocale {
    override fun getLanguageCode(): String = "en"
    override fun setLanguageCode(code: String?) {}
    override fun hasUserSetLanguage(): Boolean = false
}

@Composable
actual fun rememberAppLocale(): AppLocale = remember { AndroidAppLocale() }
