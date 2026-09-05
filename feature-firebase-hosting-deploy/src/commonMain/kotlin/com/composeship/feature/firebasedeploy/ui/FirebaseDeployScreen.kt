package com.composeship.feature.firebasedeploy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.domain.model.LogType
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FirebaseDeployScreen(
    viewModel: FirebaseDeployViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(state.deployLogs.size) {
        if (state.deployLogs.isNotEmpty()) {
            listState.animateScrollToItem(state.deployLogs.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        TechnicalGridBackground()

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            border = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) null 
                     else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(
                    text = stringResource(Res.string.firebase_deploy_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = stringResource(Res.string.firebase_deploy_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = state.projectRoot,
                    onValueChange = { viewModel.onProjectRootChanged(it) },
                    label = { Text(stringResource(Res.string.project_root_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { viewModel.onBrowseProjectRoot() }) {
                            Text(stringResource(Res.string.browse))
                        }
                    },
                    isError = state.projectValidationError != null,
                    supportingText = { state.projectValidationError?.let { Text(it) } }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.startDeploy() },
                    enabled = !state.isDeploying && state.isProjectValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isDeploying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = stringResource(Res.string.deploying))
                    } else {
                        Text(stringResource(Res.string.firebase_deploy_button))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logs Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.logs),
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(
                        onClick = {
                            val text = state.deployLogs.joinToString("\n") { it.message }
                            clipboardManager.setText(AnnotatedString(text))
                        },
                        enabled = state.deployLogs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy logs")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                ) {
                    SelectionContainer {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.deployLogs) { entry ->
                                Text(
                                    text = entry.message,
                                    color = when (entry.type) {
                                        LogType.Info -> MaterialTheme.colorScheme.onSurface
                                        LogType.Error -> MaterialTheme.colorScheme.error
                                        LogType.Success -> Color(0xFF4CAF50)
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                state.deployError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (state.deploySuccess) {
                    Text(
                        stringResource(Res.string.deployed_successfully),
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
