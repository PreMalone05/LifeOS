package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        GoalEntity::class,
        MilestoneEntity::class,
        SubTaskEntity::class,
        ChatMessageEntity::class,
        RecurringAlarmEntity::class,
        DailyReviewEntity::class,
        BehavioralEventEntity::class,
        TaskPerformanceRecordEntity::class,
        RecommendationFeedbackEntity::class,
        LearnedPatternEntity::class,
        PredictiveRecommendationEntity::class,
        PredictiveNotificationHistoryEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class LifeDatabase : RoomDatabase() {
    abstract fun lifeDao(): LifeDao

    companion object {
        @Volatile
        private var INSTANCE: LifeDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        targetTimeline TEXT NOT NULL DEFAULT 'Est. 6 Months',
                        domain TEXT NOT NULL,
                        horizon TEXT NOT NULL,
                        visionImage TEXT,
                        progressPercent INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS milestones (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        goalId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        status TEXT NOT NULL,
                        dueDate TEXT,
                        iconName TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sub_tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        milestoneId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        role TEXT NOT NULL,
                        text TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workStartTime TEXT NOT NULL DEFAULT '09:00'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workEndTime TEXT NOT NULL DEFAULT '17:00'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN wfhDays TEXT NOT NULL DEFAULT 'Mon,Wed,Fri'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN workDays TEXT NOT NULL DEFAULT 'Mon,Tue,Wed,Thu,Fri'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN weekendDays TEXT NOT NULL DEFAULT 'Sat,Sun'")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN isVacationMode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN vacationStartDate TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN vacationEndDate TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN vacationNotes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN todayBannerUrl TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN plannerBannerUrl TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN habitsBannerUrl TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE user_profile ADD COLUMN insightsBannerUrl TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recurring_alarms (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        hour INTEGER NOT NULL,
                        minute INTEGER NOT NULL,
                        repeatType TEXT NOT NULL DEFAULT 'DAILY',
                        isEnabled INTEGER NOT NULL DEFAULT 1,
                        soundEnabled INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN location TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'IMPORTANT'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN energyLevel TEXT NOT NULL DEFAULT 'MEDIUM'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN isAiSuggested INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN rescheduleCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_reviews (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        scoreRating TEXT NOT NULL DEFAULT 'GREAT',
                        summaryNotes TEXT NOT NULL DEFAULT '',
                        completedCount INTEGER NOT NULL DEFAULT 0,
                        deferredCount INTEGER NOT NULL DEFAULT 0,
                        focusPointsEarned REAL NOT NULL DEFAULT 0.0,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS behavioral_events (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        eventType TEXT NOT NULL,
                        entityId INTEGER,
                        category TEXT,
                        priority TEXT,
                        energyLevel TEXT,
                        timeOfDayHour INTEGER NOT NULL DEFAULT 0,
                        dayOfWeek INTEGER NOT NULL DEFAULT 1,
                        metadataJson TEXT,
                        timestamp INTEGER NOT NULL,
                        date TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS task_performance_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        taskId INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        estimatedMinutes INTEGER NOT NULL,
                        actualMinutes INTEGER NOT NULL,
                        estimationErrorMinutes INTEGER NOT NULL,
                        priority TEXT NOT NULL,
                        energyLevel TEXT NOT NULL,
                        timeSlotHour INTEGER NOT NULL,
                        dayOfWeek INTEGER NOT NULL,
                        isAiScheduled INTEGER NOT NULL DEFAULT 0,
                        rolloverCount INTEGER NOT NULL DEFAULT 0,
                        completedTimestamp INTEGER NOT NULL,
                        date TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recommendation_feedback (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recommendationType TEXT NOT NULL,
                        recommendationText TEXT NOT NULL,
                        feedback TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS learned_patterns (
                        patternKey TEXT PRIMARY KEY NOT NULL,
                        patternValue TEXT NOT NULL,
                        confidenceLevel TEXT NOT NULL DEFAULT 'INSUFFICIENT_DATA',
                        observationCount INTEGER NOT NULL DEFAULT 0,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS predictive_recommendations (
                        id TEXT PRIMARY KEY NOT NULL,
                        type TEXT NOT NULL,
                        priority TEXT NOT NULL DEFAULT 'IMPORTANT',
                        confidence TEXT NOT NULL DEFAULT 'MODERATE_CONFIDENCE',
                        title TEXT NOT NULL,
                        explanation TEXT NOT NULL,
                        suggestedAction TEXT NOT NULL,
                        actionType TEXT,
                        relatedTaskId INTEGER,
                        createdTimestamp INTEGER NOT NULL,
                        expirationTimestamp INTEGER NOT NULL,
                        state TEXT NOT NULL DEFAULT 'CREATED'
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS predictive_notification_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recommendationType TEXT NOT NULL,
                        deduplicationHash TEXT NOT NULL,
                        dispatchedTimestamp INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_predictive_notification_history_recommendationType ON predictive_notification_history (recommendationType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_predictive_notification_history_deduplicationHash ON predictive_notification_history (deduplicationHash)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_predictive_notification_history_dispatchedTimestamp ON predictive_notification_history (dispatchedTimestamp)")
            }
        }

        fun getDatabase(context: Context): LifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeDatabase::class.java,
                    "life_os_database"
                )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11
                )
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
