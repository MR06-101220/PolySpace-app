package com.example.polyspace.data.repository

import com.example.polyspace.data.local.Prefs
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.data.remote.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class TimetableRepository {

    suspend fun getCachedTimetable(resourceId: String, date: LocalDate): List<CourseEvent> = withContext(Dispatchers.IO) {
        val cachedJson = Prefs.getTimetableCache(resourceId, date.toString())
        if (cachedJson != null) {
            try {
                Json.decodeFromString<List<CourseEvent>>(cachedJson)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun fetchTimetable(
        resourceId: String,
        resourceType: String,
        date: LocalDate,
        forceRefresh: Boolean = false
    ): List<CourseEvent> = withContext(Dispatchers.IO) {
        val dateString = date.toString()


        val networkEvents = if (resourceType == "PROMO") {
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

        if (networkEvents.isNotEmpty()) {
            val json = Json.encodeToString(networkEvents)
            Prefs.saveTimetableCache(resourceId, dateString, json)
        }

        networkEvents
    }
}