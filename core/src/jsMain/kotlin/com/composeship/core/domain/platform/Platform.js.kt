package com.composeship.core.domain.platform

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
    override val type: PlatformType = PlatformType.WEB
    override val isMac: Boolean = false
}

actual fun getPlatform(): Platform = JsPlatform()
