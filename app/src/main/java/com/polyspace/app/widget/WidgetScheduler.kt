package com.polyspace.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.polyspace.app.data.models.CourseEvent
import java.time.Instant

object WidgetScheduler {
    fun scheduleWidgetUpdates(context: Context, events: List<CourseEvent>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetUpdateReceiver::class.java)

        val updateTimes = mutableSetOf<Long>()
        events.forEach { event ->
            try {
                val startInstant = Instant.parse(event.start)
                val endInstant = Instant.parse(event.end)

                updateTimes.add(startInstant.toEpochMilli())
                updateTimes.add(endInstant.toEpochMilli())

                val thirtyMinsBefore = startInstant.minusSeconds(30 * 60)
                updateTimes.add(thirtyMinsBefore.toEpochMilli())

            } catch (e: Exception) {}
        }

        val now = System.currentTimeMillis()

        updateTimes.filter { it > now }.forEachIndexed { index, timeInMillis ->
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                index, // Un ID unique par alarme
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        }
    }
}