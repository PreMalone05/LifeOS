package com.example.data

enum class OverloadRiskLevel(val displayName: String, val severityRank: Int) {
    LOW("Low Load", 1),
    MODERATE("Moderate Load", 2),
    HIGH("High Overload Risk", 3)
}

enum class DeadlineRiskLevel(val displayName: String, val severityRank: Int) {
    ON_TRACK("On Track", 1),
    AT_RISK("At Risk", 2),
    HIGH_RISK("Critical Risk", 3),
    OVERDUE("Overdue", 4)
}

enum class HabitRiskLevel(val displayName: String, val severityRank: Int) {
    LOW("Stable", 1),
    MODERATE("Elevated Risk", 2),
    HIGH("High Risk of Miss", 3)
}

enum class RecommendationType {
    CAPACITY_WARNING,
    DEADLINE_WARNING,
    TASK_RECOMMENDATION,
    HABIT_RISK,
    FOCUS_WINDOW,
    SCHEDULE_CONFLICT,
    PREPARATION_REMINDER,
    PLAN_DIVERGENCE
}

enum class RecommendationState {
    CREATED,
    SHOWN,
    ACCEPTED,
    DISMISSED,
    IGNORED,
    EXPIRED
}

data class ScheduleOverloadPrediction(
    val date: String,
    val riskLevel: OverloadRiskLevel,
    val plannedHours: Float,
    val expectedHours: Float, // Adjusted with historical estimation error & duration prediction
    val typicalCapacityHours: Float,
    val calendarCommitmentHours: Float,
    val remainingBufferHours: Float,
    val confidence: ConfidenceLevel,
    val contributingFactors: List<String>,
    val suggestedAction: String? = null
)

data class DeadlineRiskPrediction(
    val targetId: Int,
    val title: String,
    val targetType: String, // "TASK", "MILESTONE", "GOAL"
    val dueDate: String,
    val riskLevel: DeadlineRiskLevel,
    val daysRemaining: Int,
    val estimatedHoursRemaining: Float,
    val availableCapacityBeforeDeadline: Float,
    val confidence: ConfidenceLevel,
    val explanation: String
)

data class TaskCompletionProbability(
    val taskId: Int,
    val taskTitle: String,
    val probabilityPercent: Int, // 0..100
    val confidence: ConfidenceLevel,
    val isSufficientData: Boolean,
    val supportingFactors: List<String>,
    val recommendedWindow: String? = null
)

data class HabitRiskPrediction(
    val habitId: Int,
    val habitName: String,
    val riskLevel: HabitRiskLevel,
    val confidence: ConfidenceLevel,
    val explanation: String,
    val conflictingCommitment: String? = null
)

data class PredictiveFocusWindow(
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "10:15"
    val durationMinutes: Int,
    val recommendedTaskId: Int? = null,
    val recommendedTaskTitle: String? = null,
    val energyLevel: String = "HIGH", // "HIGH", "MEDIUM", "LOW"
    val reason: String,
    val confidence: ConfidenceLevel = ConfidenceLevel.MODERATE_CONFIDENCE
)

data class WhatShouldIDoNowResult(
    val recommendedTaskId: Int?,
    val recommendedTaskTitle: String,
    val priority: String,
    val durationMinutes: Int,
    val energyLevel: String,
    val focusWindowAvailableMinutes: Int,
    val reason: String,
    val secondaryOptionTaskId: Int? = null,
    val secondaryOptionTitle: String? = null,
    val isActionable: Boolean = true
)

data class MorningBriefing(
    val date: String,
    val capacityStatus: String, // "OPTIMAL", "MODERATE_LOAD", "HIGH_LOAD"
    val mainPriorityTask: String,
    val bestFocusWindow: String,
    val potentialIssue: String? = null,
    val aiRecommendation: String,
    val generatedTimestamp: Long = System.currentTimeMillis()
)

data class PlanDivergenceReport(
    val isDiverged: Boolean,
    val divergenceScore: Int, // 0..100
    val originalPlannedCount: Int,
    val completedCount: Int,
    val missedOrDelayedCount: Int,
    val unexpectedCalendarMinutes: Int,
    val reasons: List<String>,
    val suggestedAction: String
)

data class TomorrowPreviewReport(
    val date: String,
    val expectedCapacityHours: Float,
    val plannedWorkloadHours: Float,
    val calendarLoadHours: Float,
    val overloadRisk: OverloadRiskLevel,
    val importantDeadlines: List<String>,
    val recommendedFocusPeriod: String,
    val potentialConflicts: List<String>,
    val tasksToPostpone: List<String>
)

data class PredictiveRecommendation(
    val id: String,
    val type: RecommendationType,
    val priority: String, // "CRITICAL", "IMPORTANT", "FLEXIBLE", "OPTIONAL"
    val confidence: ConfidenceLevel,
    val title: String,
    val explanation: String,
    val suggestedAction: String,
    val actionType: String? = null, // "REBALANCE", "START_TASK", "POSTPONE", "DISMISS", "VIEW_HABIT"
    val relatedTaskId: Int? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val expirationTimestamp: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000),
    var state: RecommendationState = RecommendationState.CREATED
)
