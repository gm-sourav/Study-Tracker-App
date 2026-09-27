package com.example.data.model

import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity

data class StreakInfo(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val studiedToday: Boolean = false,
    val lastStudyDateMillis: Long? = null
)

data class SubjectStats(
    val subject: SubjectEntity,
    val totalMinutes: Int,
    val weeklyMinutes: Int,
    val sessionCount: Int,
    val goalProgressPercent: Float
)

data class DayStudyStat(
    val dayLabel: String, // "Mon", "Tue", etc.
    val dateMillis: Long,
    val totalMinutes: Int
)

data class WeeklyOverview(
    val totalMinutesThisWeek: Int = 0,
    val totalMinutesToday: Int = 0,
    val dailyStats: List<DayStudyStat> = emptyList(),
    val subjectBreakdown: List<SubjectStats> = emptyList()
)

data class SessionWithSubject(
    val session: StudySessionEntity,
    val subject: SubjectEntity?
)
