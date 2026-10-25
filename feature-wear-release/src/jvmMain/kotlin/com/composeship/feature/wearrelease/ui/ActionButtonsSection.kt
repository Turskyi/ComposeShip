package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ActionButtonsSection(
    state: WearReleaseState,
    viewModel: WearReleaseViewModel,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { viewModel.buildWear(state.projectRoot) },
            enabled = state.isProjectValid && !state.buildInProgress
        ) {
            Text("Build Wear AAB")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { viewModel.generateReleaseNotesFromGit(state.projectRoot) },
            enabled = !state.buildInProgress
        ) {
            Text("Generate Release Notes (git)")
        }
    }
}
