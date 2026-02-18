package com.example.polyspace.ui.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.polyspace.ui.features.timetable.TimetableConfig
import com.example.polyspace.utils.TimetableCalculations
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun CurrentTimeLine(
    date: LocalDate,
    isSingleDayView: Boolean,
    hourHeight: Dp,
    shouldShowLine: Boolean
) {
    if (!shouldShowLine) return

    val nowTime = LocalTime.now()
    val today = LocalDate.now()

    if (nowTime.hour < TimetableConfig.START_HOUR || nowTime.hour > TimetableConfig.END_HOUR) return

    val centerY = TimetableCalculations.calculateTimeYOffset(nowTime, hourHeight)
    val barHeight = 2.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = centerY - (barHeight / 2))
            .height(barHeight),
        contentAlignment = Alignment.Center
    ) {
        if (date == today) {
            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(TimetableConfig.VIVID_RED))
        } else if (date.isBefore(today)) {
            if (!isSingleDayView) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(TimetableConfig.VIVID_RED.copy(alpha = 0.5f))
                )
            }
        }
    }
}