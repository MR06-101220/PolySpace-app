package com.example.polyspace.ui.features.homework

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.polyspace.NotificationWorker // Assure-toi que ce fichier existe quelque part (ex: root ou workers)
import com.example.polyspace.data.local.AppDatabase // <--- CORRECTION IMPORT
import com.example.polyspace.data.models.Homework
import com.example.polyspace.data.models.Priority
import com.example.polyspace.data.repository.HomeworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class HomeworkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeworkRepository

    init {
        val dao = AppDatabase.getDatabase(application).homeworkDao()
        repository = HomeworkRepository(dao)
    }
    private val _rawHomeworks = repository.allHomeworks
    private val _sortType = MutableStateFlow(SortType.DATE)
    val sortType: StateFlow<SortType> = _sortType
    val allHomeworks: StateFlow<List<Homework>> = combine(_rawHomeworks, _sortType) { list, sort ->
        when (sort) {
            SortType.DATE -> list.sortedWith(compareBy<Homework> { it.isDone }
                .thenBy { it.dueDate == null }
                .thenBy { it.dueDate })

            SortType.PRIORITY -> list.sortedWith(compareBy<Homework> { it.isDone }
                .thenByDescending { it.priority })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSortType(type: SortType) {
        _sortType.value = type
    }

    fun addHomework(homework: Homework) {
        viewModelScope.launch {
            val id = repository.insert(homework)
            val savedHomework = homework.copy(id = id)
            scheduleSmartNotifications(savedHomework)
        }
    }

    fun deleteHomework(homework: Homework) {
        viewModelScope.launch {
            cancelNotifications(homework.id)
            repository.delete(homework)
        }
    }

    fun updateHomework(homework: Homework) {
        viewModelScope.launch {
            repository.update(homework)

            if (homework.isDone) {
                cancelNotifications(homework.id)
            } else {
                scheduleSmartNotifications(homework)
            }
        }
    }

    fun toggleHomeworkDone(homework: Homework) {
        val updatedHomework = homework.copy(isDone = !homework.isDone)
        updateHomework(updatedHomework)
    }
    val homeworkCounts: StateFlow<Map<String, Int>> = allHomeworks.map { list ->
        list
            .filter { !it.isDone && it.subject != null }
            .groupingBy { it.subject!! }
            .eachCount()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    private fun cancelNotifications(homeworkId: Long) {
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag("homework_$homeworkId")
        Log.d("NotifSystem", "Toutes les notifications annulées pour le devoir ID $homeworkId")
    }

    private fun scheduleSmartNotifications(homework: Homework) {
        cancelNotifications(homework.id)

        Log.d("NotifDebug", "Analyse pour ID ${homework.id} : Date=${homework.dueDate}, Fait=${homework.isDone}")

        if (homework.dueDate == null) return
        if (homework.isDone) return

        val daysBeforeList = when (homework.priority) {
            Priority.LOW -> emptyList()
            Priority.NORMAL -> listOf(1, 3)
            Priority.URGENT -> listOf(1, 2, 3, 5, 7)
        }

        val now = LocalDateTime.now()
        var scheduledCount = 0

        daysBeforeList.forEach { daysBefore ->
            val notificationTime = LocalDateTime.of(
                homework.dueDate.minusDays(daysBefore.toLong()),
                LocalTime.of(18, 0)
            )

            if (notificationTime.isAfter(now)) {
                val delayInSeconds = Duration.between(now, notificationTime).seconds

                val customMessage = if (daysBefore == 1) {
                    "C'est pour demain ! Au boulot : ${homework.title}"
                } else {
                    "Rappel J-$daysBefore : ${homework.title}"
                }

                val data = workDataOf(
                    "title" to "Rappel ${if (homework.priority == Priority.URGENT) "🔴 URGENT" else ""}",
                    "message" to customMessage,
                    "id" to homework.id
                )

                val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delayInSeconds, TimeUnit.SECONDS)
                    .setInputData(data)
                    .addTag("homework_${homework.id}")
                    .build()

                WorkManager.getInstance(getApplication()).enqueue(workRequest)
                scheduledCount++
            }
        }
        Log.d("NotifSystem", "Résultat final : $scheduledCount notifs programmées.")
    }
}