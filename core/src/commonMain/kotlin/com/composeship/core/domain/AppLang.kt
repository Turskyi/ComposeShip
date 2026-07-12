package com.composeship.core.domain

enum class AppLang(val code: String) {
    ENGLISH("en"),
    UKRAINIAN("uk");

    companion object {
        val DEFAULT = ENGLISH

        @Suppress("unused")
        fun fromCode(code: String?): AppLang {
            return entries.find { it.code == code?.lowercase()?.split("-")?.firstOrNull() } ?: DEFAULT
        }
    }
}
