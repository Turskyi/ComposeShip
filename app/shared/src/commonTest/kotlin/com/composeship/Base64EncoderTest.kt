package com.composeship

import com.composeship.base64.encodeBase64
import kotlin.test.Test
import kotlin.test.assertEquals

class Base64EncoderTest {
    @Test
    fun encodesStandardPaddedBase64WithoutLineBreaks() {
        assertEquals("", encodeBase64(byteArrayOf()))
        assertEquals("Zg==", encodeBase64("f".encodeToByteArray()))
        assertEquals("Zm8=", encodeBase64("fo".encodeToByteArray()))
        assertEquals("Zm9v", encodeBase64("foo".encodeToByteArray()))
        assertEquals("AAEC/w==", encodeBase64(byteArrayOf(0, 1, 2, -1)))
    }
}
