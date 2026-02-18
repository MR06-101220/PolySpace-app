package com.example.polyspace.ui.features.grades.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polyspace.data.models.GradeClass
import java.util.Locale

@Composable
fun GradeItemCard(gradeClass: GradeClass) {
    var isExpanded by remember { mutableStateOf(false) }
    val myAverage = gradeClass.studentAverage
    val promoAverage = gradeClass.promoAverage
    val uniqueEvaluations = remember(gradeClass.evaluations) {
        gradeClass.evaluations.distinctBy {
            it.assignment + it.grade.toString() + it.date.toString()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize(animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gradeClass.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!isExpanded) {
                        val count = uniqueEvaluations.size
                        val coefInfo =
                            if (gradeClass.coefficient != null) " • Coef ${gradeClass.coefficient}" else ""

                        Text(
                            text = if (count > 0) "$count note(s)$coefInfo" else "Pas de notes$coefInfo",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                if (myAverage != null) {
                    ScoreBadge(score = myAverage)
                } else {
                    Text("-", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            if (myAverage != null) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (myAverage / 20.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = getGradeColor(myAverage),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (gradeClass.coefficient != null) InfoBadge(
                            icon = Icons.Default.Scale,
                            text = "Coef. ${gradeClass.coefficient}",
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (gradeClass.classRank != null) InfoBadge(
                            icon = Icons.Default.EmojiEvents,
                            text = "${gradeClass.classRank}${if (gradeClass.classRank == 1) "er" else "e"}${if (gradeClass.classRankTotal != null) "/${gradeClass.classRankTotal}" else ""}",
                            color = Color(0xFFFBC02D)
                        )
                        if (promoAverage != null) InfoBadge(
                            icon = Icons.Default.Groups,
                            text = "Promo: ${String.format(Locale.US, "%.2f", promoAverage)}",
                            color = Color.Gray
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(12.dp))

                    if (uniqueEvaluations.isEmpty()) {
                        Text(
                            "Aucun détail disponible.",
                            fontStyle = FontStyle.Italic,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    } else {
                        uniqueEvaluations.forEach { evaluation ->
                            EvaluationRow(evaluation)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}