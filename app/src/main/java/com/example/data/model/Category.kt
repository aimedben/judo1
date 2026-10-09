package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // e.g. "BABY", "U11", "U13", "U15", "U18", "SENIOR"
    val name: String, // e.g. "Baby Judo (Éveil)", "Poussins (U11)", "Benjamins (U13)", etc.
    val coach1Name: String,
    val coach2Name: String,
    val ageRange: String,
    val isActive: Boolean = true,
    val orderIndex: Int = 0
)
