package com.composeship

import com.composeship.core.domain.model.Feature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppRouteTest {

    @Test
    fun testParseBase64DecodeRoutes() {
        val paths = listOf("decodebase64", "base64totext", "base64/decode", "base64", "/decodebase64")
        for (path in paths) {
            val route = AppRoute.parse(path, isMac = false, isWeb = true)
            assertEquals(Feature.FILE_TO_BASE64, route.feature, "Expected FILE_TO_BASE64 for path: $path")
            assertEquals(0, route.base64Tab, "Expected tab 0 (Decode) for path: $path")
            assertTrue(route.bypassOnboarding, "Expected onboarding to be bypassed for direct link: $path")
            assertFalse(route.showSupport)
            assertFalse(route.showPrivacy)
            assertFalse(route.showLanding)
        }
    }

    @Test
    fun testParseBase64EncodeRoutes() {
        val paths = listOf("filetobase64", "base64/encode", "/filetobase64")
        for (path in paths) {
            val route = AppRoute.parse(path, isMac = false, isWeb = true)
            assertEquals(Feature.FILE_TO_BASE64, route.feature, "Expected FILE_TO_BASE64 for path: $path")
            assertEquals(1, route.base64Tab, "Expected tab 1 (Encode) for path: $path")
            assertTrue(route.bypassOnboarding, "Expected onboarding to be bypassed for direct link: $path")
            assertFalse(route.showSupport)
            assertFalse(route.showPrivacy)
            assertFalse(route.showLanding)
        }
    }

    @Test
    fun testParseOtherFeatureRoutes() {
        val passwordRoute = AppRoute.parse("/password", isMac = false, isWeb = true)
        assertEquals(Feature.PASSWORD_GENERATOR, passwordRoute.feature)
        assertFalse(passwordRoute.bypassOnboarding)

        val cloudRunRoute = AppRoute.parse("/cloudrun", isMac = false, isWeb = true)
        assertEquals(Feature.CLOUD_RUN_DEPLOY, cloudRunRoute.feature)
        assertFalse(cloudRunRoute.bypassOnboarding)

        val firebaseRoute = AppRoute.parse("/firebase", isMac = false, isWeb = true)
        assertEquals(Feature.FIREBASE_HOSTING, firebaseRoute.feature)
        assertFalse(firebaseRoute.bypassOnboarding)
    }

    @Test
    fun testParseOverlayRoutes() {
        val supportRoute = AppRoute.parse("/support", isMac = false, isWeb = true)
        assertTrue(supportRoute.showSupport)

        val privacyRoute = AppRoute.parse("/privacy", isMac = false, isWeb = true)
        assertTrue(privacyRoute.showPrivacy)

        val landingRoute = AppRoute.parse("/landing", isMac = false, isWeb = true)
        assertTrue(landingRoute.showLanding)
    }

    @Test
    fun testToPath() {
        val decodeRoute = AppRoute(Feature.FILE_TO_BASE64, base64Tab = 0)
        assertEquals("/decodebase64", decodeRoute.toPath())

        val encodeRoute = AppRoute(Feature.FILE_TO_BASE64, base64Tab = 1)
        assertEquals("/filetobase64", encodeRoute.toPath())

        val passwordRoute = AppRoute(Feature.PASSWORD_GENERATOR)
        assertEquals("/password", passwordRoute.toPath())

        val supportRoute = AppRoute(Feature.CLOUD_RUN_DEPLOY, showSupport = true)
        assertEquals("/support", supportRoute.toPath())
    }
}
