package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.data.local.entity.SubjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SubjectEntity::class, StudySessionEntity::class, GoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_tracker_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val subjectDao = database.subjectDao()
                val sessionDao = database.studySessionDao()
                
                // Add starter subjects
                val dsaId = subjectDao.insertSubject(
                    SubjectEntity(
                        name = "Data Structures & Algorithms",
                        colorHex = 0xFF4F46E5,
                        iconKey = "code",
                        weeklyGoalHours = 8.0f
                    )
                )
                val osId = subjectDao.insertSubject(
                    SubjectEntity(
                        name = "Operating Systems",
                        colorHex = 0xFF059669,
                        iconKey = "terminal",
                        weeklyGoalHours = 6.0f
                    )
                )
                val dbmsId = subjectDao.insertSubject(
                    SubjectEntity(
                        name = "Database Systems (DBMS)",
                        colorHex = 0xFFD97706,
                        iconKey = "database",
                        weeklyGoalHours = 5.0f
                    )
                )
                val cnId = subjectDao.insertSubject(
                    SubjectEntity(
                        name = "Computer Networks",
                        colorHex = 0xFF7C3AED,
                        iconKey = "network",
                        weeklyGoalHours = 4.0f
                    )
                )

                // Add starter sample sessions across the past few days to showcase streaks & stats immediately
                val now = System.currentTimeMillis()
                val dayMillis = 24L * 60 * 60 * 1000

                // Today
                sessionDao.insertSession(
                    StudySessionEntity(
                        subjectId = dsaId,
                        startTime = now - 90 * 60 * 1000,
                        endTime = now - 30 * 60 * 1000,
                        durationMinutes = 60,
                        sessionType = "POMODORO",
                        notes = "Solved Binary Trees and Graph DFS problems",
                        focusRating = 5
                    )
                )
                // Yesterday
                sessionDao.insertSession(
                    StudySessionEntity(
                        subjectId = osId,
                        startTime = now - dayMillis - 120 * 60 * 1000,
                        endTime = now - dayMillis - 45 * 60 * 1000,
                        durationMinutes = 75,
                        sessionType = "STOPWATCH",
                        notes = "Virtual memory, page replacement algorithms",
                        focusRating = 4
                    )
                )
                // 2 days ago
                sessionDao.insertSession(
                    StudySessionEntity(
                        subjectId = dbmsId,
                        startTime = now - 2 * dayMillis - 90 * 60 * 1000,
                        endTime = now - 2 * dayMillis - 40 * 60 * 1000,
                        durationMinutes = 50,
                        sessionType = "POMODORO",
                        notes = "SQL Joins, B+ Trees indexing and ACID properties",
                        focusRating = 5
                    )
                )
                // 3 days ago
                sessionDao.insertSession(
                    StudySessionEntity(
                        subjectId = cnId,
                        startTime = now - 3 * dayMillis - 60 * 60 * 1000,
                        endTime = now - 3 * dayMillis,
                        durationMinutes = 60,
                        sessionType = "MANUAL",
                        notes = "TCP 3-way handshake and congestion control",
                        focusRating = 4
                    )
                )
            }
        }
    }
}
