package com.composeship.core.domain.platform

interface Platform {
    val name: String
    val type: PlatformType
    val isMac: Boolean
}

enum class PlatformType {
    ANDROID, IOS, DESKTOP, WEB
}

expect fun getPlatform(): Platform
