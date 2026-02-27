package com.example.polyspace.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.viewModelScope
import com.example.polyspace.MainActivity
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
import com.example.polyspace.widget.TimetableWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                subtitle = "Version 2.8.5 • Développé par MR06",
                onClick = { showAboutDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SettingsSectionTitle("Widget")

            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            var isLiveActivitiesEnabled by remember {
                mutableStateOf(Prefs.isLiveActivitiesEnabled())
            }

            Surface(
                onClick = {
                    val newValue = !isLiveActivitiesEnabled
                    isLiveActivitiesEnabled = newValue
                    Prefs.setLiveActivitiesEnabled(newValue)

                    coroutineScope.launch {
                        delay(300)
                        val intent = Intent(context, com.example.polyspace.MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        context.startActivity(intent)
                        Runtime.getRuntime().exit(0)
                    }
                },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cours",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Afficher/Cacher les widgets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "(Attention : L'application va redémarrer)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Switch(
                        checked = isLiveActivitiesEnabled,
                        onCheckedChange = {
                            isLiveActivitiesEnabled = it
                            Prefs.setLiveActivitiesEnabled(it)

                            coroutineScope.launch {
                                delay(300)
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                context.startActivity(intent)
                                Runtime.getRuntime().exit(0)
                            }
                        }
                    )
                }
            }

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