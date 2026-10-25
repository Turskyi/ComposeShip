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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.browse
import composeship.core.generated.resources.next
import composeship.core.generated.resources.project_root_label
import composeship.core.generated.resources.step_1_title
import org.jetbrains.compose.resources.stringResource

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
                enabled = !state.isReleasing,
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
                enabled = !state.isReleasing,
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
