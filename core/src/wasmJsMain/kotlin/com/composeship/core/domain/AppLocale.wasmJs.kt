package com.composeship.core.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private const val KEY_LANGUAGE = "key_language"
private const val KEY_USER_SET = "key_language_user_set"

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key) => window.localStorage.getItem(key)")
private external fun getStoredValue(key: String): String?

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun setStoredValue(key: String, value: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(key) => window.localStorage.removeItem(key)")
private external fun removeStoredValue(key: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => window.navigator.language")
private external fun getBrowserLanguage(): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(code) => { Object.defineProperty(window.navigator, 'language', { get: () => code, configurable: true }); Object.defineProperty(window.navigator, 'languages', { get: () => [code], configurable: true }); }")
private external fun setNavigatorLanguage(code: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(code) => document.documentElement.setAttribute('lang', code)")
private external fun updateDocumentLanguage(code: String)

class WasmAppLocale : AppLocale {
    private val initialBrowserLanguage = getBrowserLanguage().substringBefore('-')

    init {
        if (hasUserSetLanguage()) {
            setNavigatorLanguage(getStoredValue(KEY_LANGUAGE) ?: initialBrowserLanguage)
        }
        updateDocumentLanguage(getLanguageCode())
    }

    override fun getLanguageCode(): String =
        if (hasUserSetLanguage()) getStoredValue(KEY_LANGUAGE) ?: initialBrowserLanguage
        else initialBrowserLanguage

    override fun setLanguageCode(code: String?) {
        if (code == null) {
            removeStoredValue(KEY_LANGUAGE)
            setStoredValue(KEY_USER_SET, "false")
        } else {
            setStoredValue(KEY_LANGUAGE, code)
            setStoredValue(KEY_USER_SET, "true")
        }

        val effectiveLanguage = code ?: initialBrowserLanguage
        setNavigatorLanguage(effectiveLanguage)
        updateDocumentLanguage(effectiveLanguage)
    }

    override fun hasUserSetLanguage(): Boolean = getStoredValue(KEY_USER_SET) == "true"
}

@Composable
actual fun rememberAppLocale(): AppLocale = remember { WasmAppLocale() }
