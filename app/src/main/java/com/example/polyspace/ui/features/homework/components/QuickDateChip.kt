package com.example.polyspace.ui.features.homework.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.time.LocalDate

@Composable
fun QuickDateChip(
    label: String,
    date: LocalDate,
    selectedDate: LocalDate?,
    onSelect: (LocalDate) -> Unit
) {
    val isSelected = selectedDate == date
    FilterChip(
        selected = isSelected,
        onClick = { onSelect(date) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}