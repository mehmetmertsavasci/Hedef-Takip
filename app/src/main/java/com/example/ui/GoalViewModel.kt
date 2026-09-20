package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.api.ApiResponse
import com.example.api.ExternalApiHub
import com.example.data.GoalDatabase
import com.example.data.GoalRepository
import com.example.model.AppThemeMode
import com.example.model.DarkModePreference
import com.example.model.Goal
import com.example.receiver.GoalReminderReceiver
import com.example.sync.CloudSyncManager
import com.example.sync.CloudSyncState
import com.example.widget.GoalAppWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GoalRepository
    val cloudSyncManager: CloudSyncManager
    val externalApiHub: ExternalApiHub

    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _selectedCategory = MutableStateFlow("Hepsi")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.EMERALD.name) ?: AppThemeMode.EMERALD.name)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _darkMode = MutableStateFlow(
        DarkModePreference.valueOf(prefs.getString("dark_mode", DarkModePreference.SYSTEM.name) ?: DarkModePreference.SYSTEM.name)
    )
    val darkMode: StateFlow<DarkModePreference> = _darkMode.asStateFlow()

    private val _e2eePassphrase = MutableStateFlow(
        prefs.getString("e2ee_passphrase", "HedefKasa@2026!Gizli") ?: "HedefKasa@2026!Gizli"
    )
    val e2eePassphrase: StateFlow<String> = _e2eePassphrase.asStateFlow()

    private val _lastApiTestResult = MutableStateFlow<ApiResponse?>(null)
    val lastApiTestResult: StateFlow<ApiResponse?> = _lastApiTestResult.asStateFlow()

    val allGoals: StateFlow<List<Goal>>

    val filteredGoals: StateFlow<List<Goal>>

    val syncState: StateFlow<CloudSyncState>

    init {
        val database = GoalDatabase.getDatabase(application, viewModelScope)
        repository = GoalRepository(database.goalDao())
        cloudSyncManager = CloudSyncManager(application, viewModelScope)
        externalApiHub = ExternalApiHub(application)
        syncState = cloudSyncManager.syncState

        allGoals = repository.allGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredGoals = combine(allGoals, _selectedCategory) { goals, category ->
            if (category == "Hepsi") goals
            else goals.filter { it.category == category }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setTheme(theme: AppThemeMode) {
        _themeMode.value = theme
        prefs.edit().putString("theme_mode", theme.name).apply()
    }

    fun setDarkMode(mode: DarkModePreference) {
        _darkMode.value = mode
        prefs.edit().putString("dark_mode", mode.name).apply()
    }

    fun setPassphrase(passphrase: String) {
        _e2eePassphrase.value = passphrase
        prefs.edit().putString("e2ee_passphrase", passphrase).apply()
    }

    fun addOrUpdateGoal(goal: Goal) {
        viewModelScope.launch {
            if (goal.id == 0L) {
                val newId = repository.insert(goal)
                val saved = goal.copy(id = newId)
                if (saved.hasReminder) {
                    AlarmScheduler.schedule(getApplication(), saved)
                }
            } else {
                repository.update(goal)
                if (goal.hasReminder) {
                    AlarmScheduler.schedule(getApplication(), goal)
                } else {
                    AlarmScheduler.cancel(getApplication(), goal.id)
                }
            }
            GoalAppWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            AlarmScheduler.cancel(getApplication(), goal.id)
            repository.delete(goal)
            GoalAppWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun incrementProgress(goal: Goal) {
        viewModelScope.launch {
            repository.incrementProgress(goal.id, 1)
            GoalAppWidgetProvider.updateAllWidgets(getApplication())
            // If now completed, dispatch webhook
            if (goal.currentCount + 1 >= goal.targetCount) {
                externalApiHub.dispatchGoalCompletedWebhook(goal.copy(isCompleted = true))
            }
        }
    }

    fun decrementProgress(goal: Goal) {
        viewModelScope.launch {
            repository.incrementProgress(goal.id, -1)
            GoalAppWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun toggleComplete(goal: Goal) {
        viewModelScope.launch {
            repository.toggleComplete(goal.id)
            GoalAppWidgetProvider.updateAllWidgets(getApplication())
            if (!goal.isCompleted) {
                externalApiHub.dispatchGoalCompletedWebhook(goal.copy(isCompleted = true))
            }
        }
    }

    fun testAlarmNotification(goal: Goal) {
        val intent = Intent(getApplication(), GoalReminderReceiver::class.java).apply {
            action = if (goal.isAlarm) GoalReminderReceiver.ACTION_GOAL_ALARM else GoalReminderReceiver.ACTION_GOAL_REMINDER
            putExtra(GoalReminderReceiver.EXTRA_GOAL_ID, goal.id)
            putExtra(GoalReminderReceiver.EXTRA_GOAL_TITLE, goal.title)
            putExtra(GoalReminderReceiver.EXTRA_GOAL_CATEGORY, goal.category)
            putExtra(GoalReminderReceiver.EXTRA_IS_ALARM, goal.isAlarm)
        }
        getApplication<Application>().sendBroadcast(intent)
        Toast.makeText(getApplication(), "${if (goal.isAlarm) "Alarm" else "Bildirim"} tetiklendi!", Toast.LENGTH_SHORT).show()
    }

    fun syncToCloud(onDone: (Boolean, String) -> Unit) {
        val currentGoals = allGoals.value
        cloudSyncManager.pushEncryptedSync(currentGoals, _e2eePassphrase.value, onDone)
    }

    fun restoreFromCloud(onDone: (Boolean, String) -> Unit) {
        cloudSyncManager.pullEncryptedSync(_e2eePassphrase.value) { success, msg, goals ->
            if (success && goals != null) {
                viewModelScope.launch {
                    repository.replaceAll(goals)
                    // Reschedule reminders
                    for (g in goals) {
                        if (g.hasReminder) {
                            AlarmScheduler.schedule(getApplication(), g)
                        }
                    }
                    GoalAppWidgetProvider.updateAllWidgets(getApplication())
                }
            }
            onDone(success, msg)
        }
    }

    fun updateWidgets() {
        GoalAppWidgetProvider.updateAllWidgets(getApplication())
        Toast.makeText(getApplication(), "Ana ekran widget'ları güncellendi!", Toast.LENGTH_SHORT).show()
    }

    fun executeApiTest(endpoint: String) {
        viewModelScope.launch {
            val response = externalApiHub.executeSimulatedApiCall(endpoint, allGoals.value)
            _lastApiTestResult.value = response
        }
    }
}
