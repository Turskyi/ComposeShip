package com.composeship.core.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private const val KEY_LANGUAGE = "key_language"
private const val KEY_USER_SET = "key_language_user_set"

private val browserStorage = js("window.localStorage")
private val initialBrowserLanguage: String =
    (js("window.navigator.language") as? String)?.substringBefore('-') ?: "en"
private val navigatorLanguageSetter = js(
    "(code) => { Object.defineProperty(window.navigator, 'language', { get: () => code, configurable: true }); Object.defineProperty(window.navigator, 'languages', { get: () => [code], configurable: true }); }"
)

class JsAppLocale : AppLocale {
    init {
        if (hasUserSetLanguage()) {
            setNavigatorLanguage(getStoredLanguage() ?: initialBrowserLanguage)
        }
        updateDocumentLanguage(getLanguageCode())
    }

    override fun getLanguageCode(): String =
        if (hasUserSetLanguage()) getStoredLanguage() ?: initialBrowserLanguage
        else initialBrowserLanguage

    override fun setLanguageCode(code: String?) {
        if (code == null) {
            browserStorage.removeItem(KEY_LANGUAGE)
            browserStorage.setItem(KEY_USER_SET, "false")
        } else {
            browserStorage.setItem(KEY_LANGUAGE, code)
            browserStorage.setItem(KEY_USER_SET, "true")
        }

        val effectiveLanguage = code ?: initialBrowserLanguage
        setNavigatorLanguage(effectiveLanguage)
        updateDocumentLanguage(effectiveLanguage)
    }

    override fun hasUserSetLanguage(): Boolean = browserStorage.getItem(KEY_USER_SET) == "true"

    private fun getStoredLanguage(): String? = browserStorage.getItem(KEY_LANGUAGE) as? String
}

@Composable
actual fun rememberAppLocale(): AppLocale = remember { JsAppLocale() }

private fun setNavigatorLanguage(code: String) {
    navigatorLanguageSetter(code)
}

private fun updateDocumentLanguage(code: String) {
    js("document.documentElement").setAttribute("lang", code)
}
