package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val userId: String = "local_user",
    val name: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val maxXp: Int = 1000,
    val focusPoints: Float = 0f,
    val streak: Int = 0,
    val uptime: Int = 100,
    val rankPercent: Int = 100,
    val coachPersonality: String = "The Stoic Mentor",
    val currentVibe: String = "Focused & Intentional",
    val email: String? = null,
    val photoUrl: String? = null,
    val isGoogleLinked: Boolean = false,
    val isOnboarded: Boolean = false,
    val selectedInterests: String = "", // Comma-separated or JSON list
    val planningStyle: String = "Time Blocking + Tasks",
    val focusSummary: String = "",
    val priorityStatement: String = "",
    val availabilityWindow: String = "",
    val reminderIntensity: String = "Balanced",
    val workStartTime: String = "09:00",
    val workEndTime: String = "17:00",
    val wfhDays: String = "Mon,Wed,Fri",
    val workDays: String = "Mon,Tue,Wed,Thu,Fri",
    val weekendDays: String = "Sat,Sun",
    val isVacationMode: Boolean = false,
    val vacationStartDate: String? = null,
    val vacationEndDate: String? = null,
    val vacationNotes: String = "",
    val todayBannerUrl: String? = null,
    val plannerBannerUrl: String? = null,
    val habitsBannerUrl: String? = null,
    val insightsBannerUrl: String? = null
)

@Serializable
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "WORK", "HEALTH", "GROWTH", "FINANCE", "ADMIN", "REPLY"
    val timeSlot: String, // e.g., "09:00 - 10:30 AM"
    val description: String,
    val isCompleted: Boolean = false,
    val isRollover: Boolean = false,
    val date: String = "2024-10-24", // YYYY-MM-DD
    val durationHours: Int = 1,
    val location: String? = null,
    val priority: String = "IMPORTANT", // "CRITICAL", "IMPORTANT", "FLEXIBLE", "OPTIONAL"
    val energyLevel: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val isAiSuggested: Boolean = false,
    val rescheduleCount: Int = 0,
    val status: String = "PENDING" // "PENDING", "COMPLETED", "SKIPPED", "DEFERRED"
)

@Serializable
@Entity(tableName = "daily_reviews")
data class DailyReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val scoreRating: String = "GREAT", // "DOMINANT", "BALANCED", "RECOVERY", "TOUGH"
    val summaryNotes: String = "",
    val completedCount: Int = 0,
    val deferredCount: Int = 0,
    val focusPointsEarned: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val currentValue: Float,
    val targetValue: Float,
    val unit: String, // "L", "min", "Done"
    val isCompleted: Boolean = false,
    val iconName: String, // "water_drop", "menu_book", "self_improvement", "fitness_center"
    val streak: Int = 0
)

@Serializable
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetTimeline: String = "Est. 6 Months",
    val domain: String, // "Career", "Health", "Wealth", "Growth"
    val horizon: String, // "Monthly", "Quarterly", "Yearly"
    val visionImage: String? = null,
    val progressPercent: Int = 0
)

@Serializable
@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val title: String,
    val description: String,
    val status: String, // "ACTIVE", "LOCKED", "COMPLETED"
    val dueDate: String? = null,
    val iconName: String // "payments", "sports_motorsports", "shield", "two_wheeler", "architecture", "groups", "terminal", "workspace_premium"
)

@Serializable
@Entity(tableName = "sub_tasks")
data class SubTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val milestoneId: Int,
    val title: String,
    val isCompleted: Boolean = false
)

@Serializable
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "recurring_alarms")
data class RecurringAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val repeatType: String = "DAILY", // "DAILY", "WEEKDAYS", "WEEKENDS"
    val isEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)

@Serializable
@Entity(tableName = "behavioral_events")
data class BehavioralEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String,
    val entityId: Int? = null,
    val category: String? = null,
    val priority: String? = null,
    val energyLevel: String? = null,
    val timeOfDayHour: Int = 0,
    val dayOfWeek: Int = 1,
    val metadataJson: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = ""
)

@Serializable
@Entity(tableName = "task_performance_records")
data class TaskPerformanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val category: String,
    val estimatedMinutes: Int,
    val actualMinutes: Int,
    val estimationErrorMinutes: Int,
    val priority: String,
    val energyLevel: String,
    val timeSlotHour: Int,
    val dayOfWeek: Int,
    val isAiScheduled: Boolean = false,
    val rolloverCount: Int = 0,
    val completedTimestamp: Long = System.currentTimeMillis(),
    val date: String = ""
)

@Serializable
@Entity(tableName = "recommendation_feedback")
data class RecommendationFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recommendationType: String,
    val recommendationText: String,
    val feedback: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "learned_patterns")
data class LearnedPatternEntity(
    @PrimaryKey val patternKey: String,
    val patternValue: String,
    val confidenceLevel: String = "INSUFFICIENT_DATA",
    val observationCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "predictive_recommendations")
data class PredictiveRecommendationEntity(
    @PrimaryKey val id: String,
    val type: String, // from RecommendationType enum
    val priority: String = "IMPORTANT", // "CRITICAL", "IMPORTANT", "FLEXIBLE", "OPTIONAL"
    val confidence: String = "MODERATE_CONFIDENCE",
    val title: String,
    val explanation: String,
    val suggestedAction: String,
    val actionType: String? = null,
    val relatedTaskId: Int? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val expirationTimestamp: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000),
    val state: String = "CREATED" // "CREATED", "SHOWN", "ACCEPTED", "DISMISSED", "IGNORED", "EXPIRED"
)

@Serializable
@Entity(
    tableName = "predictive_notification_history",
    indices = [
        androidx.room.Index(value = ["recommendationType"]),
        androidx.room.Index(value = ["deduplicationHash"]),
        androidx.room.Index(value = ["dispatchedTimestamp"])
    ]
)
data class PredictiveNotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val recommendationType: String,
    val deduplicationHash: String,
    val dispatchedTimestamp: Long = System.currentTimeMillis()
)

