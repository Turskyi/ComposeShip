package com.composeship.password

internal actual fun secureRandomInt(bound: Int): Int {
    val range = 1L shl 31
    val limit = range - range % bound
    while (true) {
        val value = js("globalThis.crypto.getRandomValues(new Uint32Array(1))[0] >>> 1") as Int
        if (value.toLong() < limit) return value % bound
    }
}
