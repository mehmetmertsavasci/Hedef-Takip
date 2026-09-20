package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GoalViewModel
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.CloudSecurityScreen
import com.example.ui.screens.CustomizationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.GoalTrackerTheme

enum class MainNavigationTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    HOME("Hedefler", Icons.Default.TrackChanges, "nav_tab_home"),
    STATS("Grafikler", Icons.Default.QueryStats, "nav_tab_stats"),
    ALARMS("Alarmlar", Icons.Default.Alarm, "nav_tab_alarms"),
    CLOUD("Bulut & API", Icons.Default.Security, "nav_tab_cloud"),
    THEMES("Temalar", Icons.Default.Palette, "nav_tab_themes")
}

class MainActivity : ComponentActivity() {

    private val viewModel: GoalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()

            // Notification permission request for Android 13+ (API 33+)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Permission handled gracefully
            }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            GoalTrackerTheme(themeMode = themeMode, darkMode = darkMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: GoalViewModel) {
    var selectedTab by remember { mutableStateOf(MainNavigationTab.HOME) }

    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val filteredGoals by viewModel.filteredGoals.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val passphrase by viewModel.e2eePassphrase.collectAsStateWithLifecycle()
    val lastApiTestResult by viewModel.lastApiTestResult.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MainNavigationTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (selectedTab) {
            MainNavigationTab.HOME -> {
                HomeScreen(
                    goals = filteredGoals,
                    selectedCategory = selectedCategory,
                    syncState = syncState,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    onAddGoal = { viewModel.addOrUpdateGoal(it) },
                    onUpdateGoal = { viewModel.addOrUpdateGoal(it) },
                    onDeleteGoal = { viewModel.deleteGoal(it) },
                    onIncrement = { viewModel.incrementProgress(it) },
                    onDecrement = { viewModel.decrementProgress(it) },
                    onToggleComplete = { viewModel.toggleComplete(it) },
                    onTestAlarm = { viewModel.testAlarmNotification(it) },
                    modifier = screenModifier
                )
            }

            MainNavigationTab.STATS -> {
                StatsScreen(
                    goals = allGoals,
                    modifier = screenModifier
                )
            }

            MainNavigationTab.ALARMS -> {
                AlarmsScreen(
                    goals = allGoals,
                    onTestAlarm = { viewModel.testAlarmNotification(it) },
                    modifier = screenModifier
                )
            }

            MainNavigationTab.CLOUD -> {
                CloudSecurityScreen(
                    syncState = syncState,
                    passphrase = passphrase,
                    externalApiHub = viewModel.externalApiHub,
                    lastApiTestResult = lastApiTestResult,
                    onSavePassphrase = { viewModel.setPassphrase(it) },
                    onPushSync = { onDone -> viewModel.syncToCloud(onDone) },
                    onPullSync = { onDone -> viewModel.restoreFromCloud(onDone) },
                    onExecuteApiTest = { viewModel.executeApiTest(it) },
                    modifier = screenModifier
                )
            }

            MainNavigationTab.THEMES -> {
                CustomizationScreen(
                    currentTheme = themeMode,
                    currentDarkMode = darkMode,
                    goals = allGoals,
                    onSelectTheme = { viewModel.setTheme(it) },
                    onSelectDarkMode = { viewModel.setDarkMode(it) },
                    onUpdateWidgets = { viewModel.updateWidgets() },
                    modifier = screenModifier
                )
            }
        }
    }
}
