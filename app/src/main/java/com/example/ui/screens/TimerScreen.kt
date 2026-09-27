package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SubjectEntity
import com.example.data.model.StreakInfo
import com.example.ui.components.SubjectBadge
import com.example.ui.viewmodel.PomodoroPhase
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.TimerMode
import com.example.ui.viewmodel.TimerUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    timerState: TimerUiState,
    subjects: List<SubjectEntity>,
    streakInfo: StreakInfo,
    viewModel: StudyViewModel,
    onNavigateToSubjects: () -> Unit
) {
    val scrollState = rememberScrollState()
    val activeSubject = subjects.find { it.id == timerState.selectedSubjectId } ?: subjects.firstOrNull()

    var showFinishDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Streak & Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Focus Station",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (timerState.mode == TimerMode.POMODORO) "Pomodoro Technique" else "Continuous Stopwatch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Streak Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFEF3C7))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${streakInfo.currentStreak} Day Streak",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer Mode Switcher (Pomodoro vs Stopwatch)
        PrimaryTabRow(
            selectedTabIndex = if (timerState.mode == TimerMode.POMODORO) 0 else 1,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = timerState.mode == TimerMode.POMODORO,
                onClick = { viewModel.setTimerMode(TimerMode.POMODORO) },
                text = { Text("Pomodoro (25/5)") },
                modifier = Modifier.testTag("tab_pomodoro")
            )
            Tab(
                selected = timerState.mode == TimerMode.STOPWATCH,
                onClick = { viewModel.setTimerMode(TimerMode.STOPWATCH) },
                text = { Text("Stopwatch") },
                modifier = Modifier.testTag("tab_stopwatch")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Selector Carousel
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Studying Subject",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onNavigateToSubjects() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (subjects.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showAddSubjectDialog.value = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add your first subject (e.g. OS, DSA, DBMS)")
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { subject ->
                        val isSelected = (timerState.selectedSubjectId ?: subjects.firstOrNull()?.id) == subject.id
                        val subjectColor = Color(subject.colorHex)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) subjectColor else subjectColor.copy(alpha = 0.12f))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.selectSubjectForTimer(subject.id) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("select_subject_${subject.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else subjectColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pomodoro Cycle Indicator (if Pomodoro mode)
        if (timerState.mode == TimerMode.POMODORO) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = timerState.pomodoroPhase.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (timerState.pomodoroPhase) {
                        PomodoroPhase.STUDY -> MaterialTheme.colorScheme.primary
                        PomodoroPhase.SHORT_BREAK -> Color(0xFF059669)
                        PomodoroPhase.LONG_BREAK -> Color(0xFF0284C7)
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                // 4 Round Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i <= timerState.cycleCount) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
            }
        }

        // Circular Timer Gauge
        val progress = remember(timerState) {
            if (timerState.mode == TimerMode.POMODORO) {
                if (timerState.totalPhaseSeconds > 0) {
                    (timerState.remainingSeconds.toFloat() / timerState.totalPhaseSeconds.toFloat()).coerceIn(0f, 1f)
                } else 1f
            } else {
                // In stopwatch, animate pulsing 60-second loop
                ((timerState.elapsedSeconds % 60) / 60f)
            }
        }

        val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")
        val ringColor by animateColorAsState(
            targetValue = when {
                timerState.mode == TimerMode.STOPWATCH -> MaterialTheme.colorScheme.secondary
                timerState.pomodoroPhase == PomodoroPhase.STUDY -> MaterialTheme.colorScheme.primary
                else -> Color(0xFF10B981)
            },
            label = "ring_color"
        )
        val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                // Background track
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Progress Arc
                val sweep = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ringColor, ringColor.copy(alpha = 0.8f))
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Inside Timer Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val displaySeconds = if (timerState.mode == TimerMode.POMODORO) {
                    timerState.remainingSeconds
                } else {
                    timerState.elapsedSeconds
                }

                val mins = displaySeconds / 60
                val secs = displaySeconds % 60
                val timeString = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

                Text(
                    text = timeString,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("timer_display_text")
                )

                if (activeSubject != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SubjectBadge(subject = activeSubject, showIcon = true)
                }

                if (timerState.isPaused) {
                    Text(
                        text = "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            IconButton(
                onClick = { viewModel.resetTimer() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("btn_reset_timer")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Primary Start / Pause / Resume Button
            Button(
                onClick = {
                    if (timerState.isRunning) {
                        viewModel.pauseTimer()
                    } else if (timerState.isPaused) {
                        viewModel.resumeTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .testTag("btn_toggle_timer"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerState.isRunning) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Finish & Save Session Button
            FilledTonalButton(
                onClick = {
                    showFinishDialog = true
                },
                enabled = timerState.elapsedSeconds > 10 || timerState.isPaused || timerState.isRunning,
                modifier = Modifier.testTag("btn_finish_timer")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Motivational Tip Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💡 Study Tip",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (timerState.mode == TimerMode.POMODORO) {
                        "Break complex subjects (like OS or DSA) into 25-minute sprints. Take 5 minutes away from screens during breaks to consolidate memory!"
                    } else {
                        "Deep work stopwatch mode: keep your phone on Do Not Disturb and log your key insights immediately after finishing."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Finish & Save Review Dialog
    if (showFinishDialog) {
        val targetSubjectId = timerState.selectedSubjectId ?: subjects.firstOrNull()?.id ?: 1L
        var notes by remember { mutableStateOf(timerState.sessionNotes) }
        var rating by remember { mutableStateOf(5) }
        val minutesSpent = (timerState.elapsedSeconds / 60).toInt().coerceAtLeast(1)

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Log Study Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "You studied for $minutesSpent minutes on ${activeSubject?.name ?: "Subject"}!",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("What did you accomplish?") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("finish_notes_input")
                    )

                    Text("Focus Quality:", style = MaterialTheme.typography.labelMedium)
                    Row {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Rating $star",
                                    tint = if (star <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.finishAndSaveTimerSession(
                            subjectId = targetSubjectId,
                            notes = notes,
                            rating = rating
                        )
                        showFinishDialog = false
                    },
                    modifier = Modifier.testTag("confirm_finish_button")
                ) {
                    Text("Save to Log")
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
                    onClick = { showFinishDialog = false }
                ) {
                    Text("Keep Studying")
                }
            }
        )
    }
}
