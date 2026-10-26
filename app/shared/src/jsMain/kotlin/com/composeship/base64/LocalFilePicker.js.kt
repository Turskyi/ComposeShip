package com.composeship.base64

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@Composable
actual fun rememberLocalFilePicker(onFilePicked: (Result<LocalFile>) -> Unit): () -> Unit =
    remember(onFilePicked) {
        {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.style.display = "none"
            input.onchange = {
                val file = input.files?.item(0)
                if (file != null) {
                    val sizeBytes = file.size.toLong()
                    if (sizeBytes > MAX_BASE64_FILE_SIZE_BYTES) {
                        onFilePicked(Result.success(LocalFile(file.name, "", sizeBytes)))
                    } else {
                        val reader = FileReader()
                        reader.onload = {
                            val dataUrl = reader.result as String
                            val separator = dataUrl.indexOf(',')
                            if (separator < 0) {
                                onFilePicked(Result.failure(IllegalStateException("Unable to read the selected file")))
                            } else {
                                val encoded = dataUrl.substring(separator + 1)
                                onFilePicked(Result.success(LocalFile(file.name, encoded, sizeBytes)))
                            }
                            input.remove()
                        }
                        reader.onerror = {
                            onFilePicked(Result.failure(IllegalStateException("Unable to read the selected file")))
                            input.remove()
                        }
                        reader.readAsDataURL(file)
                    }
                } else {
                    input.remove()
                }
            }
            document.body?.appendChild(input)
            input.click()
        }
    }
