package com.composeship

interface Platform {
    val name: String
    val type: PlatformType
    val isMac: Boolean
}

enum class PlatformType {
    ANDROID, IOS, DESKTOP, WEB
}

expect fun getPlatform(): Platform