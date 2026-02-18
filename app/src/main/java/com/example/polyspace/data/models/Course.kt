package com.example.polyspace.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// --- API ---

@Serializable
@Parcelize
data class CourseEvent(
    val title: String? = "Cours sans titre",
    val start: String, // Format: "2025-09-01T08:00:00Z"
    val end: String,
    val type: String? = null,
    val teachers: List<String> = emptyList(),
    val rooms: List<String> = emptyList(),
    val groups: List<String> = emptyList(),
    val colorHex: String? = "#CCCCCC"
) : Parcelable

// Helper for API
data class DaySchedule(
    val date: String,
    val events: List<CourseEvent>
)