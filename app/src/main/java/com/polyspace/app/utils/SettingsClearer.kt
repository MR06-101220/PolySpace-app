package com.polyspace.app.utils

import android.content.Context
import com.polyspace.app.data.local.Prefs

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