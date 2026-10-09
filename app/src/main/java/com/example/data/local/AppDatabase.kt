package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Attendance
import com.example.data.model.ClubCategory
import com.example.data.model.Coach
import com.example.data.model.CoachCategoryAssignment
import com.example.data.model.Member
import com.example.data.model.TrainingSession

@Database(
    entities = [
        Member::class,
        Attendance::class,
        Coach::class,
        ClubCategory::class,
        CoachCategoryAssignment::class,
        TrainingSession::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun coachDao(): CoachDao
    abstract fun categoryDao(): CategoryDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createClubTables(db)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createClubTables(db)
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createClubTables(db)
            }
        }

        private fun createClubTables(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `coaches` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `beltName` TEXT NOT NULL,
                    `danGrade` TEXT NOT NULL,
                    `phone` TEXT NOT NULL,
                    `photoUrl` TEXT NOT NULL,
                    `isActive` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `categories` (
                    `code` TEXT NOT NULL,
                    `label` TEXT NOT NULL,
                    `ageRange` TEXT NOT NULL,
                    `schedule` TEXT NOT NULL,
                    `isActive` INTEGER NOT NULL,
                    PRIMARY KEY(`code`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `coach_category_assignments` (
                    `coachId` INTEGER NOT NULL,
                    `categoryCode` TEXT NOT NULL,
                    PRIMARY KEY(`coachId`, `categoryCode`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `training_sessions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `categoryCode` TEXT NOT NULL,
                    `dateString` TEXT NOT NULL,
                    `startTime` TEXT NOT NULL,
                    `endTime` TEXT NOT NULL,
                    `durationMinutes` INTEGER NOT NULL,
                    `presentCoachNames` TEXT NOT NULL,
                    `presentCount` INTEGER NOT NULL,
                    `totalCategoryMembers` INTEGER NOT NULL,
                    `notes` TEXT NOT NULL,
                    `status` TEXT NOT NULL
                )
                """.trimIndent()
            )
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "judo_seddouk.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
