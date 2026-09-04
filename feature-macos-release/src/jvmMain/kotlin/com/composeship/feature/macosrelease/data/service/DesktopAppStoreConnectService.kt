package com.composeship.feature.macosrelease.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.composeship.feature.macosrelease.domain.service.AppStoreConnectService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.Date

class DesktopAppStoreConnectService : AppStoreConnectService {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun findAppId(
        bundleId: String,
        issuerId: String,
        keyId: String,
        keyPath: String,
        onLog: (String) -> Unit
    ): String? {
        try {
            val token = generateJwt(issuerId, keyId, keyPath)
            
            val response = client.get("https://api.appstoreconnect.apple.com/v1/apps") {
                header("Authorization", "Bearer $token")
                parameter("filter[bundleId]", bundleId)
            }
            
            val responseBody = response.body<String>()
            onLog("App Store Connect API Request: ${response.call.request.url}")
            onLog("App Store Connect API Status: ${response.status}")
            onLog("App Store Connect API Response: $responseBody")

            if (response.status.value in 200..299) {
                val appResponse = Json { ignoreUnknownKeys = true }.decodeFromString<AppResponse>(responseBody)
                val appId = appResponse.data.firstOrNull()?.id
                if (appId == null) {
                    onLog("App Store Connect API: No app found for bundle ID $bundleId.")
                }
                return appId
            } else {
                onLog("App Store Connect API Error: ${response.status} - $responseBody")
                return null
            }
        } catch (e: Exception) {
            onLog("Error finding App ID: ${e.message}")
            return null
        }
    }

    private fun generateJwt(issuerId: String, keyId: String, keyPath: String): String {
        val privateKeyContent = File(keyPath).readText()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        
        val keyBytes = Base64.getDecoder().decode(privateKeyContent)
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("EC")
        val privateKey = kf.generatePrivate(spec) as ECPrivateKey
        
        val algorithm = Algorithm.ECDSA256(null, privateKey)
        
        return JWT.create()
            .withIssuer(issuerId)
            .withKeyId(keyId)
            .withAudience("appstoreconnect-v1")
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 mins
            .sign(algorithm)
    }

    @Serializable
    private data class AppResponse(
        val data: List<AppData>
    )

    @Serializable
    private data class AppData(
        val id: String,
        val attributes: AppAttributes
    )

    @Serializable
    private data class AppAttributes(
        val bundleId: String,
        val name: String
    )
}
