package com.example.polyspace.ui.features.export

import com.example.polyspace.data.models.CourseEvent
import java.util.UUID

data class DraftCourse(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: String?,
    val rooms: List<String>,
    val start: String,
    val end: String,
    val teachers: List<String>,
    val colorHex: String
) {
    companion object {

        fun fromCourseEvent(event: CourseEvent): DraftCourse {
            return DraftCourse(
                title = event.title ?: "Cours",
                type = event.type,
                rooms = event.rooms,
                start = event.start,
                end = event.end,
                teachers = event.teachers,
                colorHex = event.colorHex ?: "#1976D2"
            )
        }
    }
}