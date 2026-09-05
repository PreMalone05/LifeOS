package com.example.data

import kotlinx.serialization.Serializable

/**
 * Portable backup payload containing all Room entity tables in LifeOS (v11 schema).
 * Consistent with kotlinx.serialization and AiJsonParser.
 */
@Serializable
data class LifeBackupData(
    val formatVersion: Int = CURRENT_FORMAT_VERSION,
    val databaseSchemaVersion: Int = LifeDatabase.DATABASE_VERSION,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val userProfile: UserProfileEntity? = null,
    val tasks: List<TaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val milestones: List<MilestoneEntity> = emptyList(),
    val subTasks: List<SubTaskEntity> = emptyList(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val recurringAlarms: List<RecurringAlarmEntity> = emptyList(),
    val dailyReviews: List<DailyReviewEntity> = emptyList(),
    val behavioralEvents: List<BehavioralEventEntity> = emptyList(),
    val taskPerformanceRecords: List<TaskPerformanceRecordEntity> = emptyList(),
    val recommendationFeedbacks: List<RecommendationFeedbackEntity> = emptyList(),
    val learnedPatterns: List<LearnedPatternEntity> = emptyList(),
    val predictiveRecommendations: List<PredictiveRecommendationEntity> = emptyList(),
    val predictiveNotificationHistories: List<PredictiveNotificationHistoryEntity> = emptyList()
) {
    companion object {
        const val CURRENT_FORMAT_VERSION = 1
    }
}

sealed class BackupResult {
    data class Success(
        val message: String,
        val details: BackupRecordSummary? = null
    ) : BackupResult()

    data class Failure(
        val errorMessage: String,
        val cause: Throwable? = null
    ) : BackupResult()
}

data class BackupRecordSummary(
    val hasProfile: Boolean,
    val tasksCount: Int,
    val habitsCount: Int,
    val goalsCount: Int,
    val milestonesCount: Int,
    val subTasksCount: Int,
    val chatMessagesCount: Int,
    val alarmsCount: Int,
    val reviewsCount: Int,
    val behavioralEventsCount: Int,
    val performanceRecordsCount: Int,
    val feedbacksCount: Int,
    val learnedPatternsCount: Int,
    val recommendationsCount: Int,
    val notificationHistoriesCount: Int
) {
    val totalRecords: Int
        get() = (if (hasProfile) 1 else 0) + tasksCount + habitsCount + goalsCount +
                milestonesCount + subTasksCount + chatMessagesCount + alarmsCount +
                reviewsCount + behavioralEventsCount + performanceRecordsCount +
                feedbacksCount + learnedPatternsCount + recommendationsCount +
                notificationHistoriesCount
}
