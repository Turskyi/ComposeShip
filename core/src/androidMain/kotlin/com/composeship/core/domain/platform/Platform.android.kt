package com.composeship.core.domain.platform

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val type: PlatformType = PlatformType.ANDROID
    override val isMac: Boolean = false
}

actual fun getPlatform(): Platform = AndroidPlatform()
