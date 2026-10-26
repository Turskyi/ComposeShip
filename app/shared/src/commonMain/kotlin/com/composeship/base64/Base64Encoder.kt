package com.composeship.base64

private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
const val MAX_BASE64_FILE_SIZE_BYTES = 50L * 1024L * 1024L

fun encodeBase64(bytes: ByteArray): String {
    val output = StringBuilder(((bytes.size + 2) / 3) * 4)
    var index = 0
    while (index < bytes.size) {
        val first = bytes[index++].toInt() and 0xff
        val hasSecond = index < bytes.size
        val second = if (hasSecond) bytes[index++].toInt() and 0xff else 0
        val hasThird = index < bytes.size
        val third = if (hasThird) bytes[index++].toInt() and 0xff else 0

        output.append(BASE64_ALPHABET[first shr 2])
        output.append(BASE64_ALPHABET[((first and 0x03) shl 4) or (second shr 4)])
        output.append(if (hasSecond) BASE64_ALPHABET[((second and 0x0f) shl 2) or (third shr 6)] else '=')
        output.append(if (hasThird) BASE64_ALPHABET[third and 0x3f] else '=')
    }
    return output.toString()
}

fun localFileFromBytes(name: String, bytes: ByteArray): LocalFile {
    val sizeBytes = bytes.size.toLong()
    return LocalFile(
        name = name,
        base64 = if (sizeBytes > MAX_BASE64_FILE_SIZE_BYTES) "" else encodeBase64(bytes),
        sizeBytes = sizeBytes,
    )
}

data class LocalFile(
    val name: String,
    val base64: String,
    val sizeBytes: Long,
)
