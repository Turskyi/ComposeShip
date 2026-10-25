package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.wear_keystore_alias_info_text
import composeship.core.generated.resources.wear_keystore_alias_info_title
import composeship.core.generated.resources.wear_keystore_alias_label
import composeship.core.generated.resources.wear_keystore_path_info_text
import composeship.core.generated.resources.wear_keystore_path_info_title
import composeship.core.generated.resources.wear_keystore_path_label
import composeship.core.generated.resources.wear_package_name_info_text
import composeship.core.generated.resources.wear_package_name_info_title
import composeship.core.generated.resources.wear_package_name_label
import composeship.core.generated.resources.wear_service_account_info_text
import composeship.core.generated.resources.wear_service_account_info_title
import composeship.core.generated.resources.wear_service_account_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun CredentialsSection(
    state: WearReleaseState,
    viewModel: WearReleaseViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.serviceAccountPath,
                onValueChange = { viewModel.onServiceAccountPathChanged(it) },
                label = { Text(stringResource(Res.string.wear_service_account_label)) },
                modifier = Modifier.weight(1f),
                enabled = !state.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_service_account_info_title),
                text = stringResource(Res.string.wear_service_account_info_text)
            )
        }
        Button(
            onClick = { viewModel.onBrowseServiceAccount() },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !state.buildInProgress
        ) { Text("Browse JSON") }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.packageName,
                onValueChange = { viewModel.onPackageNameChanged(it) },
                label = { Text(stringResource(Res.string.wear_package_name_label)) },
                modifier = Modifier.weight(1f),
                enabled = !state.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_package_name_info_title),
                text = stringResource(Res.string.wear_package_name_info_text)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.keystorePath,
                onValueChange = { viewModel.onKeystorePathChanged(it) },
                label = { Text(stringResource(Res.string.wear_keystore_path_label)) },
                modifier = Modifier.weight(1f),
                enabled = !state.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_keystore_path_info_title),
                text = stringResource(Res.string.wear_keystore_path_info_text)
            )
        }
        Button(
            onClick = { viewModel.onBrowseKeystore() },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !state.buildInProgress
        ) { Text("Browse Keystore") }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.keystoreAlias,
                onValueChange = { viewModel.onKeystoreAliasChanged(it) },
                label = { Text(stringResource(Res.string.wear_keystore_alias_label)) },
                modifier = Modifier.weight(1f),
                enabled = !state.buildInProgress
            )
            InfoButton(
                title = stringResource(Res.string.wear_keystore_alias_info_title),
                text = stringResource(Res.string.wear_keystore_alias_info_text)
            )
        }
    }
}
