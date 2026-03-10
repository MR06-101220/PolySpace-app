package com.example.polyspace.core

import kotlinx.coroutines.flow.MutableSharedFlow

object GlobalEvents {
    // Clear grades cache
    val clearGradesCacheEvent = MutableSharedFlow<Unit>(replay = 0)

    // Clear Timetable cache
}