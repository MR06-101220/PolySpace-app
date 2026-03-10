package com.example.polyspace.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.ui.features.timetable.TimetableConfig
import com.example.polyspace.ui.features.timetable.TimetableViewModel
import com.example.polyspace.ui.features.timetable.detectZoomGestures
import com.example.polyspace.ui.features.timetable.components.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InfiniteTimetableScreen(
    viewModel: TimetableViewModel,
    homeworkCounts: Map<String, Int>,
    onCourseClick: (CourseEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var hourHeight by remember { mutableStateOf(52.dp) }
    var isZooming by remember { mutableStateOf(false) }
    var isUserScrollEnabled by remember { mutableStateOf(true) }
    val totalContentHeight = hourHeight * (TimetableConfig.END_HOUR - TimetableConfig.START_HOUR + 1)

    val horizontalState = rememberLazyListState(initialFirstVisibleItemIndex = TimetableConfig.START_INDEX)
    val verticalState = rememberScrollState()
    val isTodayVisible by remember {
        derivedStateOf {
            val layoutInfo = horizontalState.layoutInfo
            val viewportWidth = layoutInfo.viewportSize.width
            val todayItem = layoutInfo.visibleItemsInfo.find { it.index == TimetableConfig.START_INDEX }

            if (todayItem == null) false else {
                val itemStart = todayItem.offset
                val itemEnd = itemStart + todayItem.size
                (itemEnd > 5) && (itemStart < viewportWidth - 5)
            }
        }
    }
    val snapLayoutInfoProvider = remember(horizontalState) {
        object : SnapLayoutInfoProvider {
            override fun calculateSnapOffset(velocity: Float): Float {
                val visibleItems = horizontalState.layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return 0f
                val closestItem = visibleItems.minByOrNull { kotlin.math.abs(it.offset) }
                return closestItem?.offset?.toFloat() ?: 0f
            }
        }
    }
    val snapBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider = snapLayoutInfoProvider)
    val cacheVersion by viewModel.cacheVersion.collectAsState()

    LaunchedEffect(cacheVersion) {
        viewModel.updateWidget(context)
    }

    val currentResource by viewModel.currentResource.collectAsState()
    var targetDaysVisible by remember { mutableFloatStateOf(1f) }
    val animatedDaysVisible by animateFloatAsState(
        targetValue = targetDaysVisible,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "daysAnimation"
    )
    val firstVisibleIndex by remember { derivedStateOf { horizontalState.firstVisibleItemIndex } }
    val visibleDate = remember(firstVisibleIndex) {
        LocalDate.now().plusDays((firstVisibleIndex - TimetableConfig.START_INDEX).toLong())
    }
    var showViewMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val courseIcons by viewModel.courseIcons.collectAsState()
    var showExportScreen by remember { mutableStateOf(false) }

    if (selectedEvent != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEventSelected(null) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            CourseDetailContent(event = selectedEvent!!)
        }
    }

    if (showDatePicker) {
        val todayMillis = remember { System.currentTimeMillis() }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayMillis,
            initialDisplayedMonthMillis = todayMillis
        )

        DatePickerDialogWrapper(
            state = datePickerState,
            isLandscape = isLandscape,
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                scope.launch {
                    val daysFromToday = ChronoUnit.DAYS.between(LocalDate.now(), date).toInt()
                    val targetIndex = TimetableConfig.START_INDEX + daysFromToday
                    horizontalState.animateScrollToItem(targetIndex)
                }
                showDatePicker = false
            }
        )
    }

    if (showExportScreen) {
        val exportViewModel: com.example.polyspace.ui.features.export.ExportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

        com.example.polyspace.ui.features.export.ExportScreen(
            viewModel = exportViewModel,
            onBack = {
                exportViewModel.reset()
                showExportScreen = false }
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scope.launch { horizontalState.animateScrollToItem(TimetableConfig.START_INDEX) } },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(bottom = if (isLandscape) 16.dp else 80.dp)
                    .navigationBarsPadding()
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Aujourd'hui")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                TimetableHeader(
                    visibleDate = visibleDate,
                    onDateClick = { showDatePicker = true },
                    onViewMenuClick = { showViewMenu = true },
                    showViewMenu = showViewMenu,
                    onDismissMenu = { showViewMenu = false },
                    targetDaysVisible = targetDaysVisible,
                    onViewOptionSelected = { targetDaysVisible = it },
                    onRefreshClick = { viewModel.manualRefresh() },
                    onExportClick = {
                        showViewMenu = false
                        showExportScreen = true }
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 1.dp)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectZoomGestures(
                                headerHeightPx = TimetableConfig.HEADER_HEIGHT.toPx(),
                                topPaddingPx = TimetableConfig.TOP_SCROLL_PADDING.toPx(),
                                verticalState = verticalState,
                                getHourHeight = { hourHeight },
                                onZoomChange = { isZooming = it },
                                onScrollEnabledChange = { isUserScrollEnabled = it },
                                onHeightChange = { hourHeight = it },
                                coroutineScope = scope
                            )
                        }
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .width(TimetableConfig.TIME_COL_WIDTH)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        ) {
                            Spacer(modifier = Modifier.height(TimetableConfig.HEADER_HEIGHT))
                            Box(modifier = Modifier.weight(1f).verticalScroll(verticalState)) {
                                Column {
                                    Spacer(modifier = Modifier.height(TimetableConfig.TOP_SCROLL_PADDING))
                                    TimeLabelsColumnContent(
                                        hourHeight = hourHeight,
                                        totalContentHeight = totalContentHeight,
                                        showCurrentTime = isTodayVisible
                                    )
                                    Spacer(modifier = Modifier.height(100.dp))
                                }
                            }
                        }

                        LazyRow(
                            state = horizontalState,
                            modifier = Modifier.weight(1f),
                            userScrollEnabled = !isZooming,
                            flingBehavior = snapBehavior
                        ) {
                            items(count = Int.MAX_VALUE, key = { it }) { index ->
                                DayCompleteColumn(
                                    modifier = Modifier.fillParentMaxWidth(1f / animatedDaysVisible),
                                    date = LocalDate.now().plusDays((index - TimetableConfig.START_INDEX).toLong()),
                                    viewModel = viewModel,
                                    cacheVersion = cacheVersion,
                                    verticalScrollState = verticalState,
                                    compactMode = targetDaysVisible > 2f,
                                    homeworkCounts = homeworkCounts,
                                    onCourseClick = onCourseClick,
                                    targetDaysVisible = targetDaysVisible,
                                    hourHeight = hourHeight,
                                    totalContentHeight = totalContentHeight,
                                    isScrollEnabled = isUserScrollEnabled,
                                    isTodayVisible = isTodayVisible,
                                    courseIcons = courseIcons
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = currentResource.isTemporary,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 90.dp)
            ) {
                GuestModeBanner(
                    name = currentResource.name,
                    onExit = { viewModel.restoreOriginalProfile() }
                )
            }
        }
    }
}