package com.composeship.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeship.core.ui.TechnicalGridBackground
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.get_started
import composeship.core.generated.resources.onboarding_description
import composeship.core.generated.resources.onboarding_title
import composeship.core.generated.resources.skip
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingScreen(
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TechnicalGridBackground()
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.9f).widthIn(max = 600.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 8.dp,
                    border = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) null
                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.onboarding_title),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(Res.string.onboarding_description),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(Res.string.skip))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = onDismiss) {
                                Text(stringResource(Res.string.get_started))
                            }
                        }
                    }
                }
            }
        }
    }
}
