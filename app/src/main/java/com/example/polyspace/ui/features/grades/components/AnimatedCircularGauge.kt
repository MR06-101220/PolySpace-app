package com.example.polyspace.ui.features.grades.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
fun AnimatedCircularGauge(
    score: Float,
    maxScore: Float,
    radius: Dp,
    strokeWidth: Dp,
    color: Color
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = Modifier.size(radius * 2)) {
        drawArc(
            color = bg,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = (score / maxScore) * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}