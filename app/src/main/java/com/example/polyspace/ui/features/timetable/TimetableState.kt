package com.example.polyspace.ui.features.timetable

import com.example.polyspace.data.models.AdeResource
import com.example.polyspace.data.models.Promo
import java.time.LocalDate

sealed interface TimetableUiState {
    data object Loading : TimetableUiState
    data class Success(val date: LocalDate) : TimetableUiState
    data class Error(val message: String) : TimetableUiState
}

sealed interface SetupUiState {
    data object Idle : SetupUiState
    data object Loading : SetupUiState
    data class PromosLoaded(val promos: List<Promo>) : SetupUiState
    data class SearchResults(val results: List<AdeResource>) : SetupUiState
    data class Error(val message: String) : SetupUiState
}

data class CurrentResource(
    val type: String,
    val id: String,
    val name: String,
    val isTemporary: Boolean = false
)