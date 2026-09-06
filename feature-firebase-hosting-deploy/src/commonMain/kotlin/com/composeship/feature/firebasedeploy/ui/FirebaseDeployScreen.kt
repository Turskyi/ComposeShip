package com.composeship.feature.firebasedeploy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.domain.model.DeploySource
import com.composeship.core.domain.model.LogType
import com.composeship.core.domain.platform.PlatformType
import com.composeship.core.domain.platform.getPlatform
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.browse
import composeship.core.generated.resources.deployed_successfully
import composeship.core.generated.resources.deploying
import composeship.core.generated.resources.firebase_deploy_button
import composeship.core.generated.resources.firebase_deploy_desc
import composeship.core.generated.resources.firebase_deploy_title
import composeship.core.generated.resources.logs
import composeship.core.generated.resources.project_root_label
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseDeployScreen(
    viewModel: FirebaseDeployViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val platform = remember { getPlatform() }

    LaunchedEffect(Unit) {
        if (platform.type != PlatformType.DESKTOP) {
            viewModel.onDeploySourceChanged(DeploySource.GITHUB)
        }
    }

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

                // Source Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (platform.type == PlatformType.DESKTOP) {
                        FilterChip(
                            selected = state.deploySource == DeploySource.LOCAL,
                            onClick = { viewModel.onDeploySourceChanged(DeploySource.LOCAL) },
                            label = { Text("Local Project") },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = state.deploySource == DeploySource.GITHUB,
                            onClick = { viewModel.onDeploySourceChanged(DeploySource.GITHUB) },
                            label = { Text("GitHub Repo") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    } else {
                        // On non-desktop, only GITHUB is available, make it non-clickable
                        Surface(
                            shape = FilterChipDefaults.shape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Label,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "GitHub Repo",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.deploySource == DeploySource.LOCAL) {
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
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.githubRepoUrl,
                            onValueChange = { viewModel.onGithubRepoUrlChanged(it) },
                            label = { Text("GitHub Repo URL") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("https://github.com/user/repo") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.githubBranch,
                            onValueChange = { viewModel.onGithubBranchChanged(it) },
                            label = { Text("Branch") },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val canDeploy = if (state.deploySource == DeploySource.LOCAL) {
                    state.isProjectValid
                } else {
                    state.githubRepoUrl.isNotEmpty()
                }

                Button(
                    onClick = { viewModel.startDeploy() },
                    enabled = !state.isDeploying && canDeploy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = if (state.isDeploying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = if (state.isDeploying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (state.isDeploying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.deploying),
                            style = MaterialTheme.typography.labelLarge
                        )
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
