package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coaches")
data class Coach(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val pinCode: String = "0000",
    val role: String = "COACH", // "COACH" or "ADMIN"
    val assignedCategoryCodes: String = "", // e.g. "U13,U15"
    val isActive: Boolean = true
) {
    fun isAssignedTo(categoryCode: String): Boolean {
        if (role == "ADMIN") return true
        if (assignedCategoryCodes.isBlank()) return false
        val codes = assignedCategoryCodes.split(",").map { it.trim().uppercase() }
        return codes.contains(categoryCode.trim().uppercase())
    }
}
