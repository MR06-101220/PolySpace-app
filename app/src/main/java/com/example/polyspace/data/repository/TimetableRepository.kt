package com.example.polyspace.data.repository

import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.data.remote.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class TimetableRepository {
    suspend fun fetchTimetable(
        resourceId: String,
        resourceType: String,
        date: LocalDate,
        forceRefresh: Boolean = false
    ): List<CourseEvent> = withContext(Dispatchers.IO) {
        val dateString = date.toString()
        if (resourceType == "PROMO") {
            NetworkModule.api.getTimetableByPromo(
                promoName = resourceId,
                date = dateString,
                force = if (forceRefresh) true else null
            )
        } else {
            NetworkModule.api.getTimetableById(
                resourceId = resourceId,
                date = dateString,
                force = if (forceRefresh) true else null
            )
        }
    }
}