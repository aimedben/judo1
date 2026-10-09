package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClubCategory
import com.example.data.model.Coach
import com.example.data.model.CoachCategoryAssignment
import com.example.data.model.TrainingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {
    @Query("SELECT * FROM coaches ORDER BY id ASC")
    fun getAllCoaches(): Flow<List<Coach>>

    @Query("SELECT * FROM coaches WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveCoaches(): Flow<List<Coach>>

    @Query("SELECT * FROM coaches WHERE id = :id")
    suspend fun getCoachById(id: Long): Coach?

    @Query("SELECT COUNT(*) FROM coaches WHERE isActive = 1")
    fun countActiveCoaches(): Flow<Int>

    @Query("SELECT COUNT(*) FROM coaches")
    suspend fun countCoachesDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoach(coach: Coach): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoaches(coaches: List<Coach>)

    @Update
    suspend fun updateCoach(coach: Coach)

    @Delete
    suspend fun deleteCoach(coach: Coach)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY code ASC")
    fun getAllCategories(): Flow<List<ClubCategory>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY code ASC")
    fun getActiveCategories(): Flow<List<ClubCategory>>

    @Query("SELECT * FROM categories WHERE code = :code")
    suspend fun getCategoryByCode(code: String): ClubCategory?

    @Query("SELECT COUNT(*) FROM categories WHERE isActive = 1")
    fun countActiveCategories(): Flow<Int>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategoriesDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: ClubCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<ClubCategory>)

    @Update
    suspend fun updateCategory(category: ClubCategory)

    @Delete
    suspend fun deleteCategory(category: ClubCategory)
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM coach_category_assignments")
    fun getAllAssignments(): Flow<List<CoachCategoryAssignment>>

    @Query("SELECT categoryCode FROM coach_category_assignments WHERE coachId = :coachId")
    fun getCategoryCodesForCoach(coachId: Long): Flow<List<String>>

    @Query("SELECT coachId FROM coach_category_assignments WHERE categoryCode = :categoryCode")
    fun getCoachIdsForCategory(categoryCode: String): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: CoachCategoryAssignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<CoachCategoryAssignment>)

    @Query("DELETE FROM coach_category_assignments WHERE coachId = :coachId AND categoryCode = :categoryCode")
    suspend fun deleteAssignment(coachId: Long, categoryCode: String)

    @Query("DELETE FROM coach_category_assignments WHERE coachId = :coachId")
    suspend fun deleteAssignmentsForCoach(coachId: Long)

    @Query("SELECT COUNT(*) FROM coach_category_assignments")
    suspend fun countAssignmentsDirect(): Int
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM training_sessions ORDER BY dateString DESC, startTime DESC")
    fun getAllSessions(): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE categoryCode = :categoryCode ORDER BY dateString DESC, startTime DESC")
    fun getSessionsForCategory(categoryCode: String): Flow<List<TrainingSession>>

    @Query("SELECT * FROM training_sessions WHERE status = 'COMPLETED' ORDER BY dateString DESC, startTime DESC LIMIT :limit")
    fun getRecentCompletedSessions(limit: Int): Flow<List<TrainingSession>>

    @Query("SELECT COUNT(*) FROM training_sessions WHERE status = 'COMPLETED'")
    fun countCompletedSessions(): Flow<Int>

    @Query("SELECT * FROM training_sessions WHERE status = 'IN_PROGRESS' LIMIT 1")
    suspend fun getActiveSession(): TrainingSession?

    @Query("SELECT * FROM training_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): TrainingSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrainingSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<TrainingSession>)

    @Update
    suspend fun updateSession(session: TrainingSession)

    @Delete
    suspend fun deleteSession(session: TrainingSession)

    @Query("DELETE FROM training_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT COUNT(*) FROM training_sessions")
    suspend fun countSessionsDirect(): Int
}
