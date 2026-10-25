package com.composeship.password

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.Security.errSecSuccess

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal actual fun secureRandomInt(bound: Int): Int {
    val range = 1L shl 32
    val limit = range - range % bound
    while (true) {
        val bytes = ByteArray(4)
        val status = bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, bytes.size.toULong(), pinned.addressOf(0))
        }
        check(status == errSecSuccess) { "Secure random number generation failed." }
        val value = bytes.foldIndexed(0L) { index, result, byte ->
            result or ((byte.toLong() and 0xff) shl (index * 8))
        }
        if (value < limit) return (value % bound).toInt()
    }
}
