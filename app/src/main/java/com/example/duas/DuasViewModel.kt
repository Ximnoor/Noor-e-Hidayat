package com.example.duas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.Dua
import com.example.data.repository.DuaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DuasViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = DuaRepository(db.duaDao())

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val duas: StateFlow<List<Dua>> = combine(repository.allDuas, _selectedCategory) { duasList, category ->
        if (category == "All") {
            duasList
        } else {
            duasList.filter { it.category == category }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val categories: StateFlow<List<String>> = repository.allDuas
        .combine(MutableStateFlow(Unit)) { duasList, _ ->
            listOf("All") + duasList.map { it.category }.distinct()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("All")
        )

    init {
        viewModelScope.launch {
            repository.populateInitialDataIfNeeded()
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(duaId: Int, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(duaId, !currentStatus)
        }
    }
}
