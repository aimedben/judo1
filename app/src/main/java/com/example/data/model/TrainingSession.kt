package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_sessions",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["dateString"]),
        Index(value = ["status"])
    ]
)
data class TrainingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val categoryCode: String,
    val categoryName: String,
    val dateString: String, // "YYYY-MM-DD"
    val startTime: String, // "HH:mm"
    val endTime: String = "", // "HH:mm"
    val durationMinutes: Int = 0,
    val coach1Name: String = "",
    val coach2Name: String = "",
    val coach1Present: Boolean = true,
    val coach2Present: Boolean = false,
    val presentCoachNames: String = "", // e.g. "Sensei Ahmed BOURENANE"
    val status: String = "IN_PROGRESS", // "IN_PROGRESS" or "COMPLETED"
    val totalRegisteredAthletes: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L
) {
    val isCompleted: Boolean get() = status == "COMPLETED"
    val isInProgress: Boolean get() = status == "IN_PROGRESS"

    val attendancePercentage: Int
        get() = if (totalRegisteredAthletes > 0) {
            ((presentCount.toDouble() / totalRegisteredAthletes.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else 0

    val absentPercentage: Int
        get() = if (totalRegisteredAthletes > 0) {
            ((absentCount.toDouble() / totalRegisteredAthletes.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else 0
}
