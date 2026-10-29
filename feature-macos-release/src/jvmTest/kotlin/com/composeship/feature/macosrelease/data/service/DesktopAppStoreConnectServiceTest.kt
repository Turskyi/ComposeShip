package com.composeship.feature.macosrelease.data.service

import kotlin.test.Test
import kotlin.test.assertNotNull

class DesktopAppStoreConnectServiceTest {

    @Test
    fun serviceInstantiationDoesNotThrowServiceConfigurationError() {
        val service = DesktopAppStoreConnectService()
        assertNotNull(service, "DesktopAppStoreConnectService should instantiate successfully without ServiceConfigurationError")
    }
}
