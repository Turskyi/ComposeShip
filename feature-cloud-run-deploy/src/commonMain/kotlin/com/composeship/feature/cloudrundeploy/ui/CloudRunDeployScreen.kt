package com.composeship.feature.cloudrundeploy.ui

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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.composeship.core.ui.LinkifyText
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.browse
import composeship.core.generated.resources.cloud_run_deploy_title
import composeship.core.generated.resources.deploy
import composeship.core.generated.resources.deployed_successfully
import composeship.core.generated.resources.deploying
import composeship.core.generated.resources.gcloud_project_id_info_text
import composeship.core.generated.resources.gcloud_project_id_info_title
import composeship.core.generated.resources.gcloud_project_id_label
import composeship.core.generated.resources.logs
import composeship.core.generated.resources.no_services_found
import composeship.core.generated.resources.ok
import composeship.core.generated.resources.project_root_label
import composeship.core.generated.resources.region_label
import composeship.core.generated.resources.service_name_info_text
import composeship.core.generated.resources.service_name_info_title
import composeship.core.generated.resources.service_name_label
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudRunDeployScreen(
    viewModel: CloudRunDeployViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val platform = remember { getPlatform() }
    var showInfoDialog by remember { mutableStateOf(false) }
    var servicesExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (platform.type != PlatformType.DESKTOP) {
            viewModel.onDeploySourceChanged(DeploySource.GITHUB)
        }
    }
    var infoDialogTitle by remember { mutableStateOf("") }
    var infoDialogText by remember { mutableStateOf("") }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(infoDialogTitle) },
            text = { LinkifyText(infoDialogText) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(Res.string.ok))
                }
            }
        )
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
                    text = stringResource(Res.string.cloud_run_deploy_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Source Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (platform.type == PlatformType.DESKTOP) {
                        FilterChip(
                            selected = state.deploySource == DeploySource.LOCAL,
                            onClick = { if (!state.isDeploying) viewModel.onDeploySourceChanged(DeploySource.LOCAL) },
                            enabled = !state.isDeploying,
                            label = { Text("Local Project") },
                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = state.deploySource == DeploySource.GITHUB,
                            onClick = { if (!state.isDeploying) viewModel.onDeploySourceChanged(DeploySource.GITHUB) },
                            enabled = !state.isDeploying,
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

                // Configuration Section
                if (state.deploySource == DeploySource.LOCAL) {
                    OutlinedTextField(
                        value = state.projectRoot,
                        onValueChange = { viewModel.onProjectRootChanged(it) },
                        enabled = !state.isDeploying,
                        label = { Text(stringResource(Res.string.project_root_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            TextButton(
                                onClick = { viewModel.onBrowseProjectRoot() },
                                enabled = !state.isDeploying
                            ) {
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
                            enabled = !state.isDeploying,
                            label = { Text("GitHub Repo URL") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("https://github.com/user/repo") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.githubBranch,
                            onValueChange = { viewModel.onGithubBranchChanged(it) },
                            enabled = !state.isDeploying,
                            label = { Text("Branch") },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.gcloudProjectId,
                        onValueChange = { viewModel.onGcloudProjectIdChanged(it) },
                        enabled = !state.isDeploying,
                        label = { Text(stringResource(Res.string.gcloud_project_id_label)) },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            val canAutoFetch = (state.deploySource == DeploySource.LOCAL && state.projectRoot.isNotEmpty()) || 
                                              (state.deploySource == DeploySource.GITHUB)
                            
                            if (canAutoFetch && !state.autoFetchFailed) {
                                IconButton(
                                    onClick = { viewModel.autoFetchProjectId() },
                                    enabled = !state.isAutoFetchingProjectId && !state.isDeploying
                                ) {
                                    if (state.isAutoFetchingProjectId) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixHigh,
                                            contentDescription = "Auto-fetch project ID",
                                            tint = if (state.isDeploying) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                val title = stringResource(Res.string.gcloud_project_id_info_title)
                                val text = stringResource(Res.string.gcloud_project_id_info_text)
                                IconButton(onClick = {
                                    infoDialogTitle = title
                                    infoDialogText = text
                                    showInfoDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Project ID Info"
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = state.region,
                        onValueChange = { viewModel.onRegionChanged(it) },
                        enabled = !state.isDeploying,
                        label = { Text(stringResource(Res.string.region_label)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    ExposedDropdownMenuBox(
                        expanded = servicesExpanded,
                        onExpandedChange = {
                            if (state.gcloudProjectId.isNotEmpty()) {
                                servicesExpanded = it
                                if (it) viewModel.fetchServices()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.serviceName,
                            onValueChange = { viewModel.onServiceNameChanged(it) },
                            enabled = !state.isDeploying,
                            label = { Text(stringResource(Res.string.service_name_label)) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (state.isFetchingServices) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.ExpandMore, contentDescription = null)
                                    }
                                    Spacer(modifier = Modifier.width(40.dp))
                                }
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = servicesExpanded,
                            onDismissRequest = { servicesExpanded = false }
                        ) {
                            if (state.availableServices.isEmpty() && !state.isFetchingServices) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.no_services_found)) },
                                    onClick = { servicesExpanded = false }
                                )
                            }
                            state.availableServices.forEach { service ->
                                DropdownMenuItem(
                                    text = { Text(service) },
                                    onClick = {
                                        viewModel.onServiceNameChanged(service)
                                        servicesExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val serviceTitle = stringResource(Res.string.service_name_info_title)
                    val serviceText = stringResource(Res.string.service_name_info_text)
                    IconButton(
                        onClick = {
                            infoDialogTitle = serviceTitle
                            infoDialogText = serviceText
                            showInfoDialog = true
                            servicesExpanded = false
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Service Name Info")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val canDeploy = if (state.deploySource == DeploySource.LOCAL) {
                    state.isProjectValid && state.gcloudProjectId.isNotEmpty()
                } else {
                    state.githubRepoUrl.isNotEmpty() && state.gcloudProjectId.isNotEmpty()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.startDeploy() },
                        enabled = !state.isDeploying && canDeploy,
                        modifier = Modifier.weight(1f),
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
                            Text(stringResource(Res.string.deploy))
                        }
                    }

                    if (state.isDeploying) {
                        Button(
                            onClick = { viewModel.stopDeploy() },
                            modifier = Modifier.height(ButtonDefaults.MinHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text("Stop")
                        }
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
