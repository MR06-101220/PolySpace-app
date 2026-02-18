package com.example.polyspace.ui.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.ui.features.timetable.TimetableConfig
import com.example.polyspace.ui.features.timetable.TimetableViewModel
import com.example.polyspace.utils.TimetableCalculations
import java.time.LocalDate

@Composable
fun DayCompleteColumn(
    modifier: Modifier,
    date: LocalDate,
    viewModel: TimetableViewModel,
    cacheVersion: Int,
    verticalScrollState: androidx.compose.foundation.ScrollState,
    compactMode: Boolean,
    homeworkCounts: Map<String, Int>,
    onCourseClick: (CourseEvent) -> Unit,
    targetDaysVisible: Float,
    hourHeight: Dp,
    totalContentHeight: Dp,
    isScrollEnabled: Boolean,
    isTodayVisible: Boolean
) {
    LaunchedEffect(date) { viewModel.ensureDateLoaded(date) }
    val positionedEvents = viewModel.getEventsForDate(date)
    val isSingleDayView = targetDaysVisible <= 1.1f

    Column(
        modifier = modifier
            .fillMaxHeight()
            .drawBehind {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.05f),
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        DayHeader(date = date)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(
                    state = verticalScrollState,
                    enabled = isScrollEnabled
                )
        ) {
            Column {
                Spacer(modifier = Modifier.height(TimetableConfig.TOP_SCROLL_PADDING))

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(totalContentHeight)) {
                    for (i in 0..(TimetableConfig.END_HOUR - TimetableConfig.START_HOUR)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .offset(y = hourHeight * i)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                        )
                    }

                    positionedEvents.forEach { positionedEvent ->
                        val pos = TimetableCalculations.calculateEventPosition(positionedEvent.event, hourHeight)
                        val widthFraction = 1f / positionedEvent.totalColumns
                        val badgeCount = homeworkCounts.entries.find { (subject, count) ->
                            positionedEvent.event.title?.contains(subject, ignoreCase = true) == true
                        }?.value ?: 0

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(widthFraction)
                                .height(pos.height)
                                .offset(y = pos.topOffset)
                                .align(Alignment.TopStart)
                        ) {
                            Layout(
                                content = {
                                    Box(
                                        modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = 2.dp)
                                    ) {
                                        GridCourseCard(
                                            event = positionedEvent.event,
                                            compactMode = compactMode,
                                            badgeCount = badgeCount,
                                            hourHeight = hourHeight,
                                            onClick = {
                                                viewModel.onEventSelected(positionedEvent.event)
                                                onCourseClick(positionedEvent.event)
                                            }
                                        )
                                    }
                                }
                            ) { measurables, constraints ->
                                val placeable = measurables.first().measure(constraints)
                                val xOffset = (constraints.maxWidth * positionedEvent.colIndex)
                                layout(constraints.maxWidth, placeable.height) {
                                    placeable.place(xOffset, 0)
                                }
                            }
                        }
                    }

                    CurrentTimeLine(
                        date = date,
                        isSingleDayView = isSingleDayView,
                        hourHeight = hourHeight,
                        shouldShowLine = isTodayVisible
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}