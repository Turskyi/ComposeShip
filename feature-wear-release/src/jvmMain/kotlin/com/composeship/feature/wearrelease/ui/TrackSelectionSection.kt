package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun TrackSelectionSection(
    state: WearReleaseState,
    viewModel: WearReleaseViewModel,
    modifier: Modifier = Modifier
) {
    var trackExpanded by remember { mutableStateOf(false) }
    val tracks = listOf("internal", "alpha", "beta", "production")

    Box(modifier = modifier) {
        Button(
            onClick = { trackExpanded = true },
            enabled = !state.buildInProgress
        ) {
            Text("Track: ${state.track}")
        }
        DropdownMenu(
            expanded = trackExpanded,
            onDismissRequest = { trackExpanded = false }
        ) {
            tracks.forEach { t ->
                DropdownMenuItem(
                    text = { Text(t) },
                    onClick = {
                        viewModel.onTrackChanged(t)
                        trackExpanded = false
                    }
                )
            }
        }
    }
}
