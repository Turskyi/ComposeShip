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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
fun WearReleaseScreen(viewModel: WearReleaseViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state.collectAsState()
    val s = state.value
    val listState = rememberLazyListState()
    var autoScroll by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
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

        OutlinedTextField(
            value = s.serviceAccountPath,
            onValueChange = { viewModel.onServiceAccountPathChanged(it) },
            label = { Text("Service-account JSON path (for upload)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !s.buildInProgress
        )
        Button(onClick = { viewModel.onBrowseServiceAccount() }, modifier = Modifier.padding(top = 8.dp), enabled = !s.buildInProgress) { Text("Browse JSON") }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = s.packageName, onValueChange = { viewModel.onPackageNameChanged(it) }, label = { Text("Package name (com.example.app)") }, modifier = Modifier.fillMaxWidth(), enabled = !s.buildInProgress)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = s.keystorePath, onValueChange = { viewModel.onKeystorePathChanged(it) }, label = { Text("Keystore path (.jks/.keystore)") }, modifier = Modifier.fillMaxWidth(), enabled = !s.buildInProgress)
        Button(onClick = { viewModel.onBrowseKeystore() }, modifier = Modifier.padding(top = 8.dp), enabled = !s.buildInProgress) { Text("Browse Keystore") }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = s.keystoreAlias, onValueChange = { viewModel.onKeystoreAliasChanged(it) }, label = { Text("Keystore alias") }, modifier = Modifier.fillMaxWidth(), enabled = !s.buildInProgress)

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

        Row {
            Button(onClick = { viewModel.saveCredentials() }, modifier = Modifier, enabled = !s.buildInProgress) { Text("Save Credentials") }
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
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().weight(1f)) {
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
