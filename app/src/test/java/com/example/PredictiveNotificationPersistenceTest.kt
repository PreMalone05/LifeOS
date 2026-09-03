package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.notification.PredictiveNotificationManager
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File
import java.util.Calendar
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PredictiveNotificationPersistenceTest {

    private lateinit var context: Context
    private lateinit var db: LifeDatabase
    private lateinit var dao: LifeDao

    private var middayBaseTime: Long = 0L

    private val sampleRec = PredictiveRecommendation(
        id = "rec-1",
        type = RecommendationType.CAPACITY_WARNING,
        priority = "CRITICAL",
        confidence = ConfidenceLevel.HIGH_CONFIDENCE,
        title = "Overloaded Schedule Today",
        explanation = "Planned tasks exceed your typical capacity by 2.5 hours.",
        suggestedAction = "Consider deferring non-urgent items."
    )

    private val differentRecSameType = PredictiveRecommendation(
        id = "rec-2",
        type = RecommendationType.CAPACITY_WARNING,
        priority = "IMPORTANT",
        confidence = ConfidenceLevel.HIGH_CONFIDENCE,
        title = "Different Capacity Alert",
        explanation = "Upcoming meetings leave insufficient focus time.",
        suggestedAction = "Protect morning deep work block."
    )

    private val sampleRecDifferentType = PredictiveRecommendation(
        id = "rec-3",
        type = RecommendationType.FOCUS_WINDOW,
        priority = "IMPORTANT",
        confidence = ConfidenceLevel.HIGH_CONFIDENCE,
        title = "Optimal Focus Window Detected",
        explanation = "90 minutes open before your next sync.",
        suggestedAction = "Start deep focus on High Priority Task."
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val app = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        db = Room.inMemoryDatabaseBuilder(context, LifeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.lifeDao()

        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 28, 14, 0, 0)
        }
        middayBaseTime = cal.timeInMillis
    }

    @Test
    fun `initial notification is allowed`() {
        val allowed = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = middayBaseTime
        )
        assertTrue(allowed)
    }

    @Test
    fun `same recommendation type is blocked within 3 hours cooldown`() {
        val baseTime = middayBaseTime

        // Dispatch initial alert
        val sent = PredictiveNotificationManager.sendPredictiveAlert(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = baseTime
        )
        assertTrue(sent)

        // Attempt sending different recommendation of same type 1 hour later (within 3h cooldown)
        val blocked = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = differentRecSameType,
            daoOverride = dao,
            currentTimeMillis = baseTime + (1 * 60 * 60 * 1000L)
        )
        assertFalse(blocked)
    }

    @Test
    fun `same recommendation type is allowed after 3 hours cooldown`() {
        val baseTime = middayBaseTime

        // Dispatch initial alert
        val sent = PredictiveNotificationManager.sendPredictiveAlert(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = baseTime
        )
        assertTrue(sent)

        // Attempt sending 3.5 hours later with a different hash (14:00 + 3.5h = 17:30, within active hours)
        val allowed = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = differentRecSameType,
            daoOverride = dao,
            currentTimeMillis = baseTime + (3 * 60 * 60 * 1000L + 30 * 60 * 1000L)
        )
        assertTrue(allowed)
    }

    @Test
    fun `identical recommendation is blocked within 12 hours deduplication window`() {
        val baseTime = middayBaseTime

        // Dispatch initial alert
        val sent = PredictiveNotificationManager.sendPredictiveAlert(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = baseTime
        )
        assertTrue(sent)

        // Attempt sending identical recommendation 4 hours later (past 3h cooldown, but within 12h deduplication)
        val blockedByDeduplication = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = baseTime + (4 * 60 * 60 * 1000L)
        )
        assertFalse(blockedByDeduplication)
    }

    @Test
    fun `identical recommendation is allowed after 12 hours deduplication window`() {
        val baseTime = middayBaseTime

        // Dispatch initial alert
        val sent = PredictiveNotificationManager.sendPredictiveAlert(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = baseTime
        )
        assertTrue(sent)

        // Attempt sending identical recommendation 24 hours later (next day at 14:00)
        val allowed = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = baseTime + (24 * 60 * 60 * 1000L)
        )
        assertTrue(allowed)
    }

    @Test
    fun `different recommendation of different type is allowed immediately`() {
        val baseTime = middayBaseTime

        // Dispatch capacity warning
        val sent = PredictiveNotificationManager.sendPredictiveAlert(
            context = context,
            recommendation = sampleRec,
            daoOverride = dao,
            currentTimeMillis = baseTime
        )
        assertTrue(sent)

        // Focus window recommendation (different type, different hash)
        val allowed = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = sampleRecDifferentType,
            daoOverride = dao,
            currentTimeMillis = baseTime + 1000L
        )
        assertTrue(allowed)
    }

    @Test
    fun `process restart simulation retains cooldown and deduplication state via Room`() {
        val dbFile = File(context.cacheDir, "test_persistence_db.db")
        dbFile.delete()

        val baseTime = middayBaseTime

        // Process 1: Start and dispatch notification
        val process1Db = Room.databaseBuilder(context, LifeDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        val process1Dao = process1Db.lifeDao()

        val sent = PredictiveNotificationManager.sendPredictiveAlert(
            context = context,
            recommendation = sampleRec,
            daoOverride = process1Dao,
            currentTimeMillis = baseTime
        )
        assertTrue(sent)

        // Simulate Process Death: Close process 1 database and wipe all in-memory references
        process1Db.close()

        // Process 2: App restarts, fresh Room DB instance opened from disk
        val process2Db = Room.databaseBuilder(context, LifeDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        val process2Dao = process2Db.lifeDao()

        // Scenario B: Process restarts 1 hour later (before 3h cooldown) -> must still be blocked
        val blockedAfterRestart = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = differentRecSameType,
            daoOverride = process2Dao,
            currentTimeMillis = baseTime + (1 * 60 * 60 * 1000L)
        )
        assertFalse(blockedAfterRestart)

        // Scenario D: Process restarts 4 hours later (past 3h cooldown, but within 12h deduplication) -> identical rec must still be blocked
        val deduplicatedAfterRestart = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = sampleRec,
            daoOverride = process2Dao,
            currentTimeMillis = baseTime + (4 * 60 * 60 * 1000L)
        )
        assertFalse(deduplicatedAfterRestart)

        // Scenario C & D: Process restarts 24 hours later (past both 3h cooldown and 12h deduplication) -> allowed
        val allowedAfterFullExpiry = PredictiveNotificationManager.shouldSendNotification(
            context = context,
            recommendation = sampleRec,
            daoOverride = process2Dao,
            currentTimeMillis = baseTime + (24 * 60 * 60 * 1000L)
        )
        assertTrue(allowedAfterFullExpiry)

        process2Db.close()
        dbFile.delete()
    }

    @Test
    fun `concurrent notification attempts dispatch exactly once`() = runBlocking {
        val baseTime = middayBaseTime
        val threadCount = 10
        val latch = CountDownLatch(1)
        val successCount = AtomicInteger(0)

        val jobs = (1..threadCount).map {
            async(Dispatchers.IO) {
                latch.await()
                val sent = PredictiveNotificationManager.sendPredictiveAlert(
                    context = context,
                    recommendation = sampleRec,
                    daoOverride = dao,
                    currentTimeMillis = baseTime
                )
                if (sent) {
                    successCount.incrementAndGet()
                }
            }
        }

        // Release all threads simultaneously
        latch.countDown()
        jobs.awaitAll()

        // Exactly one thread must have succeeded; all other 9 threads must have been blocked
        assertEquals(1, successCount.get())
    }

    @Test
    fun `dont suggest again feedback continues to suppress recommendations in predictive engine`() {
        val feedbackList = listOf(
            RecommendationFeedbackEntity(
                recommendationType = "CAPACITY_WARNING",
                recommendationText = "Overloaded Schedule Today",
                feedback = "DONT_SUGGEST_AGAIN"
            )
        )

        val sampleCapacity = PersonalCapacityModel(
            averageRealisticDailyHours = 5.0f,
            averageFocusCapacityHours = 3.5f,
            averageCompletedTaskCount = 4.0f,
            averageCompletedFocusHours = 3.0f,
            averageRolloverRatePercent = 10,
            averageEstimationErrorPercent = 10,
            workdayCapacityHours = 5.5f,
            weekendCapacityHours = 2.5f,
            morningCapacityPercent = 50,
            afternoonCapacityPercent = 35,
            eveningCapacityPercent = 15,
            energyLevelCapacities = mapOf("HIGH" to 2.5f, "MEDIUM" to 2.0f, "LOW" to 1.0f),
            confidence = ConfidenceLevel.HIGH_CONFIDENCE,
            daysAnalyzed = 14
        )

        val sampleAccuracy = PlanningAccuracyReport(
            overallScore = 80,
            confidence = ConfidenceLevel.HIGH_CONFIDENCE,
            workloadCompletionRate = 0.85f,
            estimationAccuracyRate = 0.80f,
            rolloverControlRate = 0.90f,
            scheduleAdherenceRate = 0.85f,
            headline = "Planning is well-calibrated",
            detailedSummary = "High estimation consistency",
            factors = listOf(
                AccuracyFactor("Duration Accuracy", 85, 30, "Accurate estimates", "EXCELLENT")
            ),
            totalObservations = 20
        )

        val tasks = listOf(
            TaskEntity(
                id = 1,
                title = "Massive Heavy Task",
                durationHours = 10,
                date = "2026-08-28",
                isCompleted = false,
                category = "WORK",
                priority = "CRITICAL",
                energyLevel = "HIGH",
                description = "",
                timeSlot = ""
            )
        )

        val recommendations = PredictiveEngine.generateRankedRecommendations(
            todayDate = "2026-08-28",
            tomorrowDate = "2026-08-29",
            tasks = tasks,
            calendarEvents = emptyList(),
            habits = emptyList(),
            goals = emptyList(),
            milestones = emptyList(),
            capacityModel = sampleCapacity,
            accuracyReport = sampleAccuracy,
            performanceRecords = emptyList(),
            events = emptyList(),
            feedbackList = feedbackList
        )

        // CAPACITY_WARNING must be completely filtered out due to DONT_SUGGEST_AGAIN
        assertFalse(recommendations.any { it.type == RecommendationType.CAPACITY_WARNING })
    }

    @Test
    fun `migration 10 to 11 creates predictive notification history table without losing data`() {
        val dbFile = File(context.cacheDir, "migration_test_v10_to_v11.db")
        dbFile.delete()

        // Open as v10 database
        val openHelper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbFile.absolutePath)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(10) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS user_profile (
                                id INTEGER PRIMARY KEY NOT NULL,
                                userId TEXT NOT NULL,
                                name TEXT NOT NULL,
                                level INTEGER NOT NULL,
                                xp INTEGER NOT NULL,
                                maxXp INTEGER NOT NULL,
                                focusPoints REAL NOT NULL,
                                streak INTEGER NOT NULL,
                                uptime INTEGER NOT NULL,
                                rankPercent INTEGER NOT NULL,
                                coachPersonality TEXT NOT NULL,
                                currentVibe TEXT NOT NULL,
                                isGoogleLinked INTEGER NOT NULL,
                                isOnboarded INTEGER NOT NULL,
                                selectedInterests TEXT NOT NULL,
                                planningStyle TEXT NOT NULL,
                                focusSummary TEXT NOT NULL,
                                priorityStatement TEXT NOT NULL,
                                availabilityWindow TEXT NOT NULL,
                                reminderIntensity TEXT NOT NULL,
                                workStartTime TEXT NOT NULL,
                                workEndTime TEXT NOT NULL,
                                wfhDays TEXT NOT NULL,
                                workDays TEXT NOT NULL,
                                weekendDays TEXT NOT NULL,
                                isVacationMode INTEGER NOT NULL,
                                vacationStartDate TEXT,
                                vacationEndDate TEXT,
                                vacationNotes TEXT NOT NULL,
                                todayBannerUrl TEXT,
                                plannerBannerUrl TEXT,
                                habitsBannerUrl TEXT,
                                insightsBannerUrl TEXT
                            )
                        """.trimIndent())
                        db.execSQL("""
                            INSERT INTO user_profile (
                                id, userId, name, level, xp, maxXp, focusPoints, streak, uptime, rankPercent,
                                coachPersonality, currentVibe, isGoogleLinked, isOnboarded, selectedInterests,
                                planningStyle, focusSummary, priorityStatement, availabilityWindow, reminderIntensity,
                                workStartTime, workEndTime, wfhDays, workDays, weekendDays, isVacationMode, vacationNotes
                            ) VALUES (
                                1, 'user1', 'Julian Migration', 5, 250, 1000, 45.0, 7, 98, 95,
                                'The Stoic Mentor', 'Focused', 0, 1, 'Tech',
                                'Time Blocking', 'Focus deep', 'Win today', '09:00 - 17:00', 'Balanced',
                                '09:00', '17:00', 'Mon,Wed,Fri', 'Mon,Tue,Wed,Thu,Fri', 'Sat,Sun', 0, ''
                            )
                        """.trimIndent())
                    }

                    override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                })
                .build()
        )
        val writableDb = openHelper.writableDatabase

        // Execute MIGRATION_10_11
        LifeDatabase.MIGRATION_10_11.migrate(writableDb)

        // Verify table exists by inserting a history entity
        writableDb.execSQL("""
            INSERT INTO predictive_notification_history (recommendationType, deduplicationHash, dispatchedTimestamp)
            VALUES ('CAPACITY_WARNING', 'test_hash_123', 1000000)
        """.trimIndent())

        val cursor = writableDb.query("SELECT * FROM predictive_notification_history WHERE deduplicationHash = 'test_hash_123'")
        assertTrue(cursor.moveToFirst())
        assertEquals("CAPACITY_WARNING", cursor.getString(cursor.getColumnIndexOrThrow("recommendationType")))
        cursor.close()

        // Verify user profile data was preserved
        val profileCursor = writableDb.query("SELECT name FROM user_profile WHERE id = 1")
        assertTrue(profileCursor.moveToFirst())
        assertEquals("Julian Migration", profileCursor.getString(profileCursor.getColumnIndexOrThrow("name")))
        profileCursor.close()

        writableDb.close()
        openHelper.close()
        dbFile.delete()
    }
}
