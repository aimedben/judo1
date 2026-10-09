package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_check_ins",
    foreignKeys = [
        ForeignKey(
            entity = TrainingSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId", "memberId"], unique = true),
        Index(value = ["sessionId"]),
        Index(value = ["memberId"])
    ]
)
data class SessionCheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val memberId: Long,
    val scanTime: String, // "HH:mm:ss"
    val timestamp: Long = System.currentTimeMillis(),
    val method: String = "DOUCHETTE" // "DOUCHETTE", "CAMERA", "MANUAL"
)

data class SessionCheckInWithMember(
    val checkInId: Long,
    val sessionId: Long,
    val memberId: Long,
    val firstName: String,
    val lastName: String,
    val groupCode: String,
    val beltName: String,
    val scanTime: String,
    val timestamp: Long,
    val qrCode: String,
    val phone: String,
    val photoUrl: String = "",
    val method: String = "DOUCHETTE"
) {
    val fullName: String get() = "$firstName $lastName"
    val belt: Belt get() = Belt.fromString(beltName)
    val group: JudoGroup get() = JudoGroup.fromString(groupCode)
}
