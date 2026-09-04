package com.composeship.core.domain.platform

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val type: PlatformType = PlatformType.DESKTOP
    override val isMac: Boolean = System.getProperty("os.name").contains("Mac", ignoreCase = true)
}

actual fun getPlatform(): Platform = JVMPlatform()
