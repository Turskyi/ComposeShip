package com.composeship.feature.wearrelease.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.wear_release_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun WearReleaseScreen(
    viewModel: WearReleaseViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val scrollState: ScrollState = rememberScrollState()

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
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                    )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(Res.string.wear_release_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.buildInProgress) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                }

                ProjectRootSection(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                ActionButtonsSection(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                ReleaseNotesSection(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                CredentialsSection(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                TrackSelectionSection(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(16.dp))

                ActionControlSection(state = state, viewModel = viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                LogsSection(state = state)
            }
        }
    }
}
