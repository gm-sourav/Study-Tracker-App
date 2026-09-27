package com.example

import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSubjectEntityCreation() {
        val subject = SubjectEntity(
            id = 1L,
            name = "Operating Systems",
            colorHex = 0xFF059669,
            iconKey = "terminal",
            weeklyGoalHours = 6.0f
        )
        assertEquals("Operating Systems", subject.name)
        assertEquals(6.0f, subject.weeklyGoalHours)
    }

    @Test
    fun testStudySessionEntityCreation() {
        val now = System.currentTimeMillis()
        val session = StudySessionEntity(
            id = 1L,
            subjectId = 1L,
            startTime = now - 3600000,
            endTime = now,
            durationMinutes = 60,
            sessionType = "POMODORO",
            notes = "Virtual memory paging",
            focusRating = 5
        )
        assertEquals(60, session.durationMinutes)
        assertEquals("POMODORO", session.sessionType)
        assertEquals(5, session.focusRating)
        assertNotNull(session.notes)
    }
}
