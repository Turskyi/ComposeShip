package com.composeship.feature.macosrelease.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.api_key_path_label
import composeship.core.generated.resources.app_identity_label
import composeship.core.generated.resources.back
import composeship.core.generated.resources.browse
import composeship.core.generated.resources.hide_details
import composeship.core.generated.resources.identity_app_store_desc
import composeship.core.generated.resources.identity_developer_id_desc
import composeship.core.generated.resources.installer_identity_explanation
import composeship.core.generated.resources.installer_identity_label
import composeship.core.generated.resources.issuer_id_label
import composeship.core.generated.resources.key_id_label
import composeship.core.generated.resources.logs
import composeship.core.generated.resources.macos_release_title
import composeship.core.generated.resources.next
import composeship.core.generated.resources.no_app_identities
import composeship.core.generated.resources.no_installer_identities
import composeship.core.generated.resources.open_apple_developer
import composeship.core.generated.resources.project_root_label
import composeship.core.generated.resources.refresh
import composeship.core.generated.resources.released_successfully
import composeship.core.generated.resources.show_details
import composeship.core.generated.resources.start_over
import composeship.core.generated.resources.start_release
import composeship.core.generated.resources.step_1_title
import composeship.core.generated.resources.step_2_title
import composeship.core.generated.resources.step_3_title
import composeship.core.generated.resources.step_4_title
import composeship.core.generated.resources.step_5_title
import composeship.core.generated.resources.task_debug_desc
import composeship.core.generated.resources.task_debug_title
import composeship.core.generated.resources.task_release_desc
import composeship.core.generated.resources.task_release_title
import org.jetbrains.compose.resources.stringResource

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
                    text = stringResource(Res.string.macos_release_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    }
                ) { step ->
                    when (step) {
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

                        ReleaseStep.Process -> ReleaseProcessStep(
                            state,
                            viewModel
                        )
                    }
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
            stringResource(Res.string.step_1_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.projectRoot,
                onValueChange = { viewModel.onProjectRootChanged(it) },
                label = { Text(stringResource(Res.string.project_root_label)) },
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
                Text(stringResource(Res.string.browse))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.nextStep() },
            enabled = state.isProjectValid && state.projectRoot.isNotEmpty()
        ) {
            Text(stringResource(Res.string.next))
        }
    }
}

@Composable
fun TaskSelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    var showDebugWarning by remember { mutableStateOf(false) }

    Column {
        Text(
            stringResource(Res.string.step_2_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        state.availableTasks.forEach { task ->
            val isRelease = task == "packageReleasePkg"
            val title =
                if (isRelease) Res.string.task_release_title else Res.string.task_debug_title
            val desc =
                if (isRelease) Res.string.task_release_desc else Res.string.task_debug_desc

            Surface(
                onClick = {
                    viewModel.onTaskSelected(task)
                    if (!isRelease) showDebugWarning = true
                },
                shape = MaterialTheme.shapes.medium,
                color = if (state.selectedTask == task) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedTask == task,
                        onClick = {
                            viewModel.onTaskSelected(task)
                            if (!isRelease) showDebugWarning = true
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (!isRelease && state.selectedTask == task) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showDebugWarning && state.selectedTask == "packagePkg") {
            Text(
                "⚠️ Warning: packagePkg is for local testing. Do not use for App Store submission.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedButton(onClick = { viewModel.previousStep() }) {
                Text(stringResource(Res.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.nextStep() }) {
                Text(stringResource(Res.string.next))
            }
        }
    }
}

@Composable
fun IdentitySelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    val uriHandler = LocalUriHandler.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.step_3_title),
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = { viewModel.loadSigningIdentities() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(Res.string.refresh))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            stringResource(Res.string.app_identity_label),
            style = MaterialTheme.typography.titleSmall
        )
        if (state.isLoadingIdentities) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        } else if (state.signingIdentities.isEmpty()) {
            Text(
                stringResource(Res.string.no_app_identities),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        state.signingIdentities.forEach { identity ->
            val isAppStore =
                identity.contains("3rd Party Mac Developer Application")
            val isDeveloperId = identity.contains("Developer ID Application")
            val description = when {
                isAppStore -> stringResource(Res.string.identity_app_store_desc)
                isDeveloperId -> stringResource(Res.string.identity_developer_id_desc)
                else -> null
            }

            Surface(
                onClick = { viewModel.onIdentitySelected(identity) },
                shape = MaterialTheme.shapes.small,
                color = if (state.selectedIdentity == identity) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(
                        vertical = 4.dp,
                        horizontal = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = state.selectedIdentity == identity,
                        onClick = { viewModel.onIdentitySelected(identity) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            identity,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (description != null) {
                            Text(
                                description,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDeveloperId) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(Res.string.installer_identity_label),
            style = MaterialTheme.typography.titleSmall
        )
        if (state.isLoadingIdentities) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        } else if (state.installerIdentities.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        MaterialTheme.shapes.small
                    )
                    .padding(12.dp)
            ) {
                Text(
                    stringResource(Res.string.no_installer_identities),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.installer_identity_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { uriHandler.openUri("https://developer.apple.com/account/resources/certificates/list") }
                ) {
                    Text(stringResource(Res.string.open_apple_developer))
                }
            }
        } else {
            state.installerIdentities.forEach { identity ->
                Surface(
                    onClick = { viewModel.onInstallerIdentitySelected(identity) },
                    shape = MaterialTheme.shapes.small,
                    color = if (state.selectedInstallerIdentity == identity) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(
                            vertical = 4.dp,
                            horizontal = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.selectedInstallerIdentity == identity,
                            onClick = {
                                viewModel.onInstallerIdentitySelected(identity)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            identity,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            OutlinedButton(onClick = { viewModel.previousStep() }) {
                Text(stringResource(Res.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.nextStep() },
                enabled = state.selectedIdentity.isNotEmpty() &&
                        state.selectedInstallerIdentity.isNotEmpty()
            ) {
                Text(stringResource(Res.string.next))
            }
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
            stringResource(Res.string.step_4_title),
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
            label = { Text(stringResource(Res.string.issuer_id_label)) },
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
            label = { Text(stringResource(Res.string.key_id_label)) },
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
            label = { Text(stringResource(Res.string.api_key_path_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedButton(onClick = { viewModel.previousStep() }) {
                Text(stringResource(Res.string.back))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.nextStep() }) {
                Text(stringResource(Res.string.next))
            }
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
            stringResource(Res.string.step_5_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.isReleasing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            if (!state.isReleasing && !state.releaseSuccess) {
                OutlinedButton(onClick = { viewModel.previousStep() }) {
                    Text(stringResource(Res.string.back))
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Button(
                onClick = { viewModel.startRelease() },
                enabled = !state.isReleasing && !state.releaseSuccess
            ) {
                Text(
                    if (state.releaseSuccess)
                        stringResource(Res.string.released_successfully)
                    else
                        stringResource(Res.string.start_release),
                )
            }

            if (state.releaseSuccess || state.releaseError != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { viewModel.startOver() }) {
                    Text(stringResource(Res.string.start_over))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(Res.string.logs),
                style = MaterialTheme.typography.titleSmall
            )
            TextButton(onClick = { showLogs = !showLogs }) {
                Text(
                    if (showLogs) stringResource(Res.string.hide_details) else stringResource(
                        Res.string.show_details
                    )
                )
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
