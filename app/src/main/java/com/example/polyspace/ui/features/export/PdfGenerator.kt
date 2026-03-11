package com.example.polyspace.ui.features.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object PdfGenerator {

    private const val PAGE_WIDTH = 3508
    private const val PAGE_HEIGHT = 2480

    private const val START_HOUR = 8
    private const val END_HOUR = 20

    private const val MARGIN_LEFT = 250f
    private const val MARGIN_TOP = 250f
    private const val MARGIN_RIGHT = 100f
    private const val MARGIN_BOTTOM = 100f

    fun generateTimetablePdf(context: Context, courses: List<DraftCourse>, weekStart: java.time.LocalDate, studentName: String): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val bgPaint = Paint().apply { color = Color.WHITE }
        val linePaint = Paint().apply { color = Color.parseColor("#E0E0E0"); strokeWidth = 4f }
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 45f; typeface = Typeface.DEFAULT_BOLD }
        val timePaint = Paint().apply { color = Color.BLACK; textSize = 35f; textAlign = Paint.Align.RIGHT }

        val headerPaint = Paint().apply { color = Color.BLACK; textSize = 60f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.LEFT }

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        canvas.drawText("Emploi du temps - $studentName", MARGIN_LEFT, 120f, headerPaint)

        val gridWidth = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
        val gridHeight = PAGE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM
        val columnWidth = gridWidth / 5f
        val rowHeight = gridHeight / (END_HOUR - START_HOUR)

        val days = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi")

        for (i in 0..5) {
            val x = MARGIN_LEFT + (i * columnWidth)
            canvas.drawLine(x, MARGIN_TOP, x, PAGE_HEIGHT - MARGIN_BOTTOM, linePaint)

            if (i < 5) {
                val currentDay = weekStart.plusDays(i.toLong())
                val dateStr = currentDay.format(DateTimeFormatter.ofPattern("dd/MM"))
                val dayX = x + (columnWidth / 2f)
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("${days[i]} $dateStr", dayX, MARGIN_TOP - 40f, textPaint)
            }
        }

        for (i in 0..(END_HOUR - START_HOUR)) {
            val y = MARGIN_TOP + (i * rowHeight)
            canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, linePaint)
            canvas.drawText("${START_HOUR + i}h00", MARGIN_LEFT - 30f, y + 12f, timePaint)
        }

        for (course in courses) {
            try {
                val startLocal = ZonedDateTime.parse(course.start).withZoneSameInstant(java.time.ZoneId.systemDefault())
                val endLocal = ZonedDateTime.parse(course.end).withZoneSameInstant(java.time.ZoneId.systemDefault())

                val dayOfWeek = startLocal.dayOfWeek.value
                if (dayOfWeek > 5) continue

                val colIndex = dayOfWeek - 1
                val xStart = MARGIN_LEFT + (colIndex * columnWidth) + 6f
                val xEnd = MARGIN_LEFT + ((colIndex + 1) * columnWidth) - 6f

                val startHourFloat = startLocal.hour + (startLocal.minute / 60f)
                val endHourFloat = endLocal.hour + (endLocal.minute / 60f)

                if (startHourFloat < START_HOUR || endHourFloat > END_HOUR) continue

                val yStart = MARGIN_TOP + ((startHourFloat - START_HOUR) * rowHeight)
                val yEnd = MARGIN_TOP + ((endHourFloat - START_HOUR) * rowHeight)

                val courseColor = try { Color.parseColor(course.colorHex) } catch (e: Exception) { Color.BLUE }
                val rectPaint = Paint().apply { color = courseColor }
                val rectF = RectF(xStart, yStart, xEnd, yEnd)
                canvas.drawRoundRect(rectF, 24f, 24f, rectPaint)

                val textWidth = (xEnd - xStart - 30f).toInt()
                if (textWidth > 0 && (yEnd - yStart) > 60f) {
                    val timeStr = "${startLocal.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${endLocal.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                    val room = course.rooms.firstOrNull() ?: ""
                    val teachers = course.teachers.joinToString(", ")

                    val content = buildString {
                        append(course.title)
                        if (!course.type.isNullOrBlank()) append(" [${course.type}]")
                        append("\n$timeStr")
                        if (room.isNotBlank()) append("\n$room")
                        if (teachers.isNotBlank()) append("\n$teachers")
                    }

                    val tp = android.text.TextPaint().apply {
                        color = Color.WHITE
                        textSize = 38f
                        isAntiAlias = true
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    val staticLayout = android.text.StaticLayout.Builder.obtain(content, 0, content.length, tp, textWidth)
                        .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(0f, 1f)
                        .build()

                    canvas.save()
                    canvas.translate(xStart + 15f, yStart + 15f)
                    canvas.clipRect(0f, 0f, textWidth.toFloat(), (yEnd - yStart - 30f))
                    staticLayout.draw(canvas)
                    canvas.restore()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        document.finishPage(page)

        return try {
            val file = File(context.cacheDir, "Emploi_du_temps_Export.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }
}