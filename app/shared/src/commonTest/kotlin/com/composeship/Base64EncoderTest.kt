package com.composeship

import com.composeship.base64.decodeBase64
import com.composeship.base64.encodeBase64
import com.composeship.base64.formatJsonIfValid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Base64EncoderTest {
    @Test
    fun encodesStandardPaddedBase64WithoutLineBreaks() {
        assertEquals("", encodeBase64(byteArrayOf()))
        assertEquals("Zg==", encodeBase64("f".encodeToByteArray()))
        assertEquals("Zm8=", encodeBase64("fo".encodeToByteArray()))
        assertEquals("Zm9v", encodeBase64("foo".encodeToByteArray()))
        assertEquals("AAEC/w==", encodeBase64(byteArrayOf(0, 1, 2, -1)))
    }

    @Test
    fun decodesBase64Successfully() {
        val decoded = decodeBase64("Zm9v")
        assertTrue(decoded.isSuccess)
        assertEquals("foo", decoded.getOrNull())
    }

    @Test
    fun formatsValidJson() {
        val json = "{\"key\":\"value\"}"
        val formatted = formatJsonIfValid(json)
        assertTrue(formatted != null)
        assertTrue(formatted.contains("\"key\": \"value\""))
    }
}
