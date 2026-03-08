package com.example.polyspace.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.FontWeight.Companion.Bold
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.polyspace.MainActivity
import com.example.polyspace.data.local.Prefs
import com.example.polyspace.data.models.CourseEvent
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class WidgetCourseData(
    val title: String,
    val room: String,
    val timeString: String,
    val isCurrent: Boolean
)

class TimetableWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Prefs.init(context)

            if (!Prefs.isLiveActivitiesEnabled()) {
                Text(
                    text = " ",
                    style = TextStyle(fontSize = 1.sp)
                )
            } else {
                val courseData = fetchWidgetData(context)
                WidgetUI(courseData)
            }
        }
    }

    private fun fetchWidgetData(context: Context): WidgetCourseData? {
        Prefs.init(context)

        if (!Prefs.isLiveActivitiesEnabled()) return null

        val resourceId = Prefs.getResourceId()
        val dateStr = LocalDate.now().toString()
        val cachedJson = Prefs.getTimetableCache(resourceId, dateStr) ?: return null

        try {
            val events = Json.decodeFromString<List<CourseEvent>>(cachedJson)
            val now = LocalDateTime.now()
            val sortedEvents = events.sortedBy { it.start }

            for (event in sortedEvents) {
                val startInstant = Instant.parse(event.start)
                val endInstant = Instant.parse(event.end)

                val start = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault())
                val end = LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault())

                val title = event.title?.ifBlank { "Cours" } ?: "Cours"
                val room = event.rooms.firstOrNull() ?: "Salle inconnue"

                val startStr = "${start.hour}h${start.minute.toString().padStart(2, '0')}"
                val endStr = "${end.hour}h${end.minute.toString().padStart(2, '0')}"
                val timeStr = "$startStr - $endStr"

                if (now.isAfter(start) && now.isBefore(end)) {
                    return WidgetCourseData(title, room, timeStr, isCurrent = true)
                } else if (start.isAfter(now)) {
                    return WidgetCourseData(title, room, timeStr, isCurrent = false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    @Composable
    private fun WidgetUI(data: WidgetCourseData?) {
        val baseColor = ColorProvider(day = Color.Black, night = Color.White)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.CenterStart
        ) {
            if (data == null) {
                Text(
                    text = " ",
                    style = TextStyle(
                        color = baseColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    modifier = GlanceModifier.padding(start = 12.dp)
                )
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .width(4.dp)
                            .height(60.dp)
                            .cornerRadius(2.dp)
                            .background(baseColor)
                    ) {}

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    Column(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = if (data.isCurrent) "EN COURS" else "PROCHAIN",
                            style = TextStyle(
                                color = baseColor,
                                fontWeight = Bold,
                                fontSize = 10.sp
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(2.dp))

                        Text(
                            text = data.title,
                            maxLines = 1,
                            style = TextStyle(
                                color = baseColor,
                                fontWeight = Bold,
                                fontSize = 14.sp
                            )
                        )

                        Text(
                            text = "${data.room} • ${data.timeString}",
                            maxLines = 1,
                            style = TextStyle(
                                color = baseColor,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}