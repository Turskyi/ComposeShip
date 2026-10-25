package com.composeship.feature.macosrelease.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.back
import composeship.core.generated.resources.browse
import composeship.core.generated.resources.icon_path_label
import composeship.core.generated.resources.icon_selection_explanation
import composeship.core.generated.resources.next
import composeship.core.generated.resources.step_icon_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun IconSelectionStep(
    state: MacOsReleaseState,
    viewModel: MacOsReleaseViewModel
) {
    Column {
        Text(
            stringResource(Res.string.step_icon_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(Res.string.icon_selection_explanation),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.customIconPath,
                onValueChange = { viewModel.onIconPathChanged(it) },
                enabled = !state.isReleasing,
                label = { Text(stringResource(Res.string.icon_path_label)) },
                modifier = Modifier.weight(1f),
                isError = !state.isIconValid
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.onBrowseIcon() },
                enabled = !state.isReleasing,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(Res.string.browse))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
