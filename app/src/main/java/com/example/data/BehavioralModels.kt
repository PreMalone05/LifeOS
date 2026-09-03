package com.example.data

object BehavioralEventType {
    const val TASK_CREATED = "TASK_CREATED"
    const val TASK_SCHEDULED = "TASK_SCHEDULED"
    const val TASK_STARTED = "TASK_STARTED"
    const val TASK_COMPLETED = "TASK_COMPLETED"
    const val TASK_SKIPPED = "TASK_SKIPPED"
    const val TASK_POSTPONED = "TASK_POSTPONED"
    const val TASK_ROLLED_OVER = "TASK_ROLLED_OVER"
    const val TASK_RESCHEDULED_AI = "TASK_RESCHEDULED_AI"
    const val TASK_RESCHEDULED_MANUAL = "TASK_RESCHEDULED_MANUAL"
    const val AI_RECOMMENDATION_ACCEPTED = "AI_RECOMMENDATION_ACCEPTED"
    const val AI_RECOMMENDATION_REJECTED = "AI_RECOMMENDATION_REJECTED"
    const val AI_RECOMMENDATION_IGNORED = "AI_RECOMMENDATION_IGNORED"
    const val HABIT_COMPLETED = "HABIT_COMPLETED"
    const val HABIT_MISSED = "HABIT_MISSED"
    const val EVENING_REVIEW_COMPLETED = "EVENING_REVIEW_COMPLETED"
    const val DAILY_RATING_SUBMITTED = "DAILY_RATING_SUBMITTED"
    const val FOCUS_BLOCK_COMPLETED = "FOCUS_BLOCK_COMPLETED"
    const val FOCUS_BLOCK_MISSED = "FOCUS_BLOCK_MISSED"
}

enum class ConfidenceLevel(val displayName: String, val thresholdDesc: String) {
    INSUFFICIENT_DATA("Learning Patterns...", "Fewer than 3 observations recorded"),
    LOW_CONFIDENCE("Early Indication", "3 to 6 observations recorded"),
    MODERATE_CONFIDENCE("Moderate Confidence", "7 to 14 observations recorded"),
    HIGH_CONFIDENCE("High Confidence", "15+ consistent observations recorded");

    companion object {
        fun fromCount(count: Int): ConfidenceLevel {
            return when {
                count < 3 -> INSUFFICIENT_DATA
                count in 3..6 -> LOW_CONFIDENCE
                count in 7..14 -> MODERATE_CONFIDENCE
                else -> HIGH_CONFIDENCE
            }
        }
    }
}

data class AccuracyFactor(
    val title: String,
    val score: Int, // 0..100
    val weightPercent: Int,
    val explanation: String,
    val status: String // "EXCELLENT", "GOOD", "NEEDS_IMPROVEMENT"
)

data class PlanningAccuracyReport(
    val overallScore: Int, // 0..100
    val confidence: ConfidenceLevel,
    val workloadCompletionRate: Float, // 0..1
    val estimationAccuracyRate: Float, // 0..1
    val rolloverControlRate: Float, // 0..1
    val scheduleAdherenceRate: Float, // 0..1
    val headline: String,
    val detailedSummary: String,
    val factors: List<AccuracyFactor>,
    val totalObservations: Int
)

data class PersonalCapacityModel(
    val averageRealisticDailyHours: Float,
    val averageFocusCapacityHours: Float,
    val averageCompletedTaskCount: Float,
    val averageCompletedFocusHours: Float,
    val averageRolloverRatePercent: Int,
    val averageEstimationErrorPercent: Int,
    val workdayCapacityHours: Float,
    val weekendCapacityHours: Float,
    val morningCapacityPercent: Int,
    val afternoonCapacityPercent: Int,
    val eveningCapacityPercent: Int,
    val energyLevelCapacities: Map<String, Float>, // "HIGH", "MEDIUM", "LOW" in hours
    val confidence: ConfidenceLevel,
    val daysAnalyzed: Int
)

data class ProductivityPatternsReport(
    val mostProductiveHours: String, // e.g. "09:00 - 11:00"
    val leastProductiveHours: String, // e.g. "14:00 - 16:00"
    val bestFocusPeriod: String, // e.g. "Morning (09:00 - 12:00)"
    val strongestDayOfWeek: String, // e.g. "Tuesday"
    val weekdayCompletionRate: Int, // 0..100 %
    val weekendCompletionRate: Int, // 0..100 %
    val frequentlyPostponedCategories: List<String>,
    val frequentlyUnderestimatedCategories: List<String>,
    val priorityCompletionRates: Map<String, Int>, // "CRITICAL" -> 92, "IMPORTANT" -> 80, etc.
    val energyCompletionRates: Map<String, Int>, // "HIGH" -> 75, "MEDIUM" -> 85, etc.
    val overplanningTendencyPercent: Int, // e.g. +22%
    val confidence: ConfidenceLevel,
    val totalEventsAnalyzed: Int
)

data class TaskDurationPrediction(
    val predictedMinutes: Int,
    val confidence: ConfidenceLevel,
    val sampleSize: Int,
    val explanation: String,
    val isSufficientData: Boolean,
    val historicalAverageMinutes: Int,
    val category: String
)

data class PersonalizedInsightItem(
    val id: String,
    val title: String,
    val insightText: String,
    val whyExplanation: String,
    val category: String, // "CAPACITY", "TIMING", "PLANNING", "ENERGY"
    val confidence: ConfidenceLevel,
    val evidencePoints: List<String>,
    val recommendationType: String,
    var feedbackState: String = "NONE" // "NONE", "HELPFUL", "NOT_HELPFUL", "DONT_SUGGEST_AGAIN"
)

data class StructuredPersonalizationContext(
    val userName: String,
    val typicalDailyCapacityHours: Float,
    val typicalFocusHours: Float,
    val planningAccuracyScore: Int,
    val rolloverRatePercent: Int,
    val bestFocusPeriod: String,
    val strongestDay: String,
    val underestimatedCategories: List<String>,
    val postponedCategories: List<String>,
    val priorityCompletionSummary: String,
    val dataConfidence: String,
    val totalDaysHistory: Int
)
