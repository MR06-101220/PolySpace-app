package com.example.polyspace.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.polyspace.data.models.Homework
import com.example.polyspace.ui.features.homework.HomeworkViewModel
import com.example.polyspace.ui.features.homework.SortType
import com.example.polyspace.ui.features.homework.components.AddHomeworkDialog
import com.example.polyspace.ui.features.homework.components.HomeworkItemCard
import com.example.polyspace.ui.features.homework.components.SortChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(
    viewModel: HomeworkViewModel,
    availableSubjects: List<String>
) {
    val currentSort by viewModel.sortType.collectAsState()
    val homeworkList by viewModel.allHomeworks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var homeworkEditing by remember { mutableStateOf<Homework?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Devoirs",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortChip(
                        label = "Date",
                        selected = currentSort == SortType.DATE,
                        onClick = { viewModel.setSortType(SortType.DATE) }
                    )
                    SortChip(
                        label = "Priorité",
                        selected = currentSort == SortType.PRIORITY,
                        onClick = { viewModel.setSortType(SortType.PRIORITY) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (homeworkList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aucun devoir pour le moment 🎉",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                var expandedHomeworkId by remember { mutableStateOf<Long?>(null) }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(homeworkList, key = { it.id }) { homework ->
                        val isExpanded = expandedHomeworkId == homework.id

                        Box(
                            modifier = Modifier
                                .zIndex(if (isExpanded) 1f else 0f)
                                .animateItem(placementSpec = tween(durationMillis = 300))
                        ) {
                            HomeworkItemCard(
                                homework = homework,
                                isExpanded = isExpanded,
                                onExpandClick = {
                                    expandedHomeworkId = if (isExpanded) null else homework.id
                                },
                                onToggleDone = { viewModel.toggleHomeworkDone(homework) },
                                onDelete = { viewModel.deleteHomework(homework) },
                                onUpdate = { viewModel.updateHomework(it) },
                                onEdit = { homeworkEditing = homework }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }
        }

        if (showAddDialog) {
            AddHomeworkDialog(
                availableSubjects = availableSubjects,
                homeworkToEdit = null,
                onDismiss = { showAddDialog = false },
                onSave = { viewModel.addHomework(it); showAddDialog = false }
            )
        }

        homeworkEditing?.let { hw ->
            AddHomeworkDialog(
                availableSubjects = availableSubjects,
                homeworkToEdit = hw,
                onDismiss = { homeworkEditing = null },
                onSave = {
                    viewModel.updateHomework(it)
                    homeworkEditing = null
                }
            )
        }
    }
}