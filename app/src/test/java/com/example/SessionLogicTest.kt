package com.example

import com.example.data.model.Category
import com.example.data.model.Coach
import com.example.data.model.Member
import com.example.data.model.TrainingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class SessionLogicTest {

    @Test
    fun testCoachAssignmentCheck() {
        val coach = Coach(
            id = 1,
            name = "Ahmed BOURENANE",
            assignedCategoryCodes = "EVEIL,U13,U15,SENIOR"
        )

        assertTrue(coach.isAssignedTo("EVEIL"))
        assertTrue(coach.isAssignedTo("U13"))
        assertTrue(coach.isAssignedTo("u15")) // Case insensitive
        assertFalse(coach.isAssignedTo("U11"))
        assertFalse(coach.isAssignedTo("U18"))

        val adminCoach = Coach(
            id = 2,
            name = "Directeur Technique",
            role = "ADMIN",
            assignedCategoryCodes = ""
        )
        assertTrue(adminCoach.isAssignedTo("U11"))
        assertTrue(adminCoach.isAssignedTo("ANY"))
    }

    @Test
    fun testSessionDurationCalculation() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val start = "17:30"
        val end = "19:00"

        val d1 = sdf.parse(start)!!
        val d2 = sdf.parse(end)!!
        val diffMinutes = ((d2.time - d1.time) / (1000 * 60)).toInt()

        assertEquals(90, diffMinutes)
    }

    @Test
    fun testSessionPercentageCalculationSafely() {
        val emptySession = TrainingSession(
            categoryId = 1,
            categoryCode = "U15",
            categoryName = "Minimes (U15)",
            dateString = "2026-10-08",
            startTime = "18:00",
            totalRegisteredAthletes = 0,
            presentCount = 0,
            absentCount = 0
        )
        // Guard against division by zero
        assertEquals(0, emptySession.attendancePercentage)
        assertEquals(0, emptySession.absentPercentage)

        val activeSession = TrainingSession(
            categoryId = 1,
            categoryCode = "U15",
            categoryName = "Minimes (U15)",
            dateString = "2026-10-08",
            startTime = "18:00",
            totalRegisteredAthletes = 10,
            presentCount = 8,
            absentCount = 2
        )
        assertEquals(80, activeSession.attendancePercentage)
        assertEquals(20, activeSession.absentPercentage)
    }

    @Test
    fun testDouchetteInputTrimming() {
        val rawScannedWithNewline = "JS-2026-001\n"
        val cleaned = rawScannedWithNewline.trim()
        assertEquals("JS-2026-001", cleaned)

        val rawWithReturn = "JS-2026-002\r\n"
        assertEquals("JS-2026-002", rawWithReturn.trim())
    }
}
