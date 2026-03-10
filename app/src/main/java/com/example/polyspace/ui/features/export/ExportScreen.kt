package com.example.polyspace.ui.features.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.polyspace.data.local.Prefs
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onBack: () -> Unit
) {
    val draftCourses by viewModel.draftCourses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentWeekStart by viewModel.currentWeekStart.collectAsState()
    val context = LocalContext.current

    val allTemplates by viewModel.allTemplates.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<DraftCourse?>(null) }

    val groupedCourses = remember(draftCourses) {
        draftCourses.groupBy {
            try { ZonedDateTime.parse(it.start).toLocalDate() } catch (e: Exception) { LocalDate.now() }
        }.toSortedMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Préparer l'export", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val userName = Prefs.getDisplayName()
                        val pdfFile = PdfGenerator.generateTimetablePdf(context, draftCourses, currentWeekStart, userName)

                        if (pdfFile != null) {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", pdfFile
                            )
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Partager l'emploi du temps"))
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Terminer")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    courseToEdit = null
                    showEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .navigationBarsPadding()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            WeekNavigationHeader(
                currentWeekStart = currentWeekStart,
                onPrevious = { viewModel.previousWeek() },
                onNext = { viewModel.nextWeek() }
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    groupedCourses.forEach { (date, coursesForDay) ->
                        item { DaySeparator(date) }
                        items(coursesForDay) { course ->
                            DraftCourseItem(course = course) {
                                courseToEdit = course
                                showEditDialog = true
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showEditDialog) {
        DraftCourseDialog(
            initialCourse = courseToEdit,
            weekStart = currentWeekStart,

            existingCourses = allTemplates,

            onDismiss = { showEditDialog = false },
            onSave = { updatedOrNewCourse ->
                if (courseToEdit == null) viewModel.addCustomCourse(updatedOrNewCourse)
                else viewModel.updateCourse(updatedOrNewCourse)
                showEditDialog = false
            },
            onDelete = {
                courseToEdit?.let { viewModel.deleteCourse(it.id) }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun WeekNavigationHeader(currentWeekStart: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, "Précédent") }
        Text(
            text = "Semaine du ${currentWeekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRANCE))}",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, "Suivant") }
    }
}

@Composable
fun DaySeparator(date: LocalDate) {
    val dayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRANCE)
    Column {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = date.format(dayFormatter).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}