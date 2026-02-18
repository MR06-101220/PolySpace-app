package com.example.polyspace

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.polyspace.data.local.Prefs
import com.example.polyspace.ui.features.homework.HomeworkViewModel
import com.example.polyspace.ui.features.timetable.SetupUiState
import com.example.polyspace.ui.features.timetable.TimetableViewModel
import com.example.polyspace.ui.screens.GradesScreen
import com.example.polyspace.ui.screens.HomeworkScreen
import com.example.polyspace.ui.screens.InfiniteTimetableScreen
import com.example.polyspace.ui.screens.SettingsScreen
import com.example.polyspace.ui.screens.SetupScreen
import com.example.polyspace.ui.theme.PolySpaceTheme
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionRequest = registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { }
            permissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()
        Prefs.init(this)
        setContent {
            PolySpaceTheme {
                var isSetupDone by remember { mutableStateOf(Prefs.isSetupDone()) }
                if (!isSetupDone) {
                    SetupScreen(onSetupComplete = { isSetupDone = true })
                } else {
                    MainApp(onLogout = {
                        Prefs.clear()
                        isSetupDone = false
                    })
                }
            }
        }
    }
}

// Navigation
@Parcelize
sealed class Screen : Parcelable {
    abstract val route: String
    abstract val label: String
    abstract val icon: ImageVector

    data object Timetable : Screen() {
        override val route = "timetable"
        override val label = "Agenda"
        override val icon = Icons.Default.DateRange
    }

    data object Homework : Screen() {
        override val route = "homework"
        override val label = "Devoirs"
        override val icon = Icons.Default.Edit
    }

    data object Grades : Screen() {
        override val route = "grades"
        override val label = "Notes"
        override val icon = Icons.Default.Grade
    }

    data object Settings : Screen() {
        override val route = "settings"
        override val label = "Paramètres"
        override val icon = Icons.Default.Settings
    }
}

// Main
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val timetableViewModel: TimetableViewModel = viewModel()
    val homeworkViewModel: HomeworkViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home_graph",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home_graph") {
                HomeScreenWithNavigation(
                    rootNavController = navController,
                    timetableViewModel = timetableViewModel,
                    homeworkViewModel = homeworkViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenWithNavigation(
    rootNavController: NavController,
    timetableViewModel: TimetableViewModel,
    homeworkViewModel: HomeworkViewModel = viewModel(),
    onLogout: () -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf<Screen>(Screen.Timetable) }
    val items = listOf(Screen.Timetable, Screen.Homework, Screen.Grades, Screen.Settings)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showQuickPromoSelector by remember { mutableStateOf(false) }

    val homeworkCounts by homeworkViewModel.homeworkCounts.collectAsState()
    val subjects by timetableViewModel.uniqueSubjects.collectAsState()
    val animDuration = 300
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(currentTab) {
        isAnimating = true
        delay(animDuration.toLong())
        isAnimating = false
    }

    val animatedCornerRadius by animateDpAsState(
        targetValue = if (isAnimating) 32.dp else 0.dp,
        animationSpec = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
        label = "cornerAnimation"
    )

    Row(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        if (isLandscape) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                items.forEach { screen ->
                    val isTimetable = screen == Screen.Timetable
                    NavigationRailItem(
                        icon = {
                            if (isTimetable) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .combinedClickable(
                                            onClick = { currentTab = screen },
                                            onLongClick = { showQuickPromoSelector = true }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) { Icon(screen.icon, contentDescription = screen.label) }
                            } else {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        },
                        label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                        selected = currentTab == screen,
                        onClick = { if (!isTimetable) currentTab = screen }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {

            AnimatedContent(
                targetState = currentTab,
                label = "TabTransition",
                transitionSpec = {
                    val pivotFractionX = when (targetState) {
                        Screen.Timetable -> 0.15f
                        Screen.Homework -> 0.38f
                        Screen.Grades -> 0.62f
                        Screen.Settings -> 0.85f
                    }

                    val enterTransition = scaleIn(
                        initialScale = 0.1f,
                        animationSpec = tween(animDuration, easing = FastOutSlowInEasing),
                        transformOrigin = TransformOrigin(pivotFractionX, 1.0f)
                    ) + fadeIn(animationSpec = tween(200))

                    val exitTransition = fadeOut(
                        animationSpec = tween(
                            durationMillis = animDuration,
                            easing = FastOutSlowInEasing
                        )
                    )

                    enterTransition.togetherWith(exitTransition)
                }
            ) { targetState ->

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(animatedCornerRadius),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(animatedCornerRadius))) {
                        when (targetState) {
                            Screen.Timetable -> InfiniteTimetableScreen(
                                viewModel = timetableViewModel,
                                homeworkCounts = homeworkCounts,
                                onCourseClick = { event -> timetableViewModel.onEventSelected(event) }
                            )

                            Screen.Homework -> HomeworkScreen(
                                viewModel = homeworkViewModel,
                                availableSubjects = subjects
                            )

                            Screen.Grades -> GradesScreen()
                            Screen.Settings -> SettingsScreen(
                                viewModel = timetableViewModel,
                                onLogout = onLogout
                            )
                        }
                    }
                }
            }

            if (!isLandscape) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    FloatingBottomBar(
                        items = items,
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onTimetableLongPress = { showQuickPromoSelector = true })
                }
            }
        }
    }

    if (showQuickPromoSelector) {
        QuickPromoDialog(
            viewModel = timetableViewModel,
            onDismiss = { showQuickPromoSelector = false },
            onPromoSelected = { promoName ->
                timetableViewModel.switchToTemporaryPromo(promoName)
                currentTab = Screen.Timetable
                showQuickPromoSelector = false
            }
        )
    }
}

@Composable
fun QuickPromoDialog(
    viewModel: TimetableViewModel,
    onDismiss: () -> Unit,
    onPromoSelected: (String) -> Unit
) {
    val setupState by viewModel.setupState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadPromos() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Coup d'œil rapide 👀",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Voir l'agenda d'une autre promo sans changer de compte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                when (val state = setupState) {
                    is SetupUiState.PromosLoaded -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.promos) { promo ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPromoSelected(promo.name) }) {
                                    Text(
                                        text = promo.name,
                                        modifier = Modifier.padding(16.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    is SetupUiState.Loading -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    else -> Text("Impossible de charger les promos")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Annuler") }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingBottomBar(
    items: List<Screen>,
    currentTab: Screen,
    onTabSelected: (Screen) -> Unit,
    onTimetableLongPress: () -> Unit
) {
    val selectedIndex = items.indexOf(currentTab).takeIf { it >= 0 } ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 40.dp, end = 40.dp, bottom = 20.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            modifier = Modifier.height(60.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemWidth = maxWidth / items.size
                val indicatorOffset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                    label = "indicatorOffset"
                )
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .offset(x = indicatorOffset)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    items.forEachIndexed { index, screen ->
                        val isSelected = index == selectedIndex;
                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            animationSpec = tween(300),
                            label = "iconColor"
                        ); Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50.dp))
                            .combinedClickable(
                                onClick = { onTabSelected(screen) },
                                onLongClick = { if (screen == Screen.Timetable) onTimetableLongPress() })
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            tint = iconColor,
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                    }
                }
            }
        }
    }
}

private fun createNotificationChannel(context: Context) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val name = "Rappels Devoirs"
        val descriptionText = "Notifications pour les devoirs à rendre"
        val importance = android.app.NotificationManager.IMPORTANCE_HIGH
        val channel = android.app.NotificationChannel("HOMEWORK_CHANNEL", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: android.app.NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}