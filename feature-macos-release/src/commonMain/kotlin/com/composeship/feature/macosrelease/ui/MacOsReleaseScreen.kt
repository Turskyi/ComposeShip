package com.composeship.feature.macosrelease.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.macos_release_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun MacOsReleaseScreen(
    viewModel: MacOsReleaseViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.step != ReleaseStep.SelectProject && !state.isReleasing) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                    Text(
                        text = stringResource(Res.string.macos_release_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    }
                ) { step ->
                    when (step) {
                        ReleaseStep.SelectProject -> ProjectSelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.SelectTask -> TaskSelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.AppCategory -> CategorySelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.SigningIdentity -> IdentitySelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.AppStoreCredentials -> CredentialsStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.SelectIcon -> IconSelectionStep(
                            state,
                            viewModel
                        )

                        ReleaseStep.Process -> ReleaseProcessStep(
                            state,
                            viewModel
                        )
                    }
                }
            }
        }
    }
}
