package com.example.data

data class AdaptiveInterviewQuestion(
    val id: String,
    val question: String,
    val contextTopic: String,
    val options: List<String>,
    val allowCustomInput: Boolean = true,
    val isFinalQuestion: Boolean = false,
    val questionIndex: Int = 1,
    val totalEstimatedQuestions: Int = 3
)

data class InterviewHistoryItem(
    val question: AdaptiveInterviewQuestion,
    val selectedAnswer: String
)

data class SuggestedHabitItem(
    val name: String,
    val targetValue: Float,
    val unit: String,
    val iconName: String,
    val isSelected: Boolean = true
)

data class SuggestedGoalItem(
    val title: String,
    val domain: String,
    val horizon: String,
    val firstMilestoneTitle: String,
    val firstMilestoneDesc: String,
    val isSelected: Boolean = true
)

data class PersonalizedPlannerConfig(
    val focusSummary: String,
    val topPriority: String,
    val planningStyle: String,
    val scheduleConstraints: String,
    val reminderIntensity: String,
    val suggestedStarterCategories: List<String>,
    val suggestedStarterHabits: List<SuggestedHabitItem>,
    val suggestedStarterGoal: SuggestedGoalItem?
)
