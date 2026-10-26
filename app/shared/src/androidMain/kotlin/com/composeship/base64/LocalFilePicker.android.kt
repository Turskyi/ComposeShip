package com.composeship.base64

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberLocalFilePicker(onFilePicked: (Result<LocalFile>) -> Unit): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val result = withContext(Dispatchers.IO) {
                    readLocalFileResult {
                        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameColumn = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (cursor.moveToFirst() && nameColumn >= 0) cursor.getString(nameColumn) else null
                        } ?: "file"
                        val sizeBytes = context.contentResolver.openAssetFileDescriptor(uri, "r")
                            ?.use { it.length } ?: -1L
                        if (sizeBytes > MAX_BASE64_FILE_SIZE_BYTES) {
                            LocalFile(name, "", sizeBytes)
                        } else {
                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: error("Unable to open the selected file")
                            localFileFromBytes(name, bytes)
                        }
                    }
                }
                onFilePicked(result)
            }
        }
    }
    return remember(launcher) { { launcher.launch(arrayOf("*/*")) } }
}
