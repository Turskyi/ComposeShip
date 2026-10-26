package com.composeship.ui.base64

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.composeship.base64.LocalFile
import com.composeship.base64.MAX_BASE64_FILE_SIZE_BYTES
import com.composeship.base64.rememberLocalFilePicker
import com.composeship.core.ui.TechnicalGridBackground
import com.composeship.app.shared.generated.resources.Res
import com.composeship.app.shared.generated.resources.base64_choose_file
import com.composeship.app.shared.generated.resources.base64_clear
import com.composeship.app.shared.generated.resources.base64_copy
import com.composeship.app.shared.generated.resources.base64_encode
import com.composeship.app.shared.generated.resources.base64_file_read_error
import com.composeship.app.shared.generated.resources.base64_file_too_large
import com.composeship.app.shared.generated.resources.base64_output
import com.composeship.app.shared.generated.resources.base64_output_placeholder
import com.composeship.app.shared.generated.resources.base64_privacy_note
import com.composeship.app.shared.generated.resources.base64_select_file_hint
import com.composeship.app.shared.generated.resources.base64_title
import com.composeship.app.shared.generated.resources.base64_copied
import org.jetbrains.compose.resources.stringResource

@Composable
fun Base64EncoderScreen(modifier: Modifier = Modifier) {
    var selectedFile by remember { mutableStateOf<LocalFile?>(null) }
    var encodedText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val fileReadError = stringResource(Res.string.base64_file_read_error)
    val fileTooLargeError = stringResource(Res.string.base64_file_too_large)
    val picker = rememberLocalFilePicker { result ->
        result.fold(
            onSuccess = { file ->
                selectedFile = file
                encodedText = null
                errorMessage = ""
                copied = false
            },
            onFailure = {
                errorMessage = fileReadError
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        TechnicalGridBackground()
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            border = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) null
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(Res.string.base64_title), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(Res.string.base64_privacy_note), style = MaterialTheme.typography.bodyMedium)

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(Res.string.base64_choose_file), style = MaterialTheme.typography.titleMedium)
                        Button(onClick = picker) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Text(stringResource(Res.string.base64_choose_file), modifier = Modifier.padding(start = 8.dp))
                        }
                        Text(
                            selectedFile?.name ?: stringResource(Res.string.base64_select_file_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = selectedFile != null,
                        onClick = {
                            val file = selectedFile ?: return@Button
                            if (file.sizeBytes > MAX_BASE64_FILE_SIZE_BYTES) {
                                errorMessage = fileTooLargeError
                                encodedText = null
                            } else {
                                encodedText = file.base64
                                errorMessage = ""
                                copied = false
                            }
                        }
                    ) { Text(stringResource(Res.string.base64_encode)) }
                    if (selectedFile != null || encodedText != null) {
                        IconButton(onClick = {
                            selectedFile = null
                            encodedText = null
                            errorMessage = ""
                            copied = false
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.base64_clear))
                        }
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }

                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(Res.string.base64_output), style = MaterialTheme.typography.titleMedium)
                            Row {
                                IconButton(
                                    enabled = encodedText != null,
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(encodedText.orEmpty()))
                                        copied = true
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(Res.string.base64_copy))
                                }
                                if (copied) Text(stringResource(Res.string.base64_copied), modifier = Modifier.align(Alignment.CenterVertically))
                            }
                        }
                        if (encodedText == null) {
                            Text(
                                stringResource(Res.string.base64_output_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            BasicTextField(
                                value = encodedText.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                maxLines = 12,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp).padding(8.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }
                }
            }
        }
    }
}
