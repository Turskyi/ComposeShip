package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ReleaseNotesSection(
    state: WearReleaseState,
    viewModel: WearReleaseViewModel,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = state.releaseNotes,
        onValueChange = { viewModel.onReleaseNotesChanged(it) },
        label = { Text("Release Notes (uploaded to Play)") },
        modifier = modifier.fillMaxWidth(),
        enabled = !state.buildInProgress
    )
}
