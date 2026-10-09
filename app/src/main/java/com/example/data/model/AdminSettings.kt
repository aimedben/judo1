package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_settings")
data class AdminSettings(
    @PrimaryKey
    val id: Int = 1,
    val adminPin: String = "1234",
    val clubName: String = "JUDO CLUB SEDDOUK",
    val dojoLocation: String = "Dojo Municipal de Seddouk",
    val contactPhone: String = "0782487120"
)
