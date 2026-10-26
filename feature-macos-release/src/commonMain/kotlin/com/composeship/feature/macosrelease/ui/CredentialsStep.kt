package com.composeship.feature.macosrelease.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.api_key_link_text
import composeship.core.generated.resources.api_key_path_help
import composeship.core.generated.resources.api_key_path_label
import composeship.core.generated.resources.back
import composeship.core.generated.resources.browse
import composeship.core.generated.resources.error_invalid_issuer_id
import composeship.core.generated.resources.error_invalid_key_id
import composeship.core.generated.resources.issuer_id_help
import composeship.core.generated.resources.issuer_id_label
import composeship.core.generated.resources.key_id_help
import composeship.core.generated.resources.key_id_label
import composeship.core.generated.resources.next
import composeship.core.generated.resources.step_4_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    val uriHandler = LocalUriHandler.current
    val apiPageUrl = "https://appstoreconnect.apple.com/access/integrations/api"

    Column {
        Text(
            stringResource(Res.string.step_4_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        AppStorePrerequisitesCard()

        Spacer(modifier = Modifier.height(16.dp))

        CredentialField(
            value = state.apiIssuerId,
            onValueChange = {
                viewModel.onCredentialsChanged(
                    it,
                    state.apiKeyId,
                    state.apiKeyPath
                )
            },
            enabled = !state.isReleasing,
            label = stringResource(Res.string.issuer_id_label),
            helpText = stringResource(Res.string.issuer_id_help),
            isError = !state.isIssuerIdValid,
            errorText = stringResource(Res.string.error_invalid_issuer_id),
            onActionClick = { uriHandler.openUri(apiPageUrl) },
            actionLabel = stringResource(Res.string.api_key_link_text)
        )

        CredentialField(
            value = state.apiKeyId,
            onValueChange = {
                viewModel.onCredentialsChanged(
                    state.apiIssuerId,
                    it,
                    state.apiKeyPath
                )
            },
            enabled = !state.isReleasing,
            label = stringResource(Res.string.key_id_label),
            helpText = stringResource(Res.string.key_id_help),
            isError = !state.isKeyIdValid,
            errorText = stringResource(Res.string.error_invalid_key_id),
            onActionClick = { uriHandler.openUri(apiPageUrl) },
            actionLabel = stringResource(Res.string.api_key_link_text)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                CredentialField(
                    value = state.apiKeyPath,
                    onValueChange = {
                        viewModel.onCredentialsChanged(
                            state.apiIssuerId,
                            state.apiKeyId,
                            it
                        )
                    },
                    enabled = !state.isReleasing,
                    label = stringResource(Res.string.api_key_path_label),
                    helpText = stringResource(Res.string.api_key_path_help),
                    isError = false,
                    errorText = ""
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.onBrowseApiKey() },
                enabled = !state.isReleasing,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(Res.string.browse))
            }
        }

        if (state.detectedApiKeyFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (state.detectedApiKeyFiles.size == 1) "Auto-detected key:" else "Multiple keys found — select one to fill fields:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.detectedApiKeyFiles.forEach { path ->
                    val filename = path.substringAfterLast("/")
                    val labelText = getUniquePathLabel(path, state.detectedApiKeyFiles)
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = {
                            PlainTooltip {
                                Text(path)
                            }
                        },
                        state = rememberTooltipState()
                    ) {
                        FilterChip(
                            selected = state.apiKeyPath == path,
                            onClick = {
                                val keyIdMatch =
                                    Regex("AuthKey_([A-Z0-9]{10})\\.p8").find(
                                        filename
                                    )
                                val detectedKeyId = keyIdMatch?.groupValues?.get(1)
                                    ?: state.apiKeyId
                                viewModel.onCredentialsChanged(
                                    state.apiIssuerId,
                                    detectedKeyId,
                                    path
                                )
                            },
                            label = {
                                Text(
                                    labelText,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
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

private fun getUniquePathLabel(path: String, allPaths: List<String>): String {
    val filename = path.substringAfterLast("/")
    val parts = path.split("/").filter { it.isNotEmpty() }
    if (parts.isEmpty()) return path

    for (takeLastCount in 1..parts.size) {
        val suffix = parts.takeLast(takeLastCount).joinToString("/")
        val matchingCount = allPaths.count { p ->
            p.split("/").filter { it.isNotEmpty() }.takeLast(takeLastCount).joinToString("/") == suffix
        }
        if (matchingCount == 1) {
            return suffix
        }
    }
    return filename
}
