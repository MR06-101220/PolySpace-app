package com.example.polyspace.ui.features.grades

import com.example.polyspace.data.models.PolyGradeOverview

sealed interface GradesState {
    data object Loading : GradesState
    data object LoginRequired : GradesState
    data class Success(val overview: PolyGradeOverview) : GradesState
    data class Error(val message: String) : GradesState
}

enum class UpdateStatus { IDLE, LOADING, SUCCESS, ERROR }