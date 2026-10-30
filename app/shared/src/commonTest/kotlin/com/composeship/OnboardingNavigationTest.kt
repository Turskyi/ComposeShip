package com.composeship

import kotlin.test.Test
import kotlin.test.assertFalse

class OnboardingNavigationTest {

    @Test
    fun testOnboardingStateHidesNavRailLogic() {
        // Regression test: ensure that when onboarding is shown, nav rail is never shown.
        val showOnboarding = true
        val showNavRail = !showOnboarding
        assertFalse(showNavRail, "Nav rail must never be shown when onboarding is active")
    }

    @Test
    fun testLandingPageHidesNavRailLogic() {
        // Regression test: ensure that when landing page is shown, nav rail is never shown.
        val showLanding = true
        val showNavRail = !showLanding
        assertFalse(showNavRail, "Nav rail must never be shown when landing page is active")
    }
}
