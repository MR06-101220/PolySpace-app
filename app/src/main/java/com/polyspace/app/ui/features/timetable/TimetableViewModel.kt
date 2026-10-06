package com.polyspace.app.ui.features.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polyspace.app.data.local.Prefs
import com.polyspace.app.data.models.CourseEvent
import com.polyspace.app.data.models.PositionedEvent
import com.polyspace.app.data.remote.NetworkModule
import com.polyspace.app.data.repository.TimetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import androidx.glance.appwidget.updateAll
import com.polyspace.app.widget.TimetableWidget
import android.content.Context
import com.polyspace.app.utils.SymbolMapper

class TimetableViewModel : ViewModel() {

    private val repository = TimetableRepository()
    private val _uiState = MutableStateFlow<TimetableUiState>(TimetableUiState.Loading)
    val uiState: StateFlow<TimetableUiState> = _uiState

    private val _cacheVersion = MutableStateFlow(0)
    val cacheVersion = _cacheVersion.asStateFlow()

    private val _setupState = MutableStateFlow<SetupUiState>(SetupUiState.Idle)
    val setupState: StateFlow<SetupUiState> = _setupState

    private val _selectedEvent = MutableStateFlow<CourseEvent?>(null)
    val selectedEvent: StateFlow<CourseEvent?> = _selectedEvent
    private val _allLoadedEvents = MutableStateFlow<List<CourseEvent>>(emptyList())
    val events: StateFlow<List<CourseEvent>> = _allLoadedEvents.asStateFlow()
    private val _currentResource = MutableStateFlow(
        CurrentResource(
            type = Prefs.getUserType(),
            id = Prefs.getResourceId(),
            name = Prefs.getDisplayName(),
            isTemporary = false
        )
    )
    val currentResource: StateFlow<CurrentResource> = _currentResource
    private val _knownSubjects = MutableStateFlow<List<String>>(emptyList())
    val knownSubjects: StateFlow<List<String>> = _knownSubjects

    val uniqueSubjects: StateFlow<List<String>> = _knownSubjects.map { list ->
        list.filter { subject ->
            !subject.contains("tiers temps", ignoreCase = true) &&
                    !subject.contains("réservation", ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    private val eventsCache = mutableMapOf<LocalDate, List<PositionedEvent>>()
    private val fetchingDates = mutableSetOf<LocalDate>()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _courseIcons = MutableStateFlow<Map<String, androidx.compose.ui.graphics.vector.ImageVector>>(emptyMap())
    val courseIcons: StateFlow<Map<String, androidx.compose.ui.graphics.vector.ImageVector>> = _courseIcons.asStateFlow()

    init {
        refreshSubjects()
        if (Prefs.isSetupDone()) {
            fetchTimetable(LocalDate.now())
            loadPromos()
        } else {
            loadPromos()
        }
    }

    fun ensureDateLoaded(date: LocalDate) {
        if (eventsCache.containsKey(date)) return
        if (fetchingDates.contains(date)) return

        fetchingDates.add(date)
        viewModelScope.launch {
            fetchTimetableInternal(date)
            fetchingDates.remove(date)
        }
    }

    fun fetchTimetable(date: LocalDate) {
        _uiState.value = TimetableUiState.Success(date)
        viewModelScope.launch {
            fetchTimetableInternal(date)
        }
    }

    fun getEventsForDate(date: LocalDate): List<PositionedEvent> {
        return eventsCache[date] ?: emptyList()
    }

    fun onEventSelected(event: CourseEvent?) {
        _selectedEvent.value = event
    }

    fun refreshSubjects() {
        val subjects = Prefs.getKnownSubjects().toList().sorted()
        _knownSubjects.value = subjects
    }

    fun switchToTemporaryPromo(promoName: String) {
        _currentResource.value = CurrentResource("PROMO", promoName, promoName, isTemporary = true)
        refreshData()
    }

    fun restoreOriginalProfile() {
        _currentResource.value = CurrentResource(
            Prefs.getUserType(),
            Prefs.getResourceId(),
            Prefs.getDisplayName(),
            isTemporary = false
        )
        refreshData()
    }

    fun saveConfiguration(type: String, id: String, name: String) {
        if (type == "PROMO") Prefs.savePromo(id) else Prefs.saveStudent(id, name)
        _currentResource.value = CurrentResource(type, id, name, isTemporary = false)
        refreshData()
    }

    private fun refreshData() {
        eventsCache.clear()
        _cacheVersion.value += 1
        fetchTimetable(LocalDate.now())
    }

    fun manualRefresh() {
        val currentDate = (uiState.value as? TimetableUiState.Success)?.date ?: LocalDate.now()

        viewModelScope.launch {
            _isRefreshing.value = true

            fetchTimetableInternal(currentDate, forceRefresh = true)

            _isRefreshing.value = false
        }
    }

    private suspend fun fetchTimetableInternal(date: LocalDate, forceRefresh: Boolean = false) {
        val resource = _currentResource.value

        if (!forceRefresh) {
            val cachedEvents = repository.getCachedTimetable(resource.id, date)
            if (cachedEvents.isNotEmpty()) {
                processEvents(date, cachedEvents)
                loadMissingSymbols(cachedEvents)
            }
        }

        try {
            val networkEvents = repository.fetchTimetable(
                resourceId = resource.id,
                resourceType = resource.type,
                date = date,
                forceRefresh = forceRefresh
            )

            processEvents(date, networkEvents)
            updateKownSubjects(networkEvents)
            loadMissingSymbols(networkEvents)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processEvents(requestDate: LocalDate, rawEvents: List<CourseEvent>) {
        val eventsByDay = rawEvents.groupBy {
            LocalDate.parse(it.start.substring(0, 10))
        }

        eventsByDay.forEach { (day, dayEvents) ->
            val positioned = EventLayoutEngine.calculatePositions(dayEvents.sortedBy { it.start })
            eventsCache[day] = positioned
        }

        if (!eventsByDay.containsKey(requestDate)) {
            eventsCache[requestDate] = emptyList()
        }

        _cacheVersion.value += 1
    }

    private fun updateKownSubjects(events: List<CourseEvent>) {
        val newSubjects = events.mapNotNull { it.title}.filter { it.isNotBlank() }
        if (newSubjects.isNotEmpty()) {
            Prefs.addKnownSubjects(newSubjects)
            _knownSubjects.value = Prefs.getKnownSubjects().toList().sorted()
        }
    }

    fun loadPromos() {
        viewModelScope.launch {
            if (!Prefs.isSetupDone()) _setupState.value = SetupUiState.Loading
            try {
                val promos = withContext(Dispatchers.IO) { NetworkModule.api.getPromos() }
                _setupState.value = SetupUiState.PromosLoaded(promos)
            } catch (e: Exception) {
                _setupState.value = SetupUiState.Error("Erreur chargement promos")
            }
        }
    }

    fun searchStudent(query: String) {
        android.util.Log.d("SearchDebug", "Recherche demandée pour : $query")

        if (query.isBlank()) return

        viewModelScope.launch {
            _setupState.value = SetupUiState.Loading
            try {
                android.util.Log.d("SearchDebug", "Appel API en cours...")

                val results = withContext(Dispatchers.IO) {
                    NetworkModule.api.searchResources(query)
                }

                android.util.Log.d("SearchDebug", "Résultats reçus : ${results.size}")
                _setupState.value = SetupUiState.SearchResults(results)
            } catch (e: Exception) {
                android.util.Log.e("SearchDebug", "Erreur API", e)
                _setupState.value = SetupUiState.Error("Erreur : ${e.localizedMessage}")
            }
        }
    }
    fun clearSetupState() {
        _setupState.value = SetupUiState.Idle
    }

    private val _daysToShow = MutableStateFlow(1)
    val daysToShow: StateFlow<Int> = _daysToShow
    fun setDaysToShow(days: Int) { _daysToShow.value = days }


    fun updateWidget(context: Context) {
        viewModelScope.launch {
            try {
                TimetableWidget().updateAll(context)

                val dateStr = LocalDate.now().toString()
                val resourceId = Prefs.getResourceId()
                val cachedJson = Prefs.getTimetableCache(resourceId, dateStr)

                if (cachedJson != null) {
                    val events = kotlinx.serialization.json.Json.decodeFromString<List<CourseEvent>>(cachedJson)
                    com.polyspace.app.widget.WidgetScheduler.scheduleWidgetUpdates(context, events)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadMissingSymbols(events: List<CourseEvent>) {
        viewModelScope.launch(Dispatchers.IO) {
            val uniqueTitles = events.mapNotNull { it.title }.filter { it.isNotBlank() }.distinct()
            val currentIcons = _courseIcons.value.toMutableMap()
            var mapUpdated = false

            for (title in uniqueTitles) {
                if (currentIcons.containsKey(title)) continue

                var symbolId = Prefs.getCourseSymbol(title)

                if (symbolId == null) {
                    try {
                        val response = NetworkModule.api.fetchSymbol(courseName = title)
                        symbolId = response.symbolId ?: "book"
                        android.util.Log.d("SymbolMapper", "Matière : $title -> SF Symbol reçu : $symbolId")
                        Prefs.saveCourseSymbol(title, symbolId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        symbolId = "book"
                    }
                }

                currentIcons[title] = SymbolMapper.getMaterialIcon(courseName = title, sfSymbolId = symbolId)
                mapUpdated = true
            }

            if (mapUpdated) {
                _courseIcons.value = currentIcons
            }
        }
    }

    private val _treeHistory = MutableStateFlow<List<TreeHistoryItem>>(emptyList())
    val treeHistory: StateFlow<List<TreeHistoryItem>> = _treeHistory.asStateFlow()

    private val _isTreeLoading = MutableStateFlow(false)
    val isTreeLoading: StateFlow<Boolean> = _isTreeLoading.asStateFlow()

    private val _treeError = MutableStateFlow<String?>(null)
    val treeError: StateFlow<String?> = _treeError.asStateFlow()

    fun loadTreeRoot(force: Boolean = false) {
        if (_treeHistory.value.isNotEmpty() && !force) return
        viewModelScope.launch {
            _isTreeLoading.value = true
            _treeError.value = null
            try {
                val rootNodes = withContext(Dispatchers.IO) {
                    NetworkModule.api.getAdeTree(config = null)
                }
                _treeHistory.value = listOf(TreeHistoryItem(parentNode = null, items = rootNodes))
            } catch (e: Exception) {
                e.printStackTrace()
                _treeError.value = "Impossible de charger l'arborescence : ${e.localizedMessage}"
            } finally {
                _isTreeLoading.value = false
            }
        }
    }

    fun openTreeNode(node: com.polyspace.app.data.models.AdeTreeNode) {
        viewModelScope.launch {
            _isTreeLoading.value = true
            _treeError.value = null
            try {
                val children = withContext(Dispatchers.IO) {
                    NetworkModule.api.getAdeTree(config = node.config)
                }
                _treeHistory.value = _treeHistory.value + TreeHistoryItem(parentNode = node, items = children)
            } catch (e: Exception) {
                e.printStackTrace()
                _treeError.value = "Erreur lors de l'ouverture : ${e.localizedMessage}"
            } finally {
                _isTreeLoading.value = false
            }
        }
    }

    fun navigateBackInTree(): Boolean {
        if (_treeHistory.value.size > 1) {
            _treeHistory.value = _treeHistory.value.dropLast(1)
            return true
        }
        return false
    }

    fun selectTreeNode(node: com.polyspace.app.data.models.AdeTreeNode) {
        saveConfiguration(type = "STUDENT", id = node.id, name = node.label)
    }

}

data class TreeHistoryItem(
    val parentNode: com.polyspace.app.data.models.AdeTreeNode?,
    val items: List<com.polyspace.app.data.models.AdeTreeNode>
)