package com.example.polyspace.ui.features.grades

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.polyspace.core.GlobalEvents
import com.example.polyspace.data.remote.MissingPhotoException
import com.example.polyspace.data.models.PolyGradeOverview
import com.example.polyspace.data.repository.GradesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GradesViewModel(context: Context) : ViewModel() {
    private val repository = GradesRepository(context)

    private val _state = MutableStateFlow<GradesState>(GradesState.Loading)
    val state: StateFlow<GradesState> = _state.asStateFlow()

    private val _refreshStatus = MutableStateFlow(UpdateStatus.IDLE)
    val refreshStatus: StateFlow<UpdateStatus> = _refreshStatus.asStateFlow()

    init {
        val cachedData = repository.loadFromCache()
        if (cachedData != null) {
            _state.value = GradesState.Success(cachedData)
        }

        refreshGrades(isBackground = cachedData != null)
        viewModelScope.launch {
            GlobalEvents.clearGradesCacheEvent.collect {
                _state.value = GradesState.Loading
                refreshGrades(isBackground = false)
            }
        }
    }


    fun loginAndFetch(username: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = GradesState.Loading
            val loginSuccess = repository.login(username, pass)

            if (loginSuccess) {
                repository.saveCredentials(username, pass)
                delay(1000) // Sécurité Oasis
                refreshGradesInternal(forceUIUpdate = true)
            } else {
                _state.value = GradesState.Error("Identifiants incorrects ou erreur réseau.")
            }
        }
    }

    fun forceRefresh() {
        refreshGrades(isBackground = true)
    }

    fun verifySession() {
        val (user, _) = repository.getSavedCredentials()
        val hasCache = repository.loadFromCache() != null

        if (user == null && !hasCache && _state.value !is GradesState.LoginRequired) {
            _state.value = GradesState.LoginRequired
        }
    }

    private fun refreshGrades(isBackground: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isBackground) _state.value = GradesState.Loading
            _refreshStatus.value = UpdateStatus.LOADING

            val (user, pass) = repository.getSavedCredentials()

            if (user != null && pass != null) {
                val loginSuccess = repository.login(user, pass)
                if (loginSuccess) {
                    refreshGradesInternal(forceUIUpdate = !isBackground)
                } else {
                    _refreshStatus.value = UpdateStatus.ERROR
                    if (!isBackground) _state.value = GradesState.LoginRequired
                    delay(3000)
                    _refreshStatus.value = UpdateStatus.IDLE
                }
            } else {
                if (!isBackground) _state.value = GradesState.LoginRequired
                _refreshStatus.value = UpdateStatus.IDLE
            }
        }
    }

    private suspend fun refreshGradesInternal(forceUIUpdate: Boolean) {
        try {
            val newOverview = repository.fetchGrades()

            if (newOverview != null) {
                val currentData = (_state.value as? GradesState.Success)?.overview
                val hadData = currentData?.years?.isNotEmpty() == true
                val newHasData = newOverview.years.isNotEmpty()

                if (!newHasData && hadData) {
                    _refreshStatus.value = UpdateStatus.ERROR
                } else {
                    repository.saveToCache(newOverview)
                    _state.value = GradesState.Success(newOverview)
                    _refreshStatus.value = UpdateStatus.SUCCESS
                }
            } else {
                _refreshStatus.value = UpdateStatus.ERROR
                if (forceUIUpdate) _state.value = GradesState.Error("Impossible de joindre Oasis.")
            }

        } catch (e: MissingPhotoException) {
            Log.e("GradesVM", "Blocage Photo")
            _refreshStatus.value = UpdateStatus.ERROR
            _state.value = GradesState.Error("Oasis bloque vos notes (Photo manquante).")
        } catch (e: Exception) {
            Log.e("GradesVM", "Erreur technique", e)
            _refreshStatus.value = UpdateStatus.ERROR
            if (forceUIUpdate) _state.value = GradesState.Error("Erreur technique.")
        }

        delay(3000)
        _refreshStatus.value = UpdateStatus.IDLE
    }
}

class GradesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GradesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GradesViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}