package com.composeship.feature.macosrelease.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.ui.TechnicalGridBackground

@Composable
fun MacOsReleaseScreen(
    viewModel: MacOsReleaseViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        TechnicalGridBackground()

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 2.dp,
            border =
                if (MaterialTheme.colorScheme.background.luminance() < 0.5f)
                    null
                else
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                    )
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(
                    text = "macOS App Store Release Tool",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                when (state.step) {
                    ReleaseStep.SelectProject -> ProjectSelectionStep(
                        state,
                        viewModel
                    )

                    ReleaseStep.SelectTask -> TaskSelectionStep(
                        state,
                        viewModel
                    )

                    ReleaseStep.SigningIdentity -> IdentitySelectionStep(
                        state,
                        viewModel
                    )

                    ReleaseStep.AppStoreCredentials -> CredentialsStep(
                        state,
                        viewModel
                    )

                    ReleaseStep.Process -> ReleaseProcessStep(state, viewModel)
                }
            }
        }
    }
}

@Composable
fun ProjectSelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    Column {
        Text(
            "Step 1: Select Compose Multiplatform Project Root",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.projectRoot,
                onValueChange = { viewModel.onProjectRootChanged(it) },
                label = { Text("Project Root Path") },
                modifier = Modifier.weight(1f),
                isError = state.projectValidationError != null,
                supportingText = {
                    state.projectValidationError?.let {
                        Text(it)
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.onBrowseProjectRoot() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Browse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.nextStep() },
            enabled = state.isProjectValid && state.projectRoot.isNotEmpty()
        ) {
            Text("Next")
        }
    }
}

@Composable
fun TaskSelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    Column {
        Text(
            "Step 2: Select Packaging Task",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        state.availableTasks.forEach { task ->
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = state.selectedTask == task,
                    onClick = { viewModel.onTaskSelected(task) }
                )
                Text(task, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.nextStep() }) {
            Text("Next")
        }
    }
}

@Composable
fun IdentitySelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    Column {
        Text(
            "Step 3: Select Signing Identities",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Application Identity (for .app bundle)",
            style = MaterialTheme.typography.titleSmall
        )
        if (state.signingIdentities.isEmpty()) {
            Text(
                "No Application identities found.",
                color = MaterialTheme.colorScheme.error
            )
        }
        state.signingIdentities.forEach { identity ->
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = state.selectedIdentity == identity,
                    onClick = { viewModel.onIdentitySelected(identity) }
                )
                Text(identity, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Installer Identity (for .pkg package)",
            style = MaterialTheme.typography.titleSmall
        )
        if (state.installerIdentities.isEmpty()) {
            Text(
                "No Installer identities found.",
                color = MaterialTheme.colorScheme.error
            )
        }
        state.installerIdentities.forEach { identity ->
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = state.selectedInstallerIdentity == identity,
                    onClick = {
                        viewModel.onInstallerIdentitySelected(identity)
                    }
                )
                Text(identity, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.nextStep() },
            enabled = state.selectedIdentity.isNotEmpty() &&
                    state.selectedInstallerIdentity.isNotEmpty()
        ) {
            Text("Next")
        }
    }
}

@Composable
fun CredentialsStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    Column {
        Text(
            "Step 4: App Store Connect Credentials",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.apiIssuerId,
            onValueChange = {
                viewModel.onCredentialsChanged(
                    it,
                    state.apiKeyId,
                    state.apiKeyPath
                )
            },
            label = { Text("Issuer ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.apiKeyId,
            onValueChange = {
                viewModel.onCredentialsChanged(
                    state.apiIssuerId,
                    it,
                    state.apiKeyPath
                )
            },
            label = { Text("Key ID") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.apiKeyPath,
            onValueChange = {
                viewModel.onCredentialsChanged(
                    state.apiIssuerId,
                    state.apiKeyId,
                    it
                )
            },
            label = { Text("API Key (.p8) Path") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.nextStep() }) {
            Text("Next")
        }
    }
}

@Composable
fun ReleaseProcessStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    var showLogs by remember { mutableStateOf(true) }

    Column {
        Text(
            "Step 5: Release Process",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.isReleasing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.startRelease() },
            enabled = !state.isReleasing && !state.releaseSuccess
        ) {
            Text(
                if (state.releaseSuccess)
                    "Released Successfully"
                else
                    "Start Build & Release",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Logs", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { showLogs = !showLogs }) {
                Text(if (showLogs) "Hide Details" else "Show Details")
            }
        }

        if (showLogs) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.3f
                        )
                    )
                    .padding(8.dp)
            ) {
                items(state.releaseLogs) { entry ->
                    Text(
                        text = entry.message,
                        color = when (entry.type) {
                            LogType.Info -> MaterialTheme.colorScheme.onSurface
                            LogType.Error -> MaterialTheme.colorScheme.error
                            // TODO: Consider moving to theme
                            LogType.Success -> Color(0xFF4CAF50)
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        state.releaseError?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
