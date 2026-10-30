package com.composeship.ui.base64

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.composeship.app.shared.generated.resources.Res
import com.composeship.app.shared.generated.resources.base64_clear
import com.composeship.app.shared.generated.resources.base64_copied
import com.composeship.app.shared.generated.resources.base64_copy
import com.composeship.app.shared.generated.resources.base64_decode_button
import com.composeship.app.shared.generated.resources.base64_decode_error
import com.composeship.app.shared.generated.resources.base64_decode_input_hint
import com.composeship.app.shared.generated.resources.base64_decode_input_label
import com.composeship.app.shared.generated.resources.base64_decode_output
import com.composeship.app.shared.generated.resources.base64_decode_output_placeholder
import com.composeship.app.shared.generated.resources.base64_pretty_print
import com.composeship.base64.decodeBase64
import com.composeship.base64.formatJsonIfValid
import org.jetbrains.compose.resources.stringResource

@Composable
fun Base64DecodeSection(modifier: Modifier = Modifier) {
    var inputString by remember { mutableStateOf("") }
    var decodedText by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var prettyPrintEnabled by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val invalidBase64Error = stringResource(Res.string.base64_decode_error)
    val formattedJson = decodedText?.let { formatJsonIfValid(it) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputString,
                    onValueChange = {
                        inputString = it
                        errorMessage = ""
                    },
                    label = { Text(stringResource(Res.string.base64_decode_input_label)) },
                    placeholder = { Text(stringResource(Res.string.base64_decode_input_hint)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp),
                    maxLines = 10
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = inputString.isNotBlank(),
                        onClick = {
                            val result = decodeBase64(inputString)
                            result.fold(
                                onSuccess = { decoded ->
                                    decodedText = decoded
                                    errorMessage = ""
                                    copied = false
                                    prettyPrintEnabled = false
                                },
                                onFailure = {
                                    decodedText = null
                                    errorMessage = invalidBase64Error
                                }
                            )
                        }
                    ) {
                        Text(stringResource(Res.string.base64_decode_button))
                    }

                    if (inputString.isNotEmpty() || decodedText != null) {
                        IconButton(onClick = {
                            inputString = ""
                            decodedText = null
                            errorMessage = ""
                            prettyPrintEnabled = false
                            copied = false
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.base64_clear))
                        }
                    }
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
                    Text(stringResource(Res.string.base64_decode_output), style = MaterialTheme.typography.titleMedium)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (formattedJson != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Checkbox(
                                    checked = prettyPrintEnabled,
                                    onCheckedChange = { prettyPrintEnabled = it }
                                )
                                Text(
                                    stringResource(Res.string.base64_pretty_print),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        IconButton(
                            enabled = decodedText != null,
                            onClick = {
                                val textToCopy = if (prettyPrintEnabled && formattedJson != null) {
                                    formattedJson
                                } else {
                                    decodedText.orEmpty()
                                }
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                copied = true
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = stringResource(Res.string.base64_copy))
                        }
                        if (copied) {
                            Text(stringResource(Res.string.base64_copied), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (decodedText == null) {
                    Text(
                        stringResource(Res.string.base64_decode_output_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val displayedText = if (prettyPrintEnabled && formattedJson != null) {
                        formattedJson
                    } else {
                        decodedText.orEmpty()
                    }
                    OutlinedTextField(
                        value = displayedText,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 14,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }
    }
}
