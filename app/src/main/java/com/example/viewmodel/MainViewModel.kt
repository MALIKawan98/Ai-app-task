package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppDatabase
import com.example.model.Category
import com.example.model.Reminder
import com.example.model.ReminderRepository
import com.example.model.SettingsRepository
import com.example.model.ThemeMode
import com.example.notifications.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ReminderRepository(database.reminderDao())
    val settingsRepository = SettingsRepository(application)
    private val alarmScheduler = AlarmScheduler(application)

    init {
        viewModelScope.launch {
            repository.initializeCategories()
        }
    }

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val upcomingReminders = repository.upcomingReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedReminders = repository.completedReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults = _searchQuery.combine(repository.allReminders) { query, reminders ->
        if (query.isBlank()) emptyList()
        else reminders.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.notes.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun toggleReminderCompletion(reminder: Reminder) {
        viewModelScope.launch {
            val isCompleted = !reminder.isCompleted
            repository.updateCompletionStatus(reminder.id, isCompleted)
            if (isCompleted) {
                alarmScheduler.cancelAlarm(reminder.id)
            } else {
                alarmScheduler.scheduleAlarm(reminder)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminderById(reminder.id)
            alarmScheduler.cancelAlarm(reminder.id)
        }
    }

    fun saveReminder(reminder: Reminder, isEdit: Boolean = false) {
        viewModelScope.launch {
            if (isEdit) {
                repository.updateReminder(reminder)
                if (!reminder.isCompleted) {
                    alarmScheduler.scheduleAlarm(reminder)
                }
            } else {
                val id = repository.insertReminder(reminder)
                if (!reminder.isCompleted) {
                    alarmScheduler.scheduleAlarm(reminder.copy(id = id))
                }
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, isSystem = false))
        }
    }
}
