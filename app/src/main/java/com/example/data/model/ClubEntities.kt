package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "coaches")
data class Coach(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val title: String,
    val beltName: String = "NOIRE",
    val danGrade: String = "1er Dan",
    val phone: String = "",
    val photoUrl: String = "",
    val isActive: Boolean = true
) {
    val belt: Belt
        get() = Belt.fromString(beltName)
}

@Entity(tableName = "categories")
data class ClubCategory(
    @PrimaryKey
    val code: String, // e.g. "U15", "EVEIL"
    val label: String, // e.g. "Minimes (U15)"
    val ageRange: String, // e.g. "12-13 ans"
    val schedule: String = "Mardi - Jeudi 18h00",
    val isActive: Boolean = true
)

@Entity(
    tableName = "coach_category_assignments",
    primaryKeys = ["coachId", "categoryCode"]
)
data class CoachCategoryAssignment(
    val coachId: Long,
    val categoryCode: String
)

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryCode: String,
    val dateString: String, // "2026-10-08"
    val startTime: String, // "18:00"
    val endTime: String = "", // "19:30"
    val durationMinutes: Int = 0,
    val presentCoachNames: String = "", // Comma-separated: "Ahmed BOURENANE, Karim MEZIANI"
    val presentCount: Int = 0,
    val totalCategoryMembers: Int = 0,
    val notes: String = "",
    val status: String = "COMPLETED" // "IN_PROGRESS", "COMPLETED"
) {
    val attendanceRate: Int
        get() = if (totalCategoryMembers > 0) {
            ((presentCount.toDouble() / totalCategoryMembers.toDouble()) * 100).toInt()
        } else 0
}
