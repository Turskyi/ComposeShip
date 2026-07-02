package com.composeship.feature.macosrelease.data.service

import com.composeship.feature.macosrelease.domain.service.FileSystemService
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

class DesktopFileSystemService : FileSystemService {
    override suspend fun pickDirectory(): String? {
        System.setProperty("apple.awt.fileDialogForDirectories", "true")
        val dialog = FileDialog(null as Frame?, "Select Project Root", FileDialog.LOAD)
        dialog.isVisible = true
        val directory = dialog.directory
        val file = dialog.file
        System.setProperty("apple.awt.fileDialogForDirectories", "false")
        return if (directory != null && file != null) {
            File(directory, file).absolutePath
        } else {
            null
        }
    }

    override suspend fun pickFile(extension: String): String? {
        val dialog = FileDialog(null as Frame?, "Select File", FileDialog.LOAD)
        dialog.file = "*.$extension"
        dialog.isVisible = true
        val directory = dialog.directory
        val file = dialog.file
        return if (directory != null && file != null) {
            File(directory, file).absolutePath
        } else {
            null
        }
    }

    override fun exists(path: String): Boolean = File(path).exists()

    override fun readFile(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            null
        }
    }

    override fun listFiles(path: String): List<String> {
        return File(path).listFiles()?.map { it.absolutePath } ?: emptyList()
    }
}
