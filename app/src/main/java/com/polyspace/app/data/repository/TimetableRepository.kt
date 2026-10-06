package com.polyspace.app.data.repository

import com.polyspace.app.data.local.Prefs
import com.polyspace.app.data.models.CourseEvent
import com.polyspace.app.data.remote.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

            val mostFrequentGroup = networkEvents
                .flatMap { it.groups }
                .filter { it.isNotBlank() }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key

            if (mostFrequentGroup != null) {
                Prefs.saveUserGroup(mostFrequentGroup)
            }

            val uniqueCourseTitles = networkEvents.mapNotNull { it.title }.distinct()

            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                uniqueCourseTitles.forEach { title ->
                    val cachedSymbol = Prefs.getCourseSymbol(title)
                    if (cachedSymbol == null) {
                        try {
                            val response = NetworkModule.api.fetchSymbol(courseName = title)
                            val symbolId = response.symbolId

                            if (symbolId != null) {
                                Prefs.saveCourseSymbol(title, symbolId)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        networkEvents
    }
}