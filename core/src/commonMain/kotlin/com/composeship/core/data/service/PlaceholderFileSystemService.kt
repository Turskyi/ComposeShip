package com.composeship.core.data.service

import com.composeship.core.domain.service.FileSystemService

class PlaceholderFileSystemService : FileSystemService {
    override fun exists(path: String): Boolean = false
    override fun listFiles(path: String): List<String> = emptyList()
    override fun readFile(path: String): String? = null
    override suspend fun pickDirectory(): String? = null
    override suspend fun pickFile(extension: String?): String? = null
}
