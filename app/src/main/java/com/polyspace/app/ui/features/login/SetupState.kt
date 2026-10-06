package com.polyspace.app.ui.features.login

import com.polyspace.app.data.models.Promo         // Vérifie l'import
import com.polyspace.app.data.models.AdeResource   // Vérifie l'import

sealed interface SetupUiState {
    data object Idle : SetupUiState
    data object Loading : SetupUiState
    data class PromosLoaded(val promos: List<Promo>) : SetupUiState
    data class SearchResults(val results: List<AdeResource>) : SetupUiState
    data class Error(val message: String) : SetupUiState
}