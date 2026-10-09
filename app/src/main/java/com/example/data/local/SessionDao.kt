package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SessionCheckIn
import com.example.data.model.SessionCheckInWithMember
import com.example.data.model.TrainingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM training_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE categoryId = :categoryId ORDER BY id DESC")
    fun getSessionsForCategory(categoryId: Long): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE categoryCode = :categoryCode ORDER BY id DESC")
    fun getSessionsForCategoryCode(categoryCode: String): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE categoryId = :categoryId AND status = 'IN_PROGRESS' ORDER BY id DESC LIMIT 1")
    suspend fun getActiveSessionForCategory(categoryId: Long): TrainingSession?

    @Query("SELECT * FROM training_sessions WHERE categoryId = :categoryId AND status = 'IN_PROGRESS' ORDER BY id DESC LIMIT 1")
    fun getActiveSessionFlowForCategory(categoryId: Long): Flow<TrainingSession?>

    @Query("SELECT * FROM training_sessions WHERE status = 'IN_PROGRESS'")
    fun getAllActiveSessionsFlow(): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): TrainingSession?

    @Query("SELECT * FROM training_sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionFlowById(sessionId: Long): Flow<TrainingSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSession): Long

    @Update
    suspend fun updateSession(session: TrainingSession)

    @Delete
    suspend fun deleteSession(session: TrainingSession)

    @Query("SELECT COUNT(*) FROM training_sessions WHERE status = 'COMPLETED'")
    suspend fun countCompletedSessions(): Int

    // Check-ins
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckIn(checkIn: SessionCheckIn): Long

    @Query("SELECT * FROM session_check_ins WHERE sessionId = :sessionId AND memberId = :memberId LIMIT 1")
    suspend fun findCheckIn(sessionId: Long, memberId: Long): SessionCheckIn?

    @Query("SELECT COUNT(*) FROM session_check_ins WHERE sessionId = :sessionId")
    suspend fun countCheckInsForSession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM session_check_ins WHERE sessionId = :sessionId")
    fun countCheckInsFlowForSession(sessionId: Long): Flow<Int>

    @Query("""
        SELECT 
            c.id AS checkInId,
            c.sessionId AS sessionId,
            c.memberId AS memberId,
            m.firstName AS firstName,
            m.lastName AS lastName,
            m.groupCode AS groupCode,
            m.beltName AS beltName,
            c.scanTime AS scanTime,
            c.timestamp AS timestamp,
            m.qrCode AS qrCode,
            m.phone AS phone,
            m.photoUrl AS photoUrl,
            c.method AS method
        FROM session_check_ins c
        INNER JOIN members m ON c.memberId = m.id
        WHERE c.sessionId = :sessionId
        ORDER BY c.timestamp DESC
    """)
    fun getCheckInsWithMemberFlow(sessionId: Long): Flow<List<SessionCheckInWithMember>>

    @Query("""
        SELECT 
            c.id AS checkInId,
            c.sessionId AS sessionId,
            c.memberId AS memberId,
            m.firstName AS firstName,
            m.lastName AS lastName,
            m.groupCode AS groupCode,
            m.beltName AS beltName,
            c.scanTime AS scanTime,
            c.timestamp AS timestamp,
            m.qrCode AS qrCode,
            m.phone AS phone,
            m.photoUrl AS photoUrl,
            c.method AS method
        FROM session_check_ins c
        INNER JOIN members m ON c.memberId = m.id
        WHERE c.sessionId = :sessionId
        ORDER BY c.timestamp DESC
    """)
    suspend fun getCheckInsWithMember(sessionId: Long): List<SessionCheckInWithMember>

    @Query("DELETE FROM session_check_ins WHERE sessionId = :sessionId AND memberId = :memberId")
    suspend fun deleteCheckIn(sessionId: Long, memberId: Long)
}
