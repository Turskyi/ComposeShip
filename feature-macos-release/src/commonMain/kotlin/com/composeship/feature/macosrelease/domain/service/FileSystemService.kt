package com.composeship.feature.macosrelease.domain.service

interface FileSystemService {
    suspend fun pickDirectory(): String?
    suspend fun pickFile(extension: String): String?
    fun exists(path: String): Boolean
    fun readFile(path: String): String?
    fun listFiles(path: String): List<String>
}
