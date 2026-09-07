package com.composeship.core.data.service

import com.composeship.core.domain.service.FileSystemService

class PlaceholderFileSystemService : FileSystemService {
    override fun exists(path: String): Boolean = false
    override fun length(path: String): Long = 0L
    override fun listFiles(path: String): List<String> = emptyList()
    override fun getUserHome(): String? = null
    override fun readFile(path: String): String? = null
    override suspend fun pickDirectory(): String? = null
    override suspend fun pickFile(extension: String?): String? = null
}
