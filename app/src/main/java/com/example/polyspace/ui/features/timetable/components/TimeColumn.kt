package com.example.polyspace.ui.features.timetable.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.polyspace.ui.features.timetable.TimetableConfig
import com.example.polyspace.utils.TimetableCalculations
import java.time.LocalTime

@Composable
fun TimeLabelsColumnContent(
    hourHeight: Dp,
    totalContentHeight: Dp,
    showCurrentTime: Boolean
) {
    val now = LocalTime.now()
    val currentPillY = if (now.hour in TimetableConfig.START_HOUR..TimetableConfig.END_HOUR) {
        TimetableCalculations.calculateTimeYOffset(now, hourHeight)
    } else {
        (-1000).dp
    }

    Box(modifier = Modifier.height(totalContentHeight)) {
        for (i in 0..(TimetableConfig.END_HOUR - TimetableConfig.START_HOUR)) {
            val hour = TimetableConfig.START_HOUR + i
            val staticLabelY = (hourHeight * i).value.dp
            val diff = (currentPillY - staticLabelY).value
            val distance = if (diff < 0) -diff else diff
            val isOverlapped = showCurrentTime && (distance < 20.0)

            val alpha by animateFloatAsState(
                targetValue = if (isOverlapped) 0f else 1f,
                label = "opacity_$hour",
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )

            Box(
                modifier = Modifier
                    .height(hourHeight)
                    .offset(y = hourHeight * i),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "${hour}h",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    modifier = Modifier.offset(y = (-8).dp).padding(start = 8.dp)
                )
            }
        }

        if (showCurrentTime) {
            CurrentTimePill(hourHeight = hourHeight)
        }
    }
}