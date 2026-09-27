package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    indices = [Index(value = ["subjectId"]), Index(value = ["startTime"])]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val sessionType: String, // "POMODORO", "STOPWATCH", "MANUAL"
    val notes: String = "",
    val focusRating: Int = 5 // 1 to 5
)
