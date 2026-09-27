package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogSessionDialog(
    subjects: List<SubjectEntity>,
    initialSubjectId: Long? = null,
    initialDurationMinutes: Int = 45,
    initialSession: StudySessionEntity? = null,
    dialogTitle: String = "Log Study Session",
    onDismiss: () -> Unit,
    onSave: (subjectId: Long, startTimeMillis: Long, durationMinutes: Int, notes: String, rating: Int) -> Unit
) {
    var selectedSubjectId by remember {
        mutableLongStateOf(
            initialSession?.subjectId ?: initialSubjectId ?: subjects.firstOrNull()?.id ?: 0L
        )
    }
    var durationText by remember {
        mutableStateOf((initialSession?.durationMinutes ?: initialDurationMinutes).toString())
    }
    var notes by remember {
        mutableStateOf(initialSession?.notes ?: "")
    }
    var rating by remember {
        mutableIntStateOf(initialSession?.focusRating ?: 5)
    }
    var durationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Subject selection chips
                Column {
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { subject ->
                            val isSelected = selectedSubjectId == subject.id
                            val color = Color(subject.colorHex)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) color else color.copy(alpha = 0.12f)
                                    )
                                    .clickable { selectedSubjectId = subject.id }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else color
                                )
                            }
                        }
                    }
                }

                // Duration field
                OutlinedTextField(
                    value = durationText,
                    onValueChange = {
                        durationText = it.filter { char -> char.isDigit() }
                        if (durationError && durationText.isNotBlank()) durationError = false
                    },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = durationError,
                    supportingText = if (durationError) {
                        { Text("Please enter a duration greater than 0") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_duration_input")
                )

                // Quick duration presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25, 45, 60, 90).forEach { mins ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { durationText = mins.toString() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${mins}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. topics covered, exercises)") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_notes_input")
                )

                // Focus Rating
                Column {
                    Text(
                        text = "Focus Rating",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = { rating = i },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = "Rating $i",
                                    tint = if (i <= rating) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (rating) {
                                5 -> "Super Focused 🔥"
                                4 -> "Productive ✨"
                                3 -> "Moderate 👍"
                                2 -> "Distracted 🥱"
                                else -> "Tough Session 😴"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 0
                    if (duration <= 0) {
                        durationError = true
                    } else {
                        val startTime = initialSession?.startTime
                            ?: (System.currentTimeMillis() - duration * 60 * 1000L)
                        onSave(selectedSubjectId, startTime, duration, notes, rating)
                    }
                },
                modifier = Modifier.testTag("confirm_log_session_button")
            ) {
                Text("Save Session")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_log_session_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
