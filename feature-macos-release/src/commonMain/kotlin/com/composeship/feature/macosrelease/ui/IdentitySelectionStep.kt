package com.composeship.feature.macosrelease.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.app_identity_label
import composeship.core.generated.resources.back
import composeship.core.generated.resources.identity_app_store_desc
import composeship.core.generated.resources.identity_developer_id_desc
import composeship.core.generated.resources.installer_identity_explanation
import composeship.core.generated.resources.installer_identity_label
import composeship.core.generated.resources.next
import composeship.core.generated.resources.no_app_identities
import composeship.core.generated.resources.no_installer_identities
import composeship.core.generated.resources.open_apple_developer
import composeship.core.generated.resources.refresh
import composeship.core.generated.resources.step_3_title
import org.jetbrains.compose.resources.stringResource

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

        if (state.isLoadingIdentities) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }

        Text(
            stringResource(Res.string.app_identity_label),
            style = MaterialTheme.typography.titleSmall
        )
        if (state.signingIdentities.isEmpty() && !state.isLoadingIdentities) {
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
                onClick = { if (!state.isReleasing) viewModel.onIdentitySelected(identity) },
                enabled = !state.isReleasing,
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
                        enabled = !state.isReleasing,
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
        if (state.installerIdentities.isEmpty() && !state.isLoadingIdentities) {
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
                    onClick = { if (!state.isReleasing) viewModel.onInstallerIdentitySelected(identity) },
                    enabled = !state.isReleasing,
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
                            enabled = !state.isReleasing,
                            onClick = {
                                viewModel.onInstallerIdentitySelected(
                                    identity
                                )
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
