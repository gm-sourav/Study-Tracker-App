package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddSubjectDialog
import com.example.ui.components.LogSessionDialog
import com.example.ui.screens.ArchitectureScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel

enum class Screen(val title: String) {
    TIMER("Timer"),
    SUBJECTS("Subjects"),
    STATS("Stats"),
    HISTORY("History"),
    ARCHITECTURE("Architecture")
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                StudyTrackerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudyTrackerApp(viewModel: StudyViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.TIMER) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI States
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val filteredSessions by viewModel.filteredSessions.collectAsStateWithLifecycle()
    val streakInfo by viewModel.streakInfo.collectAsStateWithLifecycle()
    val weeklyOverview by viewModel.weeklyOverview.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchFilter.collectAsStateWithLifecycle()
    val selectedFilterSubjectId by viewModel.selectedFilterSubjectId.collectAsStateWithLifecycle()

    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val showAddSubjectDialog by viewModel.showAddSubjectDialog.collectAsStateWithLifecycle()
    val editingSubject by viewModel.editingSubject.collectAsStateWithLifecycle()
    val showLogSessionDialog by viewModel.showLogSessionDialog.collectAsStateWithLifecycle()
    val editingSession by viewModel.editingSession.collectAsStateWithLifecycle()

    // Handle snackbar messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Handle Back button to return to Timer screen if on other tabs
    if (currentScreen != Screen.TIMER) {
        BackHandler {
            currentScreen = Screen.TIMER
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("app_snackbar_host")
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.TIMER,
                    onClick = { currentScreen = Screen.TIMER },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Focus Timer") },
                    label = { Text("Timer") },
                    modifier = Modifier.testTag("nav_tab_timer")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.SUBJECTS,
                    onClick = { currentScreen = Screen.SUBJECTS },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Subjects") },
                    label = { Text("Subjects") },
                    modifier = Modifier.testTag("nav_tab_subjects")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.STATS,
                    onClick = { currentScreen = Screen.STATS },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics & Stats") },
                    label = { Text("Stats") },
                    modifier = Modifier.testTag("nav_tab_stats")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.HISTORY,
                    onClick = { currentScreen = Screen.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "Session History") },
                    label = { Text("History") },
                    modifier = Modifier.testTag("nav_tab_history")
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.ARCHITECTURE,
                    onClick = { currentScreen = Screen.ARCHITECTURE },
                    icon = { Icon(Icons.Default.Layers, contentDescription = "Architecture Guide") },
                    label = { Text("Arch") },
                    modifier = Modifier.testTag("nav_tab_architecture")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.TIMER -> TimerScreen(
                    timerState = timerState,
                    subjects = subjects,
                    streakInfo = streakInfo,
                    viewModel = viewModel,
                    onNavigateToSubjects = { currentScreen = Screen.SUBJECTS }
                )
                Screen.SUBJECTS -> SubjectsScreen(
                    subjects = subjects,
                    subjectStats = weeklyOverview.subjectBreakdown,
                    viewModel = viewModel,
                    onStartSubjectFocus = { subjectId ->
                        viewModel.selectSubjectForTimer(subjectId)
                        currentScreen = Screen.TIMER
                    }
                )
                Screen.STATS -> StatsScreen(
                    streakInfo = streakInfo,
                    weeklyOverview = weeklyOverview
                )
                Screen.HISTORY -> HistoryScreen(
                    sessions = filteredSessions,
                    subjects = subjects,
                    searchQuery = searchQuery,
                    selectedSubjectFilter = selectedFilterSubjectId,
                    viewModel = viewModel
                )
                Screen.ARCHITECTURE -> ArchitectureScreen()
            }
        }
    }

    // Add or Edit Subject Dialog
    if (showAddSubjectDialog) {
        AddSubjectDialog(
            initialSubject = editingSubject,
            onDismiss = {
                viewModel.showAddSubjectDialog.value = false
                viewModel.editingSubject.value = null
            },
            onSave = { name, colorHex, iconKey, weeklyGoalHours ->
                viewModel.saveSubject(name, colorHex, iconKey, weeklyGoalHours)
            }
        )
    }

    // Manual Log Session Dialog or Edit Session Dialog
    if (showLogSessionDialog) {
        LogSessionDialog(
            subjects = subjects,
            initialSubjectId = timerState.selectedSubjectId,
            initialSession = editingSession,
            dialogTitle = if (editingSession != null) "Edit Study Session" else "Log Past Session",
            onDismiss = {
                viewModel.showLogSessionDialog.value = false
                viewModel.editingSession.value = null
            },
            onSave = { subjectId, startTimeMillis, durationMinutes, notes, rating ->
                val editing = editingSession
                if (editing != null) {
                    viewModel.updateSession(
                        editing.copy(
                            subjectId = subjectId,
                            startTime = startTimeMillis,
                            durationMinutes = durationMinutes,
                            notes = notes,
                            focusRating = rating
                        )
                    )
                    viewModel.showLogSessionDialog.value = false
                } else {
                    viewModel.logManualSession(
                        subjectId = subjectId,
                        startTimeMillis = startTimeMillis,
                        durationMinutes = durationMinutes,
                        notes = notes,
                        rating = rating
                    )
                }
            }
        )
    }
}
