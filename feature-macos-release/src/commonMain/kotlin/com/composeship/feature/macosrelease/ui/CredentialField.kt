package com.composeship.feature.macosrelease.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun CredentialField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    helpText: String,
    isError: Boolean,
    errorText: String,
    onActionClick: (() -> Unit)? = null,
    actionLabel: String? = null,
    enabled: Boolean = true
) {
    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                label = { Text(label) },
                modifier = Modifier.weight(1f),
                isError = isError,
                supportingText = {
                    if (isError) Text(errorText)
                }
            )
            IconButton(onClick = { showHelp = !showHelp }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = "Help",
                    tint = if (showHelp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showHelp) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.padding(top = 4.dp, end = 48.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = helpText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (onActionClick != null && actionLabel != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onActionClick,
                            contentPadding = PaddingValues(
                                vertical = 0.dp,
                                horizontal = 8.dp
                            )
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelMedium,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }
                }
            }
        }
    }
}
