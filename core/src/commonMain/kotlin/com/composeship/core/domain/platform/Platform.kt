package com.composeship.core.domain.platform

interface Platform {
    val name: String
    val type: PlatformType
    val isMac: Boolean
    val initialPath: String? get() = null
    fun updateUrl(path: String) {}
    fun observePathChanges(onPathChanged: (String) -> Unit) {}
}

enum class PlatformType {
    ANDROID, IOS, DESKTOP, WEB
}

expect fun getPlatform(): Platform
