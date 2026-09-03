package com.composeship

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val type: PlatformType = PlatformType.WEB
    override val isMac: Boolean = false
}

actual fun getPlatform(): Platform = WasmPlatform()