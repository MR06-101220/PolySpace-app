package com.example.polyspace.ui.features.timetable.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.utils.parseColorSafe
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun GridCourseCard(
    event: CourseEvent,
    compactMode: Boolean,
    badgeCount: Int = 0,
    hourHeight: Dp,
    onClick: () -> Unit
) {
    val rawColor = parseColorSafe(event.colorHex) ?: MaterialTheme.colorScheme.primary
    val backgroundTint = rawColor.copy(alpha = 0.12f)
    val barColor = rawColor

    val zdtStart = java.time.ZonedDateTime.parse(event.start, DateTimeFormatter.ISO_DATE_TIME)
        .withZoneSameInstant(ZoneId.systemDefault())
    val zdtEnd = java.time.ZonedDateTime.parse(event.end, DateTimeFormatter.ISO_DATE_TIME)
        .withZoneSameInstant(ZoneId.systemDefault())
    val startLocal = zdtStart.toLocalTime()
    val endLocal = zdtEnd.toLocalTime()

    val durationInMinutes = java.time.Duration.between(zdtStart, zdtEnd).toMinutes()
    val isVeryShort = durationInMinutes <= 30

    val isZoomedOut = hourHeight < 60.dp
    val isZoomedIn = hourHeight > 110.dp

    val titleSize = if (compactMode) 11.sp else 12.sp
    val detailSize = if (compactMode) 9.sp else 10.sp

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundTint),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(barColor))

                Column(
                    modifier = Modifier
                        .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 2.dp)
                        .fillMaxSize(),
                    verticalArrangement = if (isVeryShort) Arrangement.Center else Arrangement.Top
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.title ?: "Cours",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (isZoomedOut) 1 else 3,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = titleSize,
                            lineHeight = titleSize * 1.1,
                            modifier = Modifier.weight(1f)
                        )

                        if (event.type != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.type,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = barColor,
                                fontSize = if (compactMode) 9.sp else 10.sp
                            )
                        }
                    }

                    if (!isZoomedOut && !isVeryShort) {
                        Spacer(modifier = Modifier.height(2.dp))

                        val roomText = event.rooms.joinToString(", ").ifEmpty { "Salle inconnue" }
                        Text(
                            text = roomText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 1,
                            fontSize = detailSize,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isZoomedIn) {
                            Spacer(modifier = Modifier.height(2.dp))
                            if (event.teachers.isNotEmpty()) {
                                Text(
                                    text = event.teachers.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    fontSize = detailSize,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${startLocal.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${
                                endLocal.format(DateTimeFormatter.ofPattern("HH:mm"))
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            }
        }
    }
}