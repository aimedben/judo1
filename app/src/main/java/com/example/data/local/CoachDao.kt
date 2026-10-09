package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Coach
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {
    @Query("SELECT * FROM coaches ORDER BY id ASC")
    fun getAllCoaches(): Flow<List<Coach>>

    @Query("SELECT * FROM coaches WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveCoaches(): Flow<List<Coach>>

    @Query("SELECT * FROM coaches WHERE id = :id LIMIT 1")
    suspend fun getCoachById(id: Long): Coach?

    @Query("SELECT * FROM coaches WHERE name = :name LIMIT 1")
    suspend fun getCoachByName(name: String): Coach?

    @Query("SELECT COUNT(*) FROM coaches WHERE isActive = 1")
    suspend fun countActiveCoaches(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoach(coach: Coach): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoaches(coaches: List<Coach>)

    @Update
    suspend fun updateCoach(coach: Coach)

    @Delete
    suspend fun deleteCoach(coach: Coach)
}
