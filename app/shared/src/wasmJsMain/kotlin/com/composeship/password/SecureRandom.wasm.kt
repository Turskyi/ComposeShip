package com.composeship.password

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => globalThis.crypto.getRandomValues(new Uint32Array(1))[0] >>> 1")
private external fun secureRandomValue(): Int

internal actual fun secureRandomInt(bound: Int): Int {
    val limit = Int.MAX_VALUE - (Int.MAX_VALUE % bound)
    while (true) {
        val value = secureRandomValue()
        if (value < limit) return value % bound
    }
}
