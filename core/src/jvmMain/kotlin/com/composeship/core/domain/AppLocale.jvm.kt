package com.composeship.core.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import java.util.Locale

class JvmAppLocale(private val settings: Settings) : AppLocale {
    private val KEY_LANGUAGE = "key_language"
    private val KEY_USER_SET = "key_language_user_set"

    init {
        val userSet = settings.getBoolean(KEY_USER_SET, false)
        if (userSet) {
            val stored = settings.get<String>(KEY_LANGUAGE)
            if (!stored.isNullOrBlank()) {
                Locale.setDefault(Locale.forLanguageTag(stored))
            }
        }
    }

    override fun getLanguageCode(): String {
        val userSet = settings.getBoolean(KEY_USER_SET, false)
        if (userSet) {
            val stored = settings.get<String>(KEY_LANGUAGE)
            if (!stored.isNullOrBlank()) {
                return stored.split("-").first()
            }
        }
        return Locale.getDefault().language.split("-").first()
    }

    override fun setLanguageCode(code: String?) {
        if (code == null) {
            settings.remove(KEY_LANGUAGE)
            settings.putBoolean(KEY_USER_SET, false)
            Locale.setDefault(systemLocale)
        } else {
            settings[KEY_LANGUAGE] = code
            settings.putBoolean(KEY_USER_SET, true)
            Locale.setDefault(Locale.forLanguageTag(code))
        }
    }

    override fun hasUserSetLanguage(): Boolean {
        return settings.getBoolean(KEY_USER_SET, false)
    }
}

@Suppress("ConstantLocale")
private val systemLocale = Locale.getDefault()

@Composable
actual fun rememberAppLocale(): AppLocale {
    val settings = remember { Settings() }
    return remember(settings) { JvmAppLocale(settings) }
}
