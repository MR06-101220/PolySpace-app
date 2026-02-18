package com.example.polyspace.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.ui.features.timetable.TimetableConfig
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PositionData(val topOffset: Dp, val height: Dp)

object TimetableCalculations {

    fun calculateTimeYOffset(time: LocalTime, hourHeight: Dp): Dp {
        if (time.hour < TimetableConfig.START_HOUR || time.hour > TimetableConfig.END_HOUR) return 0.dp

        val totalMinutes = ((time.hour - TimetableConfig.START_HOUR) * 60) + time.minute
        return ((totalMinutes.toFloat() / 60f) * hourHeight.value).dp
    }

    fun calculateEventPosition(event: CourseEvent, hourHeight: Dp): PositionData {
        val zdtStart = java.time.ZonedDateTime.parse(event.start, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())
        val zdtEnd = java.time.ZonedDateTime.parse(event.end, DateTimeFormatter.ISO_DATE_TIME)
            .withZoneSameInstant(ZoneId.systemDefault())

        val start = zdtStart.toLocalTime()
        val end = zdtEnd.toLocalTime()

        val startMinutes = ((start.hour - TimetableConfig.START_HOUR) * 60) + start.minute
        val endMinutes = ((end.hour - TimetableConfig.START_HOUR) * 60) + end.minute
        val safeStartMinutes = startMinutes.coerceAtLeast(0)

        val topOffset = (safeStartMinutes.toFloat() / 60f) * hourHeight.value
        val durationMinutes = endMinutes - startMinutes
        val height = (durationMinutes.toFloat() / 60f) * hourHeight.value

        return PositionData(topOffset.dp, height.dp)
    }
}