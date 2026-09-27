package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.model.SessionWithSubject
import com.example.data.model.StreakInfo
import com.example.data.model.WeeklyOverview
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TimerMode {
    POMODORO,
    STOPWATCH
}

enum class PomodoroPhase(val defaultMinutes: Int, val title: String) {
    STUDY(25, "Study Focus"),
    SHORT_BREAK(5, "Short Break"),
    LONG_BREAK(15, "Long Break")
}

data class TimerUiState(
    val mode: TimerMode = TimerMode.POMODORO,
    val pomodoroPhase: PomodoroPhase = PomodoroPhase.STUDY,
    val cycleCount: Int = 1, // 1 to 4
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val remainingSeconds: Long = 25 * 60L,
    val elapsedSeconds: Long = 0L,
    val totalPhaseSeconds: Long = 25 * 60L,
    val selectedSubjectId: Long? = null,
    val sessionNotes: String = "",
    val sessionRating: Int = 5,
    val sessionStartTimeMillis: Long? = null
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = StudyRepository(
        subjectDao = database.subjectDao(),
        sessionDao = database.studySessionDao(),
        goalDao = database.goalDao()
    )

    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _rawSessions = repository.sessionsWithSubject
    private val _searchFilter = MutableStateFlow("")
    private val _selectedFilterSubjectId = MutableStateFlow<Long?>(null)

    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()
    val selectedFilterSubjectId: StateFlow<Long?> = _selectedFilterSubjectId.asStateFlow()

    val filteredSessions: StateFlow<List<SessionWithSubject>> = combine(
        _rawSessions,
        _searchFilter,
        _selectedFilterSubjectId
    ) { sessions, query, subjectId ->
        sessions.filter { item ->
            val matchesSubject = subjectId == null || item.session.subjectId == subjectId
            val matchesQuery = query.isBlank() ||
                    item.subject?.name?.contains(query, ignoreCase = true) == true ||
                    item.session.notes.contains(query, ignoreCase = true) ||
                    item.session.sessionType.contains(query, ignoreCase = true)
            matchesSubject && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val streakInfo: StateFlow<StreakInfo> = repository.streakInfo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StreakInfo()
        )

    val weeklyOverview: StateFlow<WeeklyOverview> = repository.weeklyOverview
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyOverview()
        )

    // Timer state
    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    // UI Feedback state
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Dialog states
    var showAddSubjectDialog = MutableStateFlow(false)
    var editingSubject = MutableStateFlow<SubjectEntity?>(null)

    var showLogSessionDialog = MutableStateFlow(false)
    var editingSession = MutableStateFlow<StudySessionEntity?>(null)

    var showSessionFinishedDialog = MutableStateFlow(false)
    var finishedSessionSummary = MutableStateFlow<StudySessionEntity?>(null)

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setSearchFilter(query: String) {
        _searchFilter.value = query
    }

    fun setFilterSubjectId(subjectId: Long?) {
        _selectedFilterSubjectId.value = subjectId
    }

    // --- Timer Controls ---

    fun setTimerMode(mode: TimerMode) {
        if (_timerState.value.isRunning) return
        val defaultMinutes = if (mode == TimerMode.POMODORO) PomodoroPhase.STUDY.defaultMinutes else 0
        _timerState.value = _timerState.value.copy(
            mode = mode,
            pomodoroPhase = PomodoroPhase.STUDY,
            cycleCount = 1,
            remainingSeconds = defaultMinutes * 60L,
            totalPhaseSeconds = defaultMinutes * 60L,
            elapsedSeconds = 0L,
            isRunning = false,
            isPaused = false
        )
    }

    fun selectSubjectForTimer(subjectId: Long?) {
        _timerState.value = _timerState.value.copy(selectedSubjectId = subjectId)
    }

    fun setSessionNotes(notes: String) {
        _timerState.value = _timerState.value.copy(sessionNotes = notes)
    }

    fun setSessionRating(rating: Int) {
        _timerState.value = _timerState.value.copy(sessionRating = rating)
    }

    fun startTimer() {
        val current = _timerState.value
        if (current.isRunning) return

        val startTime = current.sessionStartTimeMillis ?: System.currentTimeMillis()
        _timerState.value = current.copy(
            isRunning = true,
            isPaused = false,
            sessionStartTimeMillis = startTime
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning && !_timerState.value.isPaused) {
                delay(1000L)
                tickTimer()
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(
            isPaused = true,
            isRunning = false
        )
        timerJob?.cancel()
    }

    fun resumeTimer() {
        startTimer()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val current = _timerState.value
        val defaultSec = if (current.mode == TimerMode.POMODORO) current.pomodoroPhase.defaultMinutes * 60L else 0L
        _timerState.value = current.copy(
            isRunning = false,
            isPaused = false,
            remainingSeconds = defaultSec,
            elapsedSeconds = 0L,
            sessionStartTimeMillis = null
        )
    }

    private fun tickTimer() {
        val current = _timerState.value
        if (current.mode == TimerMode.STOPWATCH) {
            _timerState.value = current.copy(
                elapsedSeconds = current.elapsedSeconds + 1
            )
        } else {
            // Pomodoro Mode
            if (current.remainingSeconds > 1) {
                _timerState.value = current.copy(
                    remainingSeconds = current.remainingSeconds - 1,
                    elapsedSeconds = current.elapsedSeconds + 1
                )
            } else {
                // Phase completed!
                onPomodoroPhaseFinished()
            }
        }
    }

    private fun onPomodoroPhaseFinished() {
        timerJob?.cancel()
        vibrateDevice()

        val current = _timerState.value
        if (current.pomodoroPhase == PomodoroPhase.STUDY) {
            // Study phase completed! Prompt user to save or proceed to break
            val durationMinutes = (current.totalPhaseSeconds / 60).toInt().coerceAtLeast(1)
            val session = StudySessionEntity(
                subjectId = current.selectedSubjectId ?: subjects.value.firstOrNull()?.id ?: 1L,
                startTime = current.sessionStartTimeMillis ?: System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                sessionType = "POMODORO",
                notes = current.sessionNotes.ifBlank { "Pomodoro Focus #${current.cycleCount}" },
                focusRating = current.sessionRating
            )

            viewModelScope.launch {
                repository.insertSession(session)
                _snackbarMessage.value = "Great job! ${durationMinutes}m Pomodoro study session logged!"
            }

            // Determine next phase: every 4th cycle gives long break
            val nextCycle = if (current.cycleCount >= 4) 1 else current.cycleCount + 1
            val nextPhase = if (current.cycleCount >= 4) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
            val nextSeconds = nextPhase.defaultMinutes * 60L

            _timerState.value = current.copy(
                pomodoroPhase = nextPhase,
                cycleCount = nextCycle,
                isRunning = false,
                isPaused = false,
                remainingSeconds = nextSeconds,
                totalPhaseSeconds = nextSeconds,
                elapsedSeconds = 0L,
                sessionStartTimeMillis = null,
                sessionNotes = ""
            )
        } else {
            // Break ended, back to STUDY
            val studySeconds = PomodoroPhase.STUDY.defaultMinutes * 60L
            _timerState.value = current.copy(
                pomodoroPhase = PomodoroPhase.STUDY,
                isRunning = false,
                isPaused = false,
                remainingSeconds = studySeconds,
                totalPhaseSeconds = studySeconds,
                elapsedSeconds = 0L,
                sessionStartTimeMillis = null
            )
            _snackbarMessage.value = "Break is over! Ready for the next study session?"
        }
    }

    fun finishAndSaveTimerSession(
        subjectId: Long,
        notes: String,
        rating: Int
    ) {
        val current = _timerState.value
        val elapsedMins = (current.elapsedSeconds / 60).toInt()
        val durationMins = if (elapsedMins > 0) elapsedMins else 1

        val session = StudySessionEntity(
            subjectId = subjectId,
            startTime = current.sessionStartTimeMillis ?: (System.currentTimeMillis() - durationMins * 60 * 1000L),
            endTime = System.currentTimeMillis(),
            durationMinutes = durationMins,
            sessionType = if (current.mode == TimerMode.POMODORO) "POMODORO" else "STOPWATCH",
            notes = notes,
            focusRating = rating
        )

        viewModelScope.launch {
            repository.insertSession(session)
            resetTimer()
            _snackbarMessage.value = "Study session saved ($durationMins min)!"
        }
    }

    private fun vibrateDevice() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(600)
            }
        } catch (_: Exception) {
            // Graceful fallback if permission or hardware not available
        }
    }

    // --- Subject Operations ---

    fun saveSubject(name: String, colorHex: Long, iconKey: String, weeklyGoalHours: Float) {
        val editing = editingSubject.value
        viewModelScope.launch {
            if (editing != null) {
                repository.updateSubject(
                    editing.copy(
                        name = name.trim(),
                        colorHex = colorHex,
                        iconKey = iconKey,
                        weeklyGoalHours = weeklyGoalHours
                    )
                )
                _snackbarMessage.value = "Subject '${name.trim()}' updated"
            } else {
                repository.insertSubject(
                    SubjectEntity(
                        name = name.trim(),
                        colorHex = colorHex,
                        iconKey = iconKey,
                        weeklyGoalHours = weeklyGoalHours
                    )
                )
                _snackbarMessage.value = "Subject '${name.trim()}' added"
            }
            editingSubject.value = null
            showAddSubjectDialog.value = false
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _snackbarMessage.value = "Subject '${subject.name}' and associated logs deleted"
        }
    }

    // --- Session Operations ---

    fun logManualSession(
        subjectId: Long,
        startTimeMillis: Long,
        durationMinutes: Int,
        notes: String,
        rating: Int
    ) {
        val session = StudySessionEntity(
            subjectId = subjectId,
            startTime = startTimeMillis,
            endTime = startTimeMillis + durationMinutes * 60 * 1000L,
            durationMinutes = durationMinutes.coerceAtLeast(1),
            sessionType = "MANUAL",
            notes = notes.trim(),
            focusRating = rating
        )
        viewModelScope.launch {
            repository.insertSession(session)
            _snackbarMessage.value = "Session logged ($durationMinutes min)"
            showLogSessionDialog.value = false
        }
    }

    fun updateSession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.updateSession(session)
            _snackbarMessage.value = "Session updated"
            editingSession.value = null
        }
    }

    fun deleteSession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
            _snackbarMessage.value = "Study session removed"
        }
    }
}
