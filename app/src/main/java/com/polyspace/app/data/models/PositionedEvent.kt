package com.polyspace.app.data.models

/**
 * Represnet event disposed in Timetable grid
 * Used to manage conflicts beetwen events
 */
data class PositionedEvent(
    val event: CourseEvent,
    val colIndex: Int,
    val totalColumns: Int
)