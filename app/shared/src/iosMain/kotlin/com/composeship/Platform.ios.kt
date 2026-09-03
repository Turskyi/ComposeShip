package com.composeship

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val type: PlatformType = PlatformType.IOS
    override val isMac: Boolean = false
}

actual fun getPlatform(): Platform = IOSPlatform()