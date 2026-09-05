package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Manages local backup export and atomic restore of all Room entities in LifeOS.
 * Provides complete schema serialization without any cloud dependency.
 */
class BackupManager(private val database: LifeDatabase) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    /**
     * Serializes all Room entities into a single JSON file and writes to the destination Uri.
     */
    suspend fun exportBackup(context: Context, destinationUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val dao = database.lifeDao()

            // 1. Gather all entity collections from Room
            val profile = dao.getUserProfileDirect()
            val tasks = dao.getAllTasksList()
            val habits = dao.getAllHabitsList()
            val goals = dao.getAllGoalsList()
            val milestones = dao.getAllMilestonesList()
            val subTasks = dao.getAllSubTasksList()
            val chatMessages = dao.getAllChatMessagesList()
            val alarms = dao.getAllRecurringAlarmsList()
            val reviews = dao.getAllDailyReviewsList()
            val behavioralEvents = dao.getAllBehavioralEventsList()
            val performanceRecords = dao.getAllTaskPerformanceRecordsList()
            val feedbacks = dao.getAllRecommendationFeedbackList()
            val patterns = dao.getAllLearnedPatternsList()
            val recommendations = dao.getAllPredictiveRecommendationsList()
            val notificationHistories = dao.getAllNotificationHistoryList()

            val backupData = LifeBackupData(
                formatVersion = LifeBackupData.CURRENT_FORMAT_VERSION,
                databaseSchemaVersion = LifeDatabase.DATABASE_VERSION,
                exportTimestamp = System.currentTimeMillis(),
                appVersion = "1.0.0",
                userProfile = profile,
                tasks = tasks,
                habits = habits,
                goals = goals,
                milestones = milestones,
                subTasks = subTasks,
                chatMessages = chatMessages,
                recurringAlarms = alarms,
                dailyReviews = reviews,
                behavioralEvents = behavioralEvents,
                taskPerformanceRecords = performanceRecords,
                recommendationFeedbacks = feedbacks,
                learnedPatterns = patterns,
                predictiveRecommendations = recommendations,
                predictiveNotificationHistories = notificationHistories
            )

            // 2. Serialize to JSON with detailed error checking
            val jsonString: String = try {
                json.encodeToString(LifeBackupData.serializer(), backupData)
            } catch (e: SerializationException) {
                Log.e(TAG, "Serialization failed", e)
                return@withContext BackupResult.Failure(
                    "Failed to serialize database entities into JSON: ${e.message ?: e.javaClass.simpleName}",
                    e
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Unexpected serialization error", e)
                return@withContext BackupResult.Failure(
                    "Unexpected error during entity serialization: ${e.message ?: e.javaClass.simpleName}",
                    e
                )
            }

            // 3. Write to destination URI via SAF ContentResolver
            val contentResolver = context.contentResolver
            val outputStream = try {
                contentResolver.openOutputStream(destinationUri)
            } catch (e: Throwable) {
                return@withContext BackupResult.Failure(
                    "Failed to open destination file for writing: ${e.message ?: "Permission or storage error"}",
                    e
                )
            } ?: return@withContext BackupResult.Failure("Unable to open write stream to selected destination URI.")

            OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                writer.write(jsonString)
                writer.flush()
            }

            val summary = BackupRecordSummary(
                hasProfile = profile != null,
                tasksCount = tasks.size,
                habitsCount = habits.size,
                goalsCount = goals.size,
                milestonesCount = milestones.size,
                subTasksCount = subTasks.size,
                chatMessagesCount = chatMessages.size,
                alarmsCount = alarms.size,
                reviewsCount = reviews.size,
                behavioralEventsCount = behavioralEvents.size,
                performanceRecordsCount = performanceRecords.size,
                feedbacksCount = feedbacks.size,
                learnedPatternsCount = patterns.size,
                recommendationsCount = recommendations.size,
                notificationHistoriesCount = notificationHistories.size
            )

            BackupResult.Success(
                message = "Backup exported successfully (${summary.totalRecords} total records saved).",
                details = summary
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Export failed", e)
            BackupResult.Failure(
                "Export failed: ${e.message ?: e.javaClass.simpleName}",
                e
            )
        }
    }

    /**
     * Reads a JSON backup from the selected source Uri, parses and validates schema,
     * and atomically clears and repopulates all Room tables via Room transaction.
     * All-or-nothing guarantee: if any step fails, the entire transaction rolls back.
     */
    suspend fun restoreBackup(context: Context, sourceUri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            // 1. Read JSON file content
            val contentResolver = context.contentResolver
            val inputStream = try {
                contentResolver.openInputStream(sourceUri)
            } catch (e: Throwable) {
                return@withContext BackupResult.Failure(
                    "Cannot open backup file: ${e.message ?: "Permission denied or file not found"}",
                    e
                )
            } ?: return@withContext BackupResult.Failure("Could not open read stream for selected file.")

            val jsonString = InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
                reader.readText()
            }

            if (jsonString.isBlank()) {
                return@withContext BackupResult.Failure("The selected backup file is completely empty.")
            }

            // 2. Deserialize JSON into LifeBackupData with detailed reporting
            val backupData: LifeBackupData = try {
                json.decodeFromString(LifeBackupData.serializer(), jsonString)
            } catch (e: SerializationException) {
                Log.e(TAG, "Deserialization failed", e)
                return@withContext BackupResult.Failure(
                    "Invalid or malformed backup file. Failed to parse entity data: ${e.message ?: e.javaClass.simpleName}",
                    e
                )
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Invalid payload content", e)
                return@withContext BackupResult.Failure(
                    "Backup file contains incompatible or illegal field data: ${e.message ?: e.javaClass.simpleName}",
                    e
                )
            } catch (e: Throwable) {
                Log.e(TAG, "Failed reading backup file", e)
                return@withContext BackupResult.Failure(
                    "Failed to process backup file: ${e.message ?: e.javaClass.simpleName}",
                    e
                )
            }

            // 3. Schema Version Validation
            if (backupData.databaseSchemaVersion > LifeDatabase.DATABASE_VERSION) {
                return@withContext BackupResult.Failure(
                    "Incompatible backup schema: Backup was created with schema v${backupData.databaseSchemaVersion}, but your installed LifeOS only supports up to schema v${LifeDatabase.DATABASE_VERSION}. Please update LifeOS before restoring this file."
                )
            }

            if (backupData.formatVersion > LifeBackupData.CURRENT_FORMAT_VERSION) {
                return@withContext BackupResult.Failure(
                    "Unsupported backup format: Backup format v${backupData.formatVersion} is newer than supported format v${LifeBackupData.CURRENT_FORMAT_VERSION}. Please update LifeOS."
                )
            }

            // 4. Atomic All-or-Nothing Database Transaction
            val dao = database.lifeDao()
            database.withTransaction {
                // Clear all existing Room tables
                dao.clearUserProfile()
                dao.clearAllTasks()
                dao.clearAllHabits()
                dao.clearAllGoals()
                dao.clearAllMilestones()
                dao.clearAllSubTasks()
                dao.clearChatMessages()
                dao.clearAllRecurringAlarms()
                dao.clearAllDailyReviews()
                dao.clearBehavioralEvents()
                dao.clearTaskPerformanceRecords()
                dao.clearRecommendationFeedback()
                dao.clearAllLearnedPatterns()
                dao.clearPredictiveRecommendations()
                dao.clearNotificationHistory()

                // Repopulate from backup
                val profileToInsert = backupData.userProfile ?: UserProfileEntity()
                dao.insertUserProfile(profileToInsert)

                if (backupData.tasks.isNotEmpty()) dao.insertTasks(backupData.tasks)
                if (backupData.habits.isNotEmpty()) dao.insertHabits(backupData.habits)
                if (backupData.goals.isNotEmpty()) dao.insertGoals(backupData.goals)
                if (backupData.milestones.isNotEmpty()) dao.insertMilestones(backupData.milestones)
                if (backupData.subTasks.isNotEmpty()) dao.insertSubTasks(backupData.subTasks)
                if (backupData.chatMessages.isNotEmpty()) dao.insertChatMessages(backupData.chatMessages)
                if (backupData.recurringAlarms.isNotEmpty()) dao.insertRecurringAlarms(backupData.recurringAlarms)
                if (backupData.dailyReviews.isNotEmpty()) dao.insertDailyReviews(backupData.dailyReviews)
                if (backupData.behavioralEvents.isNotEmpty()) dao.insertBehavioralEvents(backupData.behavioralEvents)
                if (backupData.taskPerformanceRecords.isNotEmpty()) dao.insertTaskPerformanceRecords(backupData.taskPerformanceRecords)
                if (backupData.recommendationFeedbacks.isNotEmpty()) dao.insertRecommendationFeedbacks(backupData.recommendationFeedbacks)
                if (backupData.learnedPatterns.isNotEmpty()) dao.insertLearnedPatterns(backupData.learnedPatterns)
                if (backupData.predictiveRecommendations.isNotEmpty()) dao.insertPredictiveRecommendations(backupData.predictiveRecommendations)
                if (backupData.predictiveNotificationHistories.isNotEmpty()) dao.insertNotificationHistories(backupData.predictiveNotificationHistories)
            }

            val summary = BackupRecordSummary(
                hasProfile = backupData.userProfile != null,
                tasksCount = backupData.tasks.size,
                habitsCount = backupData.habits.size,
                goalsCount = backupData.goals.size,
                milestonesCount = backupData.milestones.size,
                subTasksCount = backupData.subTasks.size,
                chatMessagesCount = backupData.chatMessages.size,
                alarmsCount = backupData.recurringAlarms.size,
                reviewsCount = backupData.dailyReviews.size,
                behavioralEventsCount = backupData.behavioralEvents.size,
                performanceRecordsCount = backupData.taskPerformanceRecords.size,
                feedbacksCount = backupData.recommendationFeedbacks.size,
                learnedPatternsCount = backupData.learnedPatterns.size,
                recommendationsCount = backupData.predictiveRecommendations.size,
                notificationHistoriesCount = backupData.predictiveNotificationHistories.size
            )

            BackupResult.Success(
                message = "Database successfully restored from backup (${summary.totalRecords} records restored).",
                details = summary
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Restore failed", e)
            BackupResult.Failure(
                "Restore failed: ${e.message ?: e.javaClass.simpleName}. All original data was preserved safely.",
                e
            )
        }
    }

    companion object {
        private const val TAG = "BackupManager"
    }
}
