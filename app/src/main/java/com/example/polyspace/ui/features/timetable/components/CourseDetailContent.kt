package com.example.polyspace.ui.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.utils.parseColorSafe
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CourseDetailContent(event: CourseEvent) {
    val rawColor = parseColorSafe(event.colorHex) ?: MaterialTheme.colorScheme.primary
    val backgroundTint = rawColor.copy(alpha = 0.12f)
    val contentColor = rawColor

    val zdtStart = ZonedDateTime.parse(event.start, DateTimeFormatter.ISO_DATE_TIME)
        .withZoneSameInstant(ZoneId.systemDefault())
    val zdtEnd = ZonedDateTime.parse(event.end, DateTimeFormatter.ISO_DATE_TIME)
        .withZoneSameInstant(ZoneId.systemDefault())

    val startLocal = zdtStart.toLocalTime()
    val endLocal = zdtEnd.toLocalTime()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val duration = Duration.between(startLocal, endLocal)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val durationText = when {
        hours == 0L -> "${minutes} min"
        minutes == 0L -> "${hours}h"
        else -> "${hours}h${if (minutes < 10) "0$minutes" else minutes}"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 50.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundTint)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(contentColor)
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    event.type?.let { type ->
                        Surface(
                            color = contentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = type.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = contentColor.copy(alpha = 0.9f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = event.title ?: "Cours sans titre",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                DetailItem(
                    icon = Icons.Default.Schedule,
                    title = "Horaire",
                    content = "${startLocal.format(timeFormatter)} - ${endLocal.format(timeFormatter)}",
                    iconColor = contentColor,
                    modifier = Modifier.weight(1f)
                )

                DetailItem(
                    icon = Icons.Default.Timer,
                    title = "Durée",
                    content = durationText,
                    iconColor = contentColor,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            if (event.rooms.isNotEmpty()) {
                DetailRowFull(
                    icon = Icons.Default.LocationOn,
                    title = "Salle de cours",
                    content = event.rooms.joinToString(", "),
                    iconColor = contentColor
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (event.teachers.isNotEmpty()) {
                DetailRowFull(
                    icon = Icons.Default.Person,
                    title = "Enseignant",
                    content = event.teachers.joinToString(", "),
                    iconColor = contentColor
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (event.groups.isNotEmpty()) {
                DetailRowFull(
                    icon = Icons.Default.Group,
                    title = "Groupes concernés",
                    content = event.groups.joinToString(", "),
                    iconColor = contentColor
                )
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    title: String,
    content: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailRowFull(icon: ImageVector, title: String, content: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HorizontalDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 1.dp
    )
}