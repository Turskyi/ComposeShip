package com.composeship.core.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun TechnicalGridBackground(
    modifier: Modifier = Modifier,
    majorGridInterval: Int = 5,
    cellSize: Float = 30f,
    showRuler: Boolean = true,
    animateDrawIn: Boolean = true,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Sampled colors from the provided reference images
    val backgroundColor = if (isDark) Color(0xFF1A4594) else Color(0xFFFFFDF6)
    val majorGridColor = if (isDark) Color(0xFF5891E5) else Color(0xFFF28B82)
    val minorGridColor = majorGridColor.copy(alpha = 0.25f)

    val infiniteTransition = rememberInfiniteTransition(label = "TechnicalGridTransition")
    
    // Ambient pan duration: 60 seconds per cell. 
    // Total cycle for major grid (5 cells) is 5 minutes.
    val panDurationMillis = 300000 
    
    val panOffsetState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = cellSize * majorGridInterval,
        animationSpec = infiniteRepeatable(
            animation = tween(panDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GridPanOffset"
    )

    val drawInProgress = remember { Animatable(if (animateDrawIn) 0f else 1f) }
    LaunchedEffect(animateDrawIn) {
        if (animateDrawIn) {
            drawInProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)
        
        val pan = panOffsetState.value
        val progress = drawInProgress.value

        drawGridLines(
            cellSize = cellSize,
            majorGridInterval = majorGridInterval,
            majorColor = majorGridColor,
            minorColor = minorGridColor,
            pan = pan,
            progress = progress
        )

        if (showRuler) {
            drawRulers(majorGridColor, progress)
        }
        
        drawCornerElements(majorGridColor, progress)
    }
}

private fun DrawScope.drawGridLines(
    cellSize: Float,
    majorGridInterval: Int,
    majorColor: Color,
    minorColor: Color,
    pan: Float,
    progress: Float
) {
    val width = size.width
    val height = size.height
    
    val majorStroke = 1.2.dp.toPx()
    val minorStroke = 0.5.dp.toPx()

    // Vertical lines: mathematically continuous panning
    // We calculate the range of line indices 'n' that are currently visible
    val startNX = floor((-cellSize - pan) / cellSize).toInt()
    val endNX = ceil((width + cellSize - pan) / cellSize).toInt()
    
    for (n in startNX..endNX) {
        val x = n * cellSize + pan
        val isMajor = n % majorGridInterval == 0
        
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = Offset(x, 0f),
            end = Offset(x, height * progress),
            strokeWidth = if (isMajor) majorStroke else minorStroke
        )
    }

    // Horizontal lines
    val startNY = floor((-cellSize - pan) / cellSize).toInt()
    val endNY = ceil((height + cellSize - pan) / cellSize).toInt()
    
    for (n in startNY..endNY) {
        val y = n * cellSize + pan
        val isMajor = n % majorGridInterval == 0
        
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = Offset(0f, y),
            end = Offset(width * progress, y),
            strokeWidth = if (isMajor) majorStroke else minorStroke
        )
    }
}

private fun DrawScope.drawRulers(color: Color, progress: Float) {
    val tickLength = 8.dp.toPx()
    val tickSpacing = 10.dp.toPx()
    val majorTickEvery = 5
    val strokeWidth = 1.dp.toPx()
    
    // Top ruler
    val topRulerLimit = size.width * progress
    val topCount = (topRulerLimit / tickSpacing).toInt()
    for (i in 0..topCount) {
        val x = i * tickSpacing
        val isMajorTick = i % majorTickEvery == 0
        val length = if (isMajorTick) tickLength else tickLength / 2
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, length),
            strokeWidth = strokeWidth
        )
    }
    
    // Left ruler
    val leftRulerLimit = size.height * progress
    val leftCount = (leftRulerLimit / tickSpacing).toInt()
    for (i in 0..leftCount) {
        val y = i * tickSpacing
        val isMajorTick = i % majorTickEvery == 0
        val length = if (isMajorTick) tickLength else tickLength / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(length, y),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawCornerElements(color: Color, progress: Float) {
    val margin = 4.dp.toPx()
    val squareSize = 8.dp.toPx() * progress
    
    // Corner squares
    val corners = listOf(
        Offset(margin, margin),
        Offset(size.width - margin - squareSize, margin),
        Offset(margin, size.height - margin - squareSize),
        Offset(size.width - margin - squareSize, size.height - margin - squareSize)
    )
    
    corners.forEach { topLeft ->
        drawRect(color = color, topLeft = topLeft, size = Size(squareSize, squareSize))
    }
}
