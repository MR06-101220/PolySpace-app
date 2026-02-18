package com.example.polyspace.ui.features.grades.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.polyspace.data.models.GradeSemester
import java.util.Locale

@Composable
fun SemesterDashboard(semester: GradeSemester) {
    val targetScore = semester.average?.toFloat() ?: 0f
    var showStats by remember { mutableStateOf(false) }
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(targetScore) {
        animatedScore.animateTo(
            targetScore,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { showStats = true }
                    .padding(12.dp)
            ) {
                AnimatedCircularGauge(
                    score = animatedScore.value,
                    maxScore = 20f,
                    radius = 50.dp,
                    strokeWidth = 10.dp,
                    color = getDashboardGradeColor(targetScore.toDouble())
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.2f", animatedScore.value),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "/20",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Equalizer,
                            "Stats",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Moyenne Générale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = getAppreciation(targetScore.toDouble()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    if (showStats) SemesterStatsDialog(semester = semester, onDismiss = { showStats = false })
}

@Composable
fun SemesterStatsDialog(semester: GradeSemester, onDismiss: () -> Unit) {
    val validClasses = semester.classes.filter { (it.studentAverage ?: 0.0) > 0.0 }
    val bestClass = validClasses.maxByOrNull { it.studentAverage ?: 0.0 }
    val worstClass = validClasses.minByOrNull { it.studentAverage ?: 0.0 }
    val validatedCount = validClasses.count { (it.studentAverage ?: 0.0) >= 10.0 }
    val totalCount = validClasses.size

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Analyse du Semestre",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(24.dp))
                if (validClasses.isEmpty()) {
                    Text("Pas assez de données.", color = Color.Gray)
                } else {
                    if (bestClass != null) {
                        StatRow(
                            icon = Icons.Default.EmojiEvents,
                            iconColor = Color(0xFFFBC02D),
                            label = "Meilleure performance",
                            value = bestClass.name,
                            score = bestClass.studentAverage
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                    if (worstClass != null && worstClass != bestClass) {
                        StatRow(
                            icon = Icons.Default.TrendingDown,
                            iconColor = MaterialTheme.colorScheme.error,
                            label = "À surveiller",
                            value = worstClass.name,
                            score = worstClass.studentAverage
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Matières validées", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text(
                                text = "$validatedCount / $totalCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (validatedCount == totalCount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (validatedCount > totalCount / 2) Icons.Default.ThumbUp else Icons.Default.PriorityHigh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Fermer") }
            }
        }
    }
}

@Composable
fun StatRow(icon: ImageVector, iconColor: Color, label: String, value: String, score: Double?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = iconColor.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        if (score != null) {
            ScoreBadgePill(score = score)
        }
    }
}

@Composable
fun ScoreBadgePill(score: Double) {
    val gradeColor = getDashboardGradeColor(score)

    Surface(
        color = gradeColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, gradeColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = String.format(Locale.US, "%.2f", score),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = gradeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getDashboardGradeColor(average: Double): Color {
    return when {
        average >= 16 -> Color(0xFF2E7D32)
        average >= 14 -> Color(0xFF43A047)
        average >= 12 -> Color(0xFF66BB6A)
        average >= 10 -> Color(0xFFFFA726)
        average >= 8 -> Color(0xFFFF7043)
        else -> Color(0xFFEF5350)
    }
}

private fun getAppreciation(score: Double): String = when {
    score >= 16 -> "Excellent travail ! 🚀"
    score >= 14 -> "Très bons résultats"
    score >= 12 -> "Bon semestre"
    score >= 10 -> "Semestre validé"
    score >= 8 -> "Un peu juste..."
    else -> "Attention aux rattrapages"
}