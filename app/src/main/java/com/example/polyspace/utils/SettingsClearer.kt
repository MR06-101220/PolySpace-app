package com.example.polyspace.utils

import android.content.Context
import com.example.polyspace.data.local.Prefs

fun clearGradesData(context: Context) {
    val prefs = context.getSharedPreferences("grades_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}

fun clearGradesCacheOnly(context: Context) {
    val prefs = context.getSharedPreferences("grades_prefs", Context.MODE_PRIVATE)
    prefs.edit().remove("cached_grades_json").apply()
}

fun clearAllAppCache(context: Context) {
    clearGradesCacheOnly(context)
    Prefs.clearTimetableCache()
}