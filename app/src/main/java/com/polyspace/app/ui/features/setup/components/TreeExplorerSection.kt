package com.polyspace.app.ui.features.setup.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polyspace.app.ui.features.setup.EmptyState
import com.polyspace.app.ui.features.setup.ErrorState
import com.polyspace.app.ui.features.timetable.TimetableViewModel

@Composable
fun TreeExplorerSection(
    viewModel: TimetableViewModel,
    onSetupComplete: () -> Unit
) {
    val treeHistory by viewModel.treeHistory.collectAsState()
    val isLoading by viewModel.isTreeLoading.collectAsState()
    val errorMessage by viewModel.treeError.collectAsState()

    val currentLevel = treeHistory.lastOrNull()
    val currentParent = currentLevel?.parentNode
    val currentNodes = currentLevel?.items ?: emptyList()
    val canGoBack = treeHistory.size > 1

    BackHandler(enabled = canGoBack) {
        viewModel.navigateBackInTree()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canGoBack) {
                IconButton(onClick = { viewModel.navigateBackInTree() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = currentParent?.label ?: "Dossiers ADE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (canGoBack) 4.dp else 8.dp)
            )

            if (currentParent != null && currentParent.id.isNotBlank() && currentParent.id != "-100") {
                FilledTonalButton(
                    onClick = {
                        viewModel.selectTreeNode(currentParent)
                        onSetupComplete()
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choisir ce dossier", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ErrorState(errorMessage ?: "Erreur inconnue")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadTreeRoot(force = true) }) {
                            Text("Réessayer")
                        }
                    }
                }
                currentNodes.isEmpty() -> {
                    EmptyState(Icons.Default.FolderOpen, "Ce dossier est vide.")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(currentNodes, key = { it.id + it.label }) { node ->
                            SetupItemCard(
                                title = node.label,
                                subtitle = if (node.hasChildren) {
                                    if (node.childCount > 0) "${node.childCount} éléments" else "Dossier"
                                } else {
                                    "Sélectionner (ID : ${node.id})"
                                },
                                icon = if (node.hasChildren) Icons.Default.Folder else Icons.Default.Person,
                                onClick = {
                                    if (node.hasChildren) {
                                        viewModel.openTreeNode(node)
                                    } else {
                                        viewModel.selectTreeNode(node)
                                        onSetupComplete()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}