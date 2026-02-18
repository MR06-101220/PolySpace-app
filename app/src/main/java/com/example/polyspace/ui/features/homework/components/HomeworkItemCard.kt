package com.example.polyspace.ui.features.homework.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.polyspace.data.models.Homework
import com.example.polyspace.data.models.Priority
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeworkItemCard(
    homework: Homework,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Homework) -> Unit,
    onEdit: () -> Unit
) {
    val totalSubTasks = homework.subTasks.size
    val completedSubTasks = homework.subTasks.count { it.isDone }
    val progress = if (totalSubTasks > 0) completedSubTasks.toFloat() / totalSubTasks else 0f

    val priorityColor = when (homework.priority) {
        Priority.LOW -> Color(0xFF81C784)
        Priority.NORMAL -> Color(0xFF64B5F6)
        Priority.URGENT -> Color(0xFFE57373)
    }

    val isOverdue =
        homework.dueDate != null && homework.dueDate < LocalDate.now() && !homework.isDone
    val isToday = homework.dueDate == LocalDate.now()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.75f,
                    stiffness = 600f
                )
            )
            .clickable { onExpandClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (isExpanded) 6.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = if (homework.isDone) Color.Gray.copy(alpha = 0.3f) else priorityColor,
                        topLeft = Offset.Zero,
                        size = Size(
                            width = 6.dp.toPx(),
                            height = size.height
                        )
                    )
                }
                .padding(start = 6.dp)
                .padding(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (homework.isDone) Color.LightGray.copy(alpha = 0.3f) else priorityColor.copy(
                        alpha = 0.15f
                    ),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = homework.subject ?: "Autre",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (homework.isDone) Color.Gray else priorityColor.copy(alpha = 1f)
                            .compositeOver(Color.Black),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (homework.dueDate != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isToday) "Aujourd'hui" else homework.dueDate.format(
                                DateTimeFormatter.ofPattern(
                                    "dd MMM"
                                )
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isOverdue || isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (homework.isDone) TextDecoration.LineThrough else null,
                    color = if (homework.isDone) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onToggleDone, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (homework.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Fait",
                        tint = if (homework.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (totalSubTasks > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = priorityColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$completedSubTasks/$totalSubTasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (homework.description.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Default.Notes,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = homework.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (homework.subTasks.isNotEmpty()) {
                        homework.subTasks.forEachIndexed { index, subTask ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newSubTasks = homework.subTasks.toMutableList()
                                        newSubTasks[index] = subTask.copy(isDone = !subTask.isDone)
                                        onUpdate(homework.copy(subTasks = newSubTasks))
                                    }
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (subTask.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (subTask.isDone) priorityColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = subTask.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (subTask.isDone) TextDecoration.LineThrough else null,
                                    color = if (subTask.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.7f
                                    ) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    var showLocalDeleteConfirm by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Modifier")
                        }
                        TextButton(
                            onClick = { showLocalDeleteConfirm = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Supprimer")
                        }
                    }

                    if (showLocalDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showLocalDeleteConfirm = false },
                            title = { Text("Supprimer ?") },
                            text = { Text("Voulez-vous vraiment supprimer ce devoir ?") },
                            confirmButton = {
                                TextButton(
                                    onClick = { onDelete(); showLocalDeleteConfirm = false },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) { Text("Supprimer") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showLocalDeleteConfirm = false
                                }) { Text("Annuler") }
                            }
                        )
                    }
                }
            }
        }
    }
}