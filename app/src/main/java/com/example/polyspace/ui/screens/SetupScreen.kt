package com.example.polyspace.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.polyspace.ui.features.setup.components.PromoListSection
import com.example.polyspace.ui.features.setup.components.StudentSearchSection
import com.example.polyspace.ui.features.timetable.TimetableViewModel

@Composable
fun SetupScreen(viewModel: TimetableViewModel = viewModel(), onSetupComplete: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val setupState by viewModel.setupState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenue ! 👋",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Configurons ton espace Polytech.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = { Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0; viewModel.loadPromos() },
                text = { Text("Par Promo", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.School, null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    viewModel.clearSetupState()
                          },
                text = { Text("Par Étudiant", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Person, null) }
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally { width -> if (targetState > initialState) width else -width } togetherWith
                            fadeOut(animationSpec = tween(300)) + slideOutHorizontally { width -> if (targetState > initialState) -width else width }
                },
                label = "TabContent"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> PromoListSection(setupState, viewModel, onSetupComplete)
                    1 -> StudentSearchSection(
                        searchQuery,
                        { searchQuery = it },
                        setupState,
                        viewModel,
                        focusManager,
                        onSetupComplete
                    )
                }
            }
        }
    }
}