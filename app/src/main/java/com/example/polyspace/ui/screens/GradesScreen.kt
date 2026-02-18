package com.example.polyspace.ui.screens

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.polyspace.data.models.PolyGradeOverview
import com.example.polyspace.ui.features.grades.GradesState
import com.example.polyspace.ui.features.grades.GradesViewModel
import com.example.polyspace.ui.features.grades.GradesViewModelFactory
import com.example.polyspace.ui.features.grades.UpdateStatus
import com.example.polyspace.ui.features.grades.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun GradesScreen() {
    val context = LocalContext.current
    val viewModel: GradesViewModel = viewModel(factory = GradesViewModelFactory(context))
    val state by viewModel.state.collectAsState()
    val refreshStatus by viewModel.refreshStatus.collectAsState()

    LaunchedEffect(Unit) { viewModel.verifySession() }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is GradesState.Loading -> LoadingView()

                is GradesState.LoginRequired -> {
                    LoginScreen(
                        onLoginClick = { user, pass -> viewModel.loginAndFetch(user, pass) },
                        isLoading = false,
                        errorMessage = null
                    )
                }

                is GradesState.Error -> {
                    LoginScreen(
                        onLoginClick = { user, pass -> viewModel.loginAndFetch(user, pass) },
                        isLoading = false,
                        errorMessage = currentState.message
                    )
                }

                is GradesState.Success -> {
                    GradesListView(
                        overview = currentState.overview,
                        refreshStatus = refreshStatus,
                        onRefresh = { viewModel.forceRefresh() }
                    )
                }
            }
        }
    }
}

@Composable
fun GradesListView(
    overview: PolyGradeOverview,
    refreshStatus: UpdateStatus,
    onRefresh: () -> Unit
) {
    if (overview.years.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Aucune note disponible 🤷‍♂️")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRefresh) { Text("Actualiser") }
            }
        }
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedSemesterKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (selectedSemesterKey == null) {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYearVal = calendar.get(Calendar.YEAR)
            val targetSchoolYear =
                if (currentMonth < 8) (currentYearVal - 1).toString() else currentYearVal.toString()
            val targetSemNumber = if (currentMonth in 8..11 || currentMonth == 0) "1" else "2"
            val targetKey = "$targetSchoolYear-$targetSemNumber"
            val exists =
                overview.years.any { y -> y.year == targetSchoolYear && y.semesters.any { it.number == targetSemNumber } }

            if (exists) {
                selectedSemesterKey = targetKey
            } else {
                val firstYear = overview.years.firstOrNull()
                val firstSem = firstYear?.semesters?.firstOrNull()
                if (firstYear != null && firstSem != null) {
                    selectedSemesterKey = "${firstYear.year}-${firstSem.number}"
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        var headerIndexCounter = 1

        item(key = "main_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mes Résultats",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                SmartRefreshButton(status = refreshStatus, onRefresh = onRefresh)
            }
        }

        overview.years.forEach { year ->
            year.semesters.forEach { semester ->
                val semesterKey = "${year.year}-${semester.number}"
                val isSelected = selectedSemesterKey == semesterKey
                val myHeaderIndex = headerIndexCounter
                val smoothSpec = spring<IntOffset>(dampingRatio = 0.8f, stiffness = 200f)

                item(key = "header_$semesterKey") {
                    Box(modifier = Modifier.animateItem(placementSpec = smoothSpec)) {
                        SemesterSelectorHeader(
                            title = "Semestre ${semester.number}",
                            year = year.year,
                            average = semester.average,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedSemesterKey = null
                                } else {
                                    selectedSemesterKey = semesterKey
                                    coroutineScope.launch {
                                        delay(20)
                                        listState.animateScrollToItem(
                                            index = myHeaderIndex,
                                            scrollOffset = 0
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                headerIndexCounter++

                if (isSelected) {
                    item(key = "dash_$semesterKey") {
                        Box(
                            modifier = Modifier.animateItem(
                                placementSpec = smoothSpec,
                                fadeInSpec = tween(300),
                                fadeOutSpec = tween(300)
                            )
                        ) {
                            SemesterDashboard(semester = semester)
                        }
                    }

                    val groupedClasses = semester.classes.groupBy { it.moduleName }

                    groupedClasses.forEach { (moduleName, classes) ->
                        if (moduleName.isNotBlank() && moduleName != "null") {
                            item(key = "module_${semesterKey}_$moduleName") {
                                Box(modifier = Modifier.animateItem(placementSpec = smoothSpec)) {
                                    ModuleHeader(moduleName)
                                }
                            }
                        }

                        items(items = classes, key = { "class_${it.id}" }) { gradeClass ->
                            Box(modifier = Modifier.animateItem(placementSpec = smoothSpec)) {
                                GradeItemCard(gradeClass)
                            }
                        }

                        item(key = "spacer_${semesterKey}_$moduleName") { Spacer(Modifier.height(16.dp)) }
                    }
                    item(key = "spacer_end_$semesterKey") { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chargement des notes...",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}