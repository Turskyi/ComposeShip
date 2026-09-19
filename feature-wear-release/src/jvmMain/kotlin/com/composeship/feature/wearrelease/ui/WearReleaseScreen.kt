package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.ok
import composeship.core.generated.resources.wear_keystore_alias_info_text
import composeship.core.generated.resources.wear_keystore_alias_info_title
import composeship.core.generated.resources.wear_keystore_alias_label
import composeship.core.generated.resources.wear_keystore_path_info_text
import composeship.core.generated.resources.wear_keystore_path_info_title
import composeship.core.generated.resources.wear_keystore_path_label
import composeship.core.generated.resources.wear_package_name_info_text
import composeship.core.generated.resources.wear_package_name_info_title
import composeship.core.generated.resources.wear_package_name_label
import composeship.core.generated.resources.wear_save_credentials_info_text
import composeship.core.generated.resources.wear_save_credentials_info_title
import composeship.core.generated.resources.wear_save_credentials_label
import composeship.core.generated.resources.wear_service_account_info_text
import composeship.core.generated.resources.wear_service_account_info_title
import composeship.core.generated.resources.wear_service_account_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoButton(title: String, text: String) {
    var showDialog by remember { mutableStateOf(false) }
    IconButton(onClick = { showDialog = true }) {
        Icon(Icons.Default.Info, contentDescription = "Info")
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(Res.string.ok))
                }
            }
        )
    }
}

@Composable
fun WearReleaseScreen(viewModel: WearReleaseViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state.collectAsState()
    val s = state.value
    val listState = rememberLazyListState()
    val scrollState = rememberScrollState()
    var autoScroll by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
    ) {
        if (s.buildInProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = s.projectRoot,
                    onValueChange = { viewModel.onProjectRootChanged(it) },
                    label = { Text("Flutter project root") },
                    modifier = Modifier.weight(1f),
                    enabled = !s.buildInProgress
                )
                Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.onBrowseProjectRoot() }, enabled = !s.buildInProgress) {
                Text("Browse")
            }
            }
            if (!s.isProjectValid && s.projectRoot.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Selected path is not a Flutter project.", modifier = Modifier.padding(start = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Button(onClick = { viewModel.buildWear(s.projectRoot) }, enabled = s.isProjectValid && !s.buildInProgress) {
                Text("Build Wear AAB")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.generateReleaseNotesFromGit(s.projectRoot) }, enabled = !s.buildInProgress) {
                Text("Generate Release Notes (git)")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = s.releaseNotes,
            onValueChange = { viewModel.onReleaseNotesChanged(it) },
            label = { Text("Release Notes (generated)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !s.buildInProgress
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = s.serviceAccountPath,
                onValueChange = { viewModel.onServiceAccountPathChanged(it) },
                label = { Text(stringResource(Res.string.wear_service_account_label)) },
                modifier = Modifier.weight(1f),
                enabled = !s.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_service_account_info_title),
                text = stringResource(Res.string.wear_service_account_info_text)
            )
        }
        Button(onClick = { viewModel.onBrowseServiceAccount() }, modifier = Modifier.padding(top = 8.dp), enabled = !s.buildInProgress) { Text("Browse JSON") }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = s.packageName,
                onValueChange = { viewModel.onPackageNameChanged(it) },
                label = { Text(stringResource(Res.string.wear_package_name_label)) },
                modifier = Modifier.weight(1f),
                enabled = !s.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_package_name_info_title),
                text = stringResource(Res.string.wear_package_name_info_text)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = s.keystorePath,
                onValueChange = { viewModel.onKeystorePathChanged(it) },
                label = { Text(stringResource(Res.string.wear_keystore_path_label)) },
                modifier = Modifier.weight(1f),
                enabled = !s.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_keystore_path_info_title),
                text = stringResource(Res.string.wear_keystore_path_info_text)
            )
        }
        Button(onClick = { viewModel.onBrowseKeystore() }, modifier = Modifier.padding(top = 8.dp), enabled = !s.buildInProgress) { Text("Browse Keystore") }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = s.keystoreAlias,
                onValueChange = { viewModel.onKeystoreAliasChanged(it) },
                label = { Text(stringResource(Res.string.wear_keystore_alias_label)) },
                modifier = Modifier.weight(1f),
                enabled = !s.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_keystore_alias_info_title),
                text = stringResource(Res.string.wear_keystore_alias_info_text)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Track selection
        var trackExpanded by remember { mutableStateOf(false) }
        val tracks = listOf("internal", "alpha", "beta", "production")
        Button(onClick = { trackExpanded = true }, enabled = !s.buildInProgress) { Text("Track: ${s.track}") }
        androidx.compose.material3.DropdownMenu(expanded = trackExpanded, onDismissRequest = { trackExpanded = false }) {
            tracks.forEach { t ->
                androidx.compose.material3.DropdownMenuItem(text = { Text(t) }, onClick = { viewModel.onTrackChanged(t); trackExpanded = false })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { viewModel.saveCredentials() }, modifier = Modifier, enabled = !s.buildInProgress) {
                Text(stringResource(Res.string.wear_save_credentials_label))
            }
            InfoButton(
                title = stringResource(Res.string.wear_save_credentials_info_title),
                text = stringResource(Res.string.wear_save_credentials_info_text)
            )
            Spacer(modifier = Modifier.width(8.dp))
            val uploadEnabled = !s.buildInProgress && s.serviceAccountPath.isNotBlank() && s.packageName.isNotBlank()
            Button(onClick = { viewModel.prepareAndUpload(s.projectRoot) }, enabled = uploadEnabled) {
                Text(if (s.track == "production") "Upload to Play (production)" else "Upload to Play")
            }
        }

        if (s.needsConfirmation) {
            Text("Production upload requires confirmation — press Upload again to confirm.", modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logs
        Text("Logs", modifier = Modifier.padding(bottom = 4.dp))
        SelectionContainer {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                items(s.lastOutputLines) { line ->
                    Text(line)
                }
            }
        }

        LaunchedEffect(s.lastOutputLines.size) {
            if (autoScroll && s.lastOutputLines.isNotEmpty()) {
                listState.scrollToItem(s.lastOutputLines.size - 1)
            }
        }
    }
}
