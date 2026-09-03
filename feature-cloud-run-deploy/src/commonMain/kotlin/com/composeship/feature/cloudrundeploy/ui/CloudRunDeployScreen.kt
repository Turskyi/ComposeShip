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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import com.composeship.core.domain.model.LogType
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
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogTitle by remember { mutableStateOf("") }
    var infoDialogText by remember { mutableStateOf("") }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(infoDialogTitle) },
            text = { Text(infoDialogText) },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Configuration Section
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.gcloudProjectId,
                        onValueChange = { viewModel.onGcloudProjectIdChanged(it) },
                        label = { Text(stringResource(Res.string.gcloud_project_id_label)) },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            if (state.projectRoot.isNotEmpty() && !state.autoFetchFailed) {
                                IconButton(
                                    onClick = { viewModel.autoFetchProjectId() },
                                    enabled = !state.isAutoFetchingProjectId
                                ) {
                                    if (state.isAutoFetchingProjectId) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixHigh,
                                            contentDescription = "Auto-fetch project ID",
                                            tint = MaterialTheme.colorScheme.primary
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
                        label = { Text(stringResource(Res.string.region_label)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                var servicesExpanded by remember { mutableStateOf(false) }

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
                        label = { Text(stringResource(Res.string.service_name_label)) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (state.isFetchingServices) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                                }
                                if (state.availableServices.size > 1 || state.serviceFetchFailed) {
                                    val serviceTitle = stringResource(Res.string.service_name_info_title)
                                    val serviceText = stringResource(Res.string.service_name_info_text)
                                    IconButton(onClick = {
                                        infoDialogTitle = serviceTitle
                                        infoDialogText = serviceText
                                        showInfoDialog = true
                                    }) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = "Service Name Info")
                                    }
                                }
                            }
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = servicesExpanded,
                        onDismissRequest = { servicesExpanded = false }
                    ) {
                        if (state.availableServices.isEmpty() && !state.isFetchingServices) {
                            DropdownMenuItem(
                                text = { Text("No services found. Type a new name to create one.") },
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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.startDeploy() },
                    enabled = !state.isDeploying && state.isProjectValid && state.gcloudProjectId.isNotEmpty(),
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
                        Text(stringResource(Res.string.deploy))
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
