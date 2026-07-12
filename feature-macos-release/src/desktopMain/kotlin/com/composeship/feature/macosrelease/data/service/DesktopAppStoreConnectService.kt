package com.composeship.feature.macosrelease.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.composeship.feature.macosrelease.domain.service.AppStoreConnectService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

class DesktopAppStoreConnectService : AppStoreConnectService {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun findAppId(bundleId: String, issuerId: String, keyId: String, keyPath: String): String? {
        try {
            val token = generateJwt(issuerId, keyId, keyPath)
            
            val response: AppResponse = client.get("https://api.appstoreconnect.apple.com/v1/apps") {
                header("Authorization", "Bearer $token")
                parameter("filter[bundleId]", bundleId)
            }.body()
            
            return response.data.firstOrNull()?.id
        } catch (e: Exception) {
            println("Error finding App ID: ${e.message}")
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
            .withExpiresAt(Date(System.currentTimeMillis() + 20 * 60 * 1000)) // 20 mins
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
