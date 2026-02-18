package com.example.polyspace.utils

import androidx.compose.ui.graphics.Color

fun parseColorSafe(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        null
    }
}