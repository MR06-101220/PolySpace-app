package com.example.polyspace.ui.features.setup.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.polyspace.ui.features.setup.ErrorState
import com.example.polyspace.ui.features.timetable.SetupUiState
import com.example.polyspace.ui.features.timetable.TimetableViewModel

@Composable
fun PromoListSection(
    state: SetupUiState,
    viewModel: TimetableViewModel,
    onComplete: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is SetupUiState.PromosLoaded -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Sélectionne ta promotion",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                    }
                    items(state.promos) { promo ->
                        SetupItemCard(
                            title = promo.name,
                            subtitle = "Emploi du temps complet",
                            icon = Icons.Default.School,
                            onClick = {
                                viewModel.saveConfiguration("PROMO", promo.name, promo.name)
                                onComplete()
                            }
                        )
                    }
                }
            }

            is SetupUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is SetupUiState.Error -> ErrorState(message = state.message)
            else -> LaunchedEffect(Unit) { viewModel.loadPromos() }
        }
    }
}