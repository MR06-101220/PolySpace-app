package com.example.polyspace.ui.features.login

import com.example.polyspace.data.models.Promo         // Vérifie l'import
import com.example.polyspace.data.models.AdeResource   // Vérifie l'import

sealed interface SetupUiState {
    data object Idle : SetupUiState
    data object Loading : SetupUiState
    data class PromosLoaded(val promos: List<Promo>) : SetupUiState
    data class SearchResults(val results: List<AdeResource>) : SetupUiState
    data class Error(val message: String) : SetupUiState
}