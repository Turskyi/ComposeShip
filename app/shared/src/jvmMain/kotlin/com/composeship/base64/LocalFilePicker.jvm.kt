package com.composeship.base64

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import javax.swing.JFileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberLocalFilePicker(onFilePicked: (Result<LocalFile>) -> Unit): () -> Unit {
    val coroutineScope = rememberCoroutineScope()
    return remember(onFilePicked, coroutineScope) {
        {
            val chooser = JFileChooser()
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file: File = chooser.selectedFile
                coroutineScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        readLocalFileResult {
                            if (file.length() > MAX_BASE64_FILE_SIZE_BYTES) {
                                LocalFile(file.name, "", file.length())
                            } else {
                                localFileFromBytes(file.name, file.readBytes())
                            }
                        }
                    }
                    onFilePicked(result)
                }
            }
        }
    }
}
