package com.example.polyspace.utils

import android.content.Context

fun clearGradesData(context: Context) {
    val prefs = context.getSharedPreferences("grades_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}

fun clearGradesCacheOnly(context: Context) {
    val prefs = context.getSharedPreferences("grades_prefs", Context.MODE_PRIVATE)
    prefs.edit().remove("cached_grades_json").apply()
}