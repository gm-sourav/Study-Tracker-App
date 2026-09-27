package com.example.data.repository

import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.model.DayStudyStat
import com.example.data.model.SessionWithSubject
import com.example.data.model.StreakInfo
import com.example.data.model.SubjectStats
import com.example.data.model.WeeklyOverview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class StudyRepository(
    private val subjectDao: SubjectDao,
    private val sessionDao: StudySessionDao,
    private val goalDao: GoalDao
) {
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    val allSessions: Flow<List<StudySessionEntity>> = sessionDao.getAllSessions()

    val sessionsWithSubject: Flow<List<SessionWithSubject>> = combine(
        sessionDao.getAllSessions(),
        subjectDao.getAllSubjects()
    ) { sessions, subjects ->
        val subjectMap = subjects.associateBy { it.id }
        sessions.map { session ->
            SessionWithSubject(
                session = session,
                subject = subjectMap[session.subjectId]
            )
        }
    }

    val streakInfo: Flow<StreakInfo> = sessionDao.getAllSessions().combine(allSubjects) { sessions, _ ->
        calculateStreak(sessions)
    }

    val weeklyOverview: Flow<WeeklyOverview> = combine(
        sessionDao.getAllSessions(),
        subjectDao.getAllSubjects()
    ) { sessions, subjects ->
        calculateWeeklyOverview(sessions, subjects)
    }

    suspend fun insertSubject(subject: SubjectEntity): Long = subjectDao.insertSubject(subject)

    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.updateSubject(subject)

    suspend fun deleteSubject(subject: SubjectEntity) {
        sessionDao.deleteSessionsBySubject(subject.id)
        subjectDao.deleteSubject(subject)
    }

    suspend fun insertSession(session: StudySessionEntity): Long = sessionDao.insertSession(session)

    suspend fun updateSession(session: StudySessionEntity) = sessionDao.updateSession(session)

    suspend fun deleteSession(session: StudySessionEntity) = sessionDao.deleteSession(session)

    suspend fun deleteSessionById(id: Long) = sessionDao.deleteSessionById(id)

    private fun calculateStreak(sessions: List<StudySessionEntity>): StreakInfo {
        if (sessions.isEmpty()) {
            return StreakInfo(0, 0, false, null)
        }

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val studyDates = sessions.map {
            Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
        }.toSet()

        val studiedToday = studyDates.contains(today)

        // Calculate current streak
        var currentStreak = 0
        var checkDate = if (studiedToday) today else today.minusDays(1)

        while (studyDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // If user didn't study today and didn't study yesterday, current streak is 0
        if (!studiedToday && !studyDates.contains(today.minusDays(1))) {
            currentStreak = 0
        }

        // Calculate longest streak
        val sortedDates = studyDates.sorted()
        var longestStreak = 0
        var tempStreak = 0
        var prevDate: LocalDate? = null

        for (date in sortedDates) {
            if (prevDate == null || date == prevDate.plusDays(1)) {
                tempStreak++
            } else if (date != prevDate) {
                tempStreak = 1
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            prevDate = date
        }

        val latestSession = sessions.maxByOrNull { it.startTime }

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = longestStreak.coerceAtLeast(currentStreak),
            studiedToday = studiedToday,
            lastStudyDateMillis = latestSession?.startTime
        )
    }

    private fun calculateWeeklyOverview(
        sessions: List<StudySessionEntity>,
        subjects: List<SubjectEntity>
    ): WeeklyOverview {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        // Last 7 days stats for bar chart
        val dailyStats = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val daySessions = sessions.filter {
                val sessionDate = Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
                sessionDate == date
            }
            val minutes = daySessions.sumOf { it.durationMinutes }
            val label = if (offset == 0) "Today" else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val dateMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
            DayStudyStat(
                dayLabel = label,
                dateMillis = dateMillis,
                totalMinutes = minutes
            )
        }

        // Total minutes today
        val todayMinutes = sessions.filter {
            Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate() == today
        }.sumOf { it.durationMinutes }

        // Total minutes this calendar week
        val thisWeekMinutes = sessions.filter {
            val date = Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
            !date.isBefore(startOfWeek) && !date.isAfter(today)
        }.sumOf { it.durationMinutes }

        // Breakdown per subject
        val subjectStatsList = subjects.map { subject ->
            val subjectSessions = sessions.filter { it.subjectId == subject.id }
            val totalMin = subjectSessions.sumOf { it.durationMinutes }
            val weekMin = subjectSessions.filter {
                val date = Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
                !date.isBefore(startOfWeek) && !date.isAfter(today)
            }.sumOf { it.durationMinutes }
            val targetMinutes = (subject.weeklyGoalHours * 60f).toInt().coerceAtLeast(1)
            val progressPercent = (weekMin.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f)

            SubjectStats(
                subject = subject,
                totalMinutes = totalMin,
                weeklyMinutes = weekMin,
                sessionCount = subjectSessions.size,
                goalProgressPercent = progressPercent
            )
        }.sortedByDescending { it.weeklyMinutes }

        return WeeklyOverview(
            totalMinutesThisWeek = thisWeekMinutes,
            totalMinutesToday = todayMinutes,
            dailyStats = dailyStats,
            subjectBreakdown = subjectStatsList
        )
    }
}
