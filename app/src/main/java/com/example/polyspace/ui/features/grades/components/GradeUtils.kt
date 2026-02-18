package com.example.polyspace.ui.features.grades.components

import androidx.compose.ui.graphics.Color

fun getGradeColor(score: Double): Color = when {
    score >= 16 -> Color(0xFF2E7D32)
    score >= 14 -> Color(0xFF43A047)
    score >= 12 -> Color(0xFF66BB6A)
    score >= 10 -> Color(0xFFFFA726)
    score >= 8 -> Color(0xFFFF7043)
    else -> Color(0xFFEF5350)
}