package com.composeship.feature.macosrelease.domain.service

interface AppStoreConnectService {
    suspend fun findAppId(
        bundleId: String,
        issuerId: String,
        keyId: String,
        keyPath: String,
        onLog: (String) -> Unit = {}
    ): String?
}
