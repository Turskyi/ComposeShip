package com.composeship.core.domain

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import java.util.Locale

private const val KEY_LANGUAGE = "key_language"
private const val KEY_USER_SET = "key_language_user_set"

class AndroidAppLocale(private val settings: Settings = Settings()) : AppLocale {
    init {
        if (hasUserSetLanguage()) {
            settings.get<String>(KEY_LANGUAGE)?.let { languageCode ->
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(languageCode)
                )
            }
        }
    }

    override fun getLanguageCode(): String {
        if (hasUserSetLanguage()) {
            settings.get<String>(KEY_LANGUAGE)?.let { return it }
        }
        return Locale.getDefault().language
    }

    override fun setLanguageCode(code: String?) {
        if (code == null) {
            settings.remove(KEY_LANGUAGE)
            settings.putBoolean(KEY_USER_SET, false)
        } else {
            settings[KEY_LANGUAGE] = code
            settings.putBoolean(KEY_USER_SET, true)
        }

        AppCompatDelegate.setApplicationLocales(
            code?.let(LocaleListCompat::forLanguageTags)
                ?: LocaleListCompat.getEmptyLocaleList()
        )
    }

    override fun hasUserSetLanguage(): Boolean = settings.getBoolean(KEY_USER_SET, false)
}

@Composable
actual fun rememberAppLocale(): AppLocale = remember { AndroidAppLocale() }
