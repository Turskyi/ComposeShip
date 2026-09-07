package com.composeship.core.domain.service

interface FileSystemService {
    fun exists(path: String): Boolean
    fun length(path: String): Long
    fun listFiles(path: String): List<String>
    fun getUserHome(): String?
    fun readFile(path: String): String?
    suspend fun pickDirectory(): String?
    suspend fun pickFile(extension: String? = null): String?
}
