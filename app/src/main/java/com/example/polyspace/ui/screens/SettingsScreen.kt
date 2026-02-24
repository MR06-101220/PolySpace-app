package com.example.polyspace.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.polyspace.core.GlobalEvents
import com.example.polyspace.data.local.Prefs
import com.example.polyspace.ui.features.settings.components.AboutDialog
import com.example.polyspace.ui.features.settings.components.CustomAlertDialog
import com.example.polyspace.ui.features.settings.components.DangerTile
import com.example.polyspace.ui.features.settings.components.IconSelector
import com.example.polyspace.ui.features.settings.components.ProfileHeaderCard
import com.example.polyspace.ui.features.settings.components.SettingsSectionTitle
import com.example.polyspace.ui.features.settings.components.SettingsTile
import com.example.polyspace.ui.features.timetable.TimetableViewModel
import com.example.polyspace.utils.clearAllAppCache
import com.example.polyspace.utils.clearGradesCacheOnly
import com.example.polyspace.utils.clearGradesData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TimetableViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentResource by viewModel.currentResource.collectAsState()
    val scrollState = rememberScrollState()

    var showGradesLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearSubjectsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Paramètres",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {

            ProfileHeaderCard(
                name = currentResource.name,
                type = currentResource.type
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionTitle("Général")

            SettingsTile(
                icon = Icons.Outlined.Info,
                title = "À propos",
                subtitle = "Version 2.7.3 • Développé par MR06",
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))


            SettingsSectionTitle("Personnalisation")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                IconSelector(context)
            }

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionTitle("Dépannage")

            SettingsTile(
                icon = Icons.Default.CleaningServices,
                title = "Vider le cache de l'application",
                subtitle = "Résout les bugs d'affichage (Notes & Emploi du temps)",
                onClick = {
                    clearAllAppCache(context)

                    viewModel.viewModelScope.launch {
                        GlobalEvents.clearGradesCacheEvent.emit(Unit)
                    }
                    viewModel.manualRefresh()

                    Toast.makeText(context, "Cache vidé !", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsTile(
                icon = Icons.Default.DeleteSweep,
                title = "Réinitialiser la liste des matières",
                subtitle = "Si la liste de choix des devoirs est polluée",
                onClick = { showClearSubjectsDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionTitle("Gestion du compte")

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DangerTile(
                    icon = Icons.Outlined.School,
                    title = "Déconnecter les Notes",
                    subtitle = "Efface les identifiants Oasis enregistrés",
                    onClick = { showGradesLogoutDialog = true }
                )

                DangerTile(
                    icon = Icons.Outlined.Logout,
                    title = "Changer d'Emploi du temps",
                    subtitle = "Retourner à la sélection (Promo/Étudiant)",
                    onClick = onLogout
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "PolySpace 2026",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showGradesLogoutDialog) {
        CustomAlertDialog(
            title = "Déconnexion",
            text = "Voulez-vous vraiment effacer vos identifiants Oasis ? Vous ne pourrez plus voir vos notes sans vous reconnecter.",
            confirmText = "Déconnecter",
            cancelText = "Annuler",
            isDestructive = true,
            onConfirm = {
                clearGradesData(context)
                Toast.makeText(context, "Notes déconnectées.", Toast.LENGTH_SHORT).show()
                showGradesLogoutDialog = false
            },
            onDismiss = { showGradesLogoutDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showClearSubjectsDialog) {
        CustomAlertDialog(
            title = "Réinitialiser les matières ?",
            text = "Cela effacera la liste des matières suggérées lors de la création d'un devoir. Elle se remplira à nouveau automatiquement en parcourant votre emploi du temps.",
            confirmText = "Effacer",
            cancelText = "Annuler",
            isDestructive = true,
            onConfirm = {
                Prefs.clearSubjects()
                viewModel.refreshSubjects()
                Toast.makeText(context, "Liste des matières effacée.", Toast.LENGTH_SHORT).show()
                showClearSubjectsDialog = false
            },
            onDismiss = { showClearSubjectsDialog = false }
        )
    }
}