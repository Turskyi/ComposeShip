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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.back
import composeship.core.generated.resources.next
import composeship.core.generated.resources.step_2_title
import composeship.core.generated.resources.task_debug_desc
import composeship.core.generated.resources.task_release_desc
import org.jetbrains.compose.resources.stringResource

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
            val isRelease = task.contains("Release", ignoreCase = true)
            val descRes =
                if (isRelease) Res.string.task_release_desc else Res.string.task_debug_desc

            Surface(
                onClick = {
                    if (!state.isReleasing) {
                        viewModel.onTaskSelected(task)
                        if (!isRelease) showDebugWarning = true
                    }
                },
                enabled = !state.isReleasing,
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
                        enabled = !state.isReleasing,
                        onClick = {
                            viewModel.onTaskSelected(task)
                            if (!isRelease) showDebugWarning = true
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = task,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (!isRelease && state.selectedTask == task) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(descRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showDebugWarning && !state.selectedTask.contains(
                "Release",
                ignoreCase = true
            )
        ) {
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
