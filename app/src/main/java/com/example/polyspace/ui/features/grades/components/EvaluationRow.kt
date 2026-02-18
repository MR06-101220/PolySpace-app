package com.example.polyspace.ui.features.grades.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyspace.data.models.GradeEvaluation
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EvaluationRow(evaluation: GradeEvaluation) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evaluation.assignment,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(evaluation.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (evaluation.grade != null) ScoreBadge(score = evaluation.grade) else Text(
                "N.C",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        if (evaluation.classAverage != null || evaluation.rank != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (evaluation.classAverage != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Groups,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Moy: ${
                                String.format(
                                    Locale.US,
                                    "%.2f",
                                    evaluation.classAverage
                                )
                            }", style = MaterialTheme.typography.labelSmall, color = Color.Gray
                        )
                    }
                }
                if (evaluation.rank != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFBC02D)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${evaluation.rank}${if (evaluation.rank == 1) "er" else "ème"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        if (evaluation.totalPeople != null && evaluation.totalPeople > 0) Text(
                            text = "/${evaluation.totalPeople}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}