package com.example.polyspace.ui.features.export

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DraftCourseItem(course: DraftCourse, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val startTime = try { ZonedDateTime.parse(course.start).withZoneSameInstant(ZoneId.systemDefault()).format(formatter) } catch (e: Exception) { "??:??" }
    val endTime = try { ZonedDateTime.parse(course.end).withZoneSameInstant(ZoneId.systemDefault()).format(formatter) } catch (e: Exception) { "??:??" }

    val color = try { Color(android.graphics.Color.parseColor(course.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(50)).background(color))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = course.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1, modifier = Modifier.weight(1f))
                if (!course.type.isNullOrBlank()) {
                    Text(text = course.type, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = color, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Text(text = "${course.rooms.firstOrNull() ?: "Salle inconnue"} • $startTime - $endTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DraftCourseDialog(
    initialCourse: DraftCourse?,
    weekStart: LocalDate,
    existingCourses: List<DraftCourse>,
    onDismiss: () -> Unit,
    onSave: (DraftCourse) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = initialCourse != null

    var title by remember { mutableStateOf(initialCourse?.title ?: "") }
    var room by remember { mutableStateOf(initialCourse?.rooms?.firstOrNull() ?: "") }
    var teacher by remember { mutableStateOf(initialCourse?.teachers?.firstOrNull() ?: "") }
    var type by remember { mutableStateOf(initialCourse?.type ?: "CM") }
    var colorHex by remember { mutableStateOf(initialCourse?.colorHex ?: "#1E88E5") }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    var startTimeStr by remember { mutableStateOf(if (isEditMode) ZonedDateTime.parse(initialCourse!!.start).withZoneSameInstant(ZoneId.systemDefault()).format(timeFormatter) else "08:00") }
    var endTimeStr by remember { mutableStateOf(if (isEditMode) ZonedDateTime.parse(initialCourse!!.end).withZoneSameInstant(ZoneId.systemDefault()).format(timeFormatter) else "10:00") }

    var selectedDayOffset by remember { mutableStateOf(if (isEditMode) ZonedDateTime.parse(initialCourse!!.start).withZoneSameInstant(ZoneId.systemDefault()).dayOfWeek.value - 1 else 0) }

    val types = listOf("CM", "TD", "TP", "Exam")
    val palette = listOf("#E53935", "#1E88E5", "#43A047", "#FDD835", "#8E24AA", "#F4511E", "#3949AB", "#00ACC1")
    val uniqueTemplates = remember(existingCourses) { existingCourses.distinctBy { it.title } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "Modifier le cours" else "Nouveau cours") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isEditMode && uniqueTemplates.isNotEmpty()) {
                    Text("Auto-remplissage :", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uniqueTemplates.size) { index ->
                            val template = uniqueTemplates[index]
                            FilterChip(
                                selected = false,
                                onClick = {
                                    title = template.title
                                    room = template.rooms.firstOrNull() ?: ""
                                    teacher = template.teachers.firstOrNull() ?: ""
                                    type = template.type ?: "CM"
                                    colorHex = template.colorHex
                                },
                                label = { Text(template.title.take(15) + "...") }
                            )
                        }
                    }
                    HorizontalDivider()
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Matière") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Salle") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Professeur") }, singleLine = true, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = startTimeStr, onValueChange = {}, readOnly = true, label = { Text("Début") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.matchParentSize().clickable {
                            val parts = startTimeStr.split(":")
                            TimePickerDialog(context, { _, h, m ->
                                startTimeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                            }, parts[0].toInt(), parts[1].toInt(), true).show()
                        })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = endTimeStr, onValueChange = {}, readOnly = true, label = { Text("Fin") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.matchParentSize().clickable {
                            val parts = endTimeStr.split(":")
                            TimePickerDialog(context, { _, h, m ->
                                endTimeStr = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                            }, parts[0].toInt(), parts[1].toInt(), true).show()
                        })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    types.forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                    }
                }

                Text("Couleur de la matière :", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    palette.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { colorHex = hex }
                                .then(if (colorHex == hex) Modifier.background(Color.Black.copy(alpha = 0.3f)) else Modifier)
                        )
                    }
                }

                if (!isEditMode) {
                    Text("Jour de la semaine :", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Lun", "Mar", "Mer", "Jeu", "Ven").forEachIndexed { index, day ->
                            FilterChip(
                                selected = selectedDayOffset == index,
                                onClick = { selectedDayOffset = index },
                                label = { Text(day, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val targetDate = weekStart.plusDays(selectedDayOffset.toLong())
                        val st = LocalTime.parse(startTimeStr, timeFormatter)
                        val et = LocalTime.parse(endTimeStr, timeFormatter)

                        val startZdt = ZonedDateTime.of(targetDate, st, ZoneId.systemDefault())
                        val endZdt = ZonedDateTime.of(targetDate, et, ZoneId.systemDefault())

                        val newCourse = initialCourse?.copy(
                            title = title.ifBlank { "Cours" },
                            type = type,
                            rooms = if (room.isNotBlank()) listOf(room) else emptyList(),
                            teachers = if (teacher.isNotBlank()) listOf(teacher) else emptyList(),
                            colorHex = colorHex,
                            start = startZdt.toString(),
                            end = endZdt.toString()
                        ) ?: DraftCourse(
                            title = title.ifBlank { "Nouveau Cours" },
                            type = type,
                            rooms = if (room.isNotBlank()) listOf(room) else emptyList(),
                            teachers = if (teacher.isNotBlank()) listOf(teacher) else emptyList(),
                            colorHex = colorHex,
                            start = startZdt.toString(),
                            end = endZdt.toString()
                        )
                        onSave(newCourse)
                    } catch (e: Exception) { e.printStackTrace() }
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            if (isEditMode) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Supprimer") }
            } else {
                TextButton(onClick = onDismiss) { Text("Annuler") }
            }
        }
    )
}