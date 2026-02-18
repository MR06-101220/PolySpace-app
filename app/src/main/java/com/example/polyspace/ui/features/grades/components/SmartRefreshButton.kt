package com.example.polyspace.ui.features.grades.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.polyspace.ui.features.grades.UpdateStatus

@Composable
fun SmartRefreshButton(
    status: UpdateStatus,
    onRefresh: () -> Unit
) {
    val backgroundColor = when (status) {
        UpdateStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
        UpdateStatus.SUCCESS -> Color(0xFFE8F5E9)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when (status) {
        UpdateStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        UpdateStatus.SUCCESS -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    IconButton(
        onClick = onRefresh,
        enabled = status != UpdateStatus.LOADING,
        modifier = Modifier
            .background(backgroundColor, CircleShape)
            .animateContentSize()
    ) {
        Crossfade(
            targetState = status,
            label = "refreshIcon"
        ) { currentStatus ->
            when (currentStatus) {
                UpdateStatus.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }

                UpdateStatus.SUCCESS -> {
                    Icon(Icons.Default.Check, "À jour", tint = contentColor)
                }

                UpdateStatus.ERROR -> {
                    Icon(Icons.Default.PriorityHigh, "Erreur", tint = contentColor)
                }

                else -> {
                    Icon(Icons.Default.Refresh, "Actualiser", tint = contentColor)
                }
            }
        }
    }
}