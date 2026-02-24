package com.example.polyspace.ui.features.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.polyspace.data.local.Prefs
import com.example.polyspace.data.models.CourseEvent
import com.example.polyspace.data.models.PositionedEvent
import com.example.polyspace.data.remote.NetworkModule
import com.example.polyspace.data.repository.TimetableRepository
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

            fetchTimetableInternal(currentDate, forceRefresh = true)

        }
    }

    private suspend fun fetchTimetableInternal(date: LocalDate, forceRefresh: Boolean = false) {
        val resource = _currentResource.value

        if (!forceRefresh) {
            val cachedEvents = repository.getCachedTimetable(resource.id, date)
            if (cachedEvents.isNotEmpty()) {
                processEvents(date, cachedEvents)
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
}

