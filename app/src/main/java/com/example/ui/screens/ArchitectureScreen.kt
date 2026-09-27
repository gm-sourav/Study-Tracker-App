package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ArchitectureScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Architecture & Design",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tutor Guide: Separation of Concerns with MVVM, Clean Layers & Room",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Summary Hero
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("arch_overview_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Why Not Put Everything in One Activity?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "If all database queries, timers, and UI drawing were packed into `MainActivity`:\n" +
                            "• Rotating your phone destroys the Activity and loses your timer state.\n" +
                            "• UI code becomes unreadable (1000+ lines of spaghetti).\n" +
                            "• Testing business logic (streak rules, goal percentages) becomes impossible without launching the full UI.\n\n" +
                            "Separation of concerns gives each layer ONE responsibility.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }

        // Layer Pipeline Flow Diagram
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Architecture Diagram (Clean MVVM)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                ArchitectureFlowStep(
                    layerName = "1. UI Layer (Jetpack Compose)",
                    responsibility = "TimerScreen, SubjectsScreen, StatsScreen observe StateFlow reactively and emit user events (e.g. startTimer, addSubject).",
                    color = Color(0xFF4F46E5),
                    icon = Icons.Default.Terminal
                )

                FlowArrow()

                ArchitectureFlowStep(
                    layerName = "2. ViewModel Layer",
                    responsibility = "StudyViewModel holds UI State, drives ticker coroutines, survives configuration changes, and formats data for Compose.",
                    color = Color(0xFF059669),
                    icon = Icons.Default.AccountTree
                )

                FlowArrow()

                ArchitectureFlowStep(
                    layerName = "3. Domain & Repository",
                    responsibility = "StudyRepository coordinates between local Room DB and business calculations (consecutive streak, 7-day stats).",
                    color = Color(0xFFD97706),
                    icon = Icons.Default.DataObject
                )

                FlowArrow()

                ArchitectureFlowStep(
                    layerName = "4. Data Layer (Room SQLite)",
                    responsibility = "Entities (SubjectEntity, StudySessionEntity) + DAOs return reactive Flow streams that auto-notify on inserts/deletions.",
                    color = Color(0xFF7C3AED),
                    icon = Icons.Default.Storage
                )
            }
        }

        // Expandable Deep Dives
        Text(
            text = "Deep Dive by Layer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ExpandableLessonCard(
            title = "UI Layer: Unidirectional Data Flow (UDF)",
            icon = Icons.Default.Terminal,
            summary = "State flows DOWN, Events flow UP",
            details = "• Compose functions are stateless renderers of `TimerUiState`.\n" +
                    "• When the user taps 'Start', Compose calls `viewModel.startTimer()`.\n" +
                    "• Compose NEVER touches `AppDatabase` or SQLite directly.\n" +
                    "• Using `collectAsStateWithLifecycle()` ensures the timer doesn't waste CPU when the screen is in the background."
        )

        ExpandableLessonCard(
            title = "ViewModel: Surviving Lifecycle Changes",
            icon = Icons.Default.AccountTree,
            summary = "Keeps active timers running across screen rotations",
            details = "• Android Activities are destroyed and recreated during screen rotations or split-screen.\n" +
                    "• `StudyViewModel` survives rotation because its lifecycle is tied to the ViewModelStore, not the Activity.\n" +
                    "• Uses `viewModelScope` to automatically cancel background jobs when the user navigates away."
        )

        ExpandableLessonCard(
            title = "Room Database: Single Source of Truth",
            icon = Icons.Default.Storage,
            summary = "Offline-first persistence with reactive Kotlin Flows",
            details = "• `@Dao` functions return `Flow<List<T>>` instead of one-shot queries.\n" +
                    "• Whenever a study session is inserted or deleted, Room automatically re-executes the query and pushes fresh lists to the UI.\n" +
                    "• `@Entity` definitions automatically map Kotlin data classes to SQLite relational tables."
        )

        ExpandableLessonCard(
            title = "Extending with Hilt & Backend Sync",
            icon = Icons.Default.Lightbulb,
            summary = "How this connects to your Spring Boot backend",
            details = "• In the next phase, add `@HiltViewModel` and `@Inject` to inject `StudyRepository` into `StudyViewModel` seamlessly.\n" +
                    "• To sync study logs to a Spring Boot / REST backend, simply add a Retrofit `StudyApiService` into `StudyRepository`.\n" +
                    "• The UI layer will NOT need to change at all, because it only observes `StudyRepository`!"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ArchitectureFlowStep(
    layerName: String,
    responsibility: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = layerName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = responsibility, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FlowArrow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ExpandableLessonCard(
    title: String,
    icon: ImageVector,
    summary: String,
    details: String
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = details,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Default, fontSize = 12.sp, lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
