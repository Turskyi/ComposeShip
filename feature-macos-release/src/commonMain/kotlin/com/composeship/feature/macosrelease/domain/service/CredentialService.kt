package com.composeship.feature.macosrelease.domain.service

interface CredentialService {
    suspend fun saveCredential(key: String, value: String)
    suspend fun getCredential(key: String): String?
    suspend fun deleteCredential(key: String)
}
