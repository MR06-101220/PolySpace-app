package com.example.polyspace.ui.features.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.polyspace.data.local.Prefs
import com.example.polyspace.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class ExportViewModel : ViewModel() {

    private val repository = TimetableRepository()

    private val _draftCourses = MutableStateFlow<List<DraftCourse>>(emptyList())
    val draftCourses: StateFlow<List<DraftCourse>> = _draftCourses.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadWeek(_currentWeekStart.value)
    }

    fun loadWeek(monday: LocalDate) {
        _currentWeekStart.value = monday
        viewModelScope.launch {
            _isLoading.value = true

            val resourceId = Prefs.getResourceId()
            val resourceType = Prefs.getUserType()

            try {
                val allRawEvents = mutableListOf<com.example.polyspace.data.models.CourseEvent>()

                for (i in 0..4) {
                    val date = monday.plusDays(i.toLong())
                    val dailyEvents = repository.fetchTimetable(resourceId, resourceType, date, forceRefresh = false)
                    allRawEvents.addAll(dailyEvents)
                }

                val uniqueRawEvents = allRawEvents.distinctBy { "${it.title}_${it.start}" }

                _draftCourses.value = uniqueRawEvents.map { DraftCourse.fromCourseEvent(it) }.sortedBy { it.start }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCourse(updatedCourse: DraftCourse) {
        _draftCourses.value = _draftCourses.value.map {
            if (it.id == updatedCourse.id) updatedCourse else it
        }
    }

    fun deleteCourse(courseId: String) {
        _draftCourses.value = _draftCourses.value.filter { it.id != courseId }
    }

    fun addCustomCourse(newCourse: DraftCourse) {
        val newList = _draftCourses.value.toMutableList()
        newList.add(newCourse)
        _draftCourses.value = newList.sortedBy { it.start }
    }

    fun previousWeek() {
        loadWeek(_currentWeekStart.value.minusWeeks(1))
    }

    fun nextWeek() {
        loadWeek(_currentWeekStart.value.plusWeeks(1))
    }

    private val _allTemplates = MutableStateFlow<List<DraftCourse>>(emptyList())
    val allTemplates: StateFlow<List<DraftCourse>> = _allTemplates.asStateFlow()

    init {
        loadWeek(_currentWeekStart.value)
        loadAllTemplates()
    }

    private fun loadAllTemplates() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val resourceId = Prefs.getResourceId()
            val resourceType = Prefs.getUserType()
            val templates = mutableListOf<com.example.polyspace.data.models.CourseEvent>()

            val today = LocalDate.now()
            for (i in -15..15) {
                try {
                    templates.addAll(repository.fetchTimetable(resourceId, resourceType, today.plusDays(i.toLong()), false))
                } catch (e: Exception) {}
            }

            val uniqueTemplates = templates.map { DraftCourse.fromCourseEvent(it) }.distinctBy { it.title }
            _allTemplates.value = uniqueTemplates
        }
    }

    fun reset() {
        val monday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        _currentWeekStart.value = monday
        loadWeek(monday)
    }

}