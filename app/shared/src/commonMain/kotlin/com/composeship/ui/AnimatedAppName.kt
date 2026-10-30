package com.composeship.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import composeship.core.generated.resources.Res
import composeship.core.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

@Composable
fun AnimatedAppName() {
    val transition = rememberInfiniteTransition(label = "appNameGradient")
    val gradientOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "appNameGradientOffset"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary
        ),
        start = Offset(1000f * gradientOffset, 0f),
        end = Offset(1000f * (gradientOffset + 1f), 100f)
    )

    Text(
        text = stringResource(Res.string.app_name),
        style = MaterialTheme.typography.titleLarge.copy(brush = brush)
    )
}
