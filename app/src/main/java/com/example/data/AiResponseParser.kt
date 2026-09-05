package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Structured kotlinx.serialization models and decoder for AI provider responses.
 * Replaces ad-hoc string and regex parsing with safe, schema-validated JSON decoding.
 */
object AiJsonParser {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    /**
     * Extracts the raw JSON structure (object or array) cleanly by locating the outermost
     * enclosing brackets, safely ignoring markdown code block fences (```json ... ```)
     * and preamble/postscript natural language commentary.
     */
    fun extractStructuredJson(raw: String): String {
        val trimmed = raw.trim()
        val firstBrace = trimmed.indexOf('{')
        val firstBracket = trimmed.indexOf('[')
        val startIdx = when {
            firstBrace >= 0 && firstBracket >= 0 -> minOf(firstBrace, firstBracket)
            firstBrace >= 0 -> firstBrace
            firstBracket >= 0 -> firstBracket
            else -> -1
        }
        if (startIdx == -1) return trimmed

        val isArray = trimmed[startIdx] == '['
        val endChar = if (isArray) ']' else '}'
        val lastIdx = trimmed.lastIndexOf(endChar)

        return if (lastIdx > startIdx) {
            trimmed.substring(startIdx, lastIdx + 1)
        } else {
            trimmed
        }
    }

    inline fun <reified T> decode(raw: String): T {
        val jsonPayload = extractStructuredJson(raw)
        return json.decodeFromString<T>(jsonPayload)
    }
}

@Serializable
data class AdaptiveInterviewQuestionDto(
    val id: String? = null,
    val question: String? = null,
    val contextTopic: String? = null,
    val options: List<String> = emptyList(),
    val isFinalQuestion: Boolean = false
)

@Serializable
data class SuggestedHabitItemDto(
    val name: String? = null,
    val targetValue: Float = 30f,
    val unit: String = "min",
    val iconName: String = "self_improvement",
    val isSelected: Boolean = true
)

@Serializable
data class SuggestedGoalItemDto(
    val title: String? = null,
    val domain: String = "Growth",
    val horizon: String = "Quarterly",
    val firstMilestoneTitle: String = "",
    val firstMilestoneDesc: String = "",
    val isSelected: Boolean = true
)

@Serializable
data class PersonalizedPlannerConfigDto(
    val focusSummary: String? = null,
    val topPriority: String? = null,
    val planningStyle: String? = null,
    val scheduleConstraints: String? = null,
    val reminderIntensity: String = "Balanced",
    val suggestedStarterCategories: List<String> = emptyList(),
    val suggestedStarterHabits: List<SuggestedHabitItemDto> = emptyList(),
    val suggestedStarterGoal: SuggestedGoalItemDto? = null
)

@Serializable
data class RebalanceTaskProposalDto(
    val taskId: Int = 0,
    val title: String = "",
    val originalTimeSlot: String = "",
    val newTimeSlot: String = "",
    val priority: String = "IMPORTANT",
    val action: String = "KEEP_TODAY",
    val reason: String = ""
)

@Serializable
data class AdaptiveRebalanceResultDto(
    val summary: String? = null,
    val bufferRestoredMinutes: Int = 30,
    val keptTasks: List<RebalanceTaskProposalDto> = emptyList(),
    val deferredTasks: List<RebalanceTaskProposalDto> = emptyList(),
    val droppedTasks: List<RebalanceTaskProposalDto> = emptyList()
)

@Serializable
data class AdaptiveCapacityReportDto(
    val capacityStatus: String? = null,
    val realisticTaskCap: Int = 4,
    val realityInsight: String? = null,
    val transparentObservations: List<String> = emptyList(),
    val actionableTips: List<String> = emptyList()
)

@Serializable
data class EveningReviewSummaryDto(
    val suggestedScore: String? = null,
    val coachPraise: String? = null,
    val completionSummary: String? = null,
    val leftoverTriageAdvice: String? = null,
    val tomorrowSetupAdvice: String? = null
)

@Serializable
data class PersonalizedInsightItemDto(
    val id: String? = null,
    val title: String? = null,
    val insightText: String? = null,
    val whyExplanation: String? = null,
    val category: String = "PLANNING",
    val confidence: String = "MODERATE_CONFIDENCE",
    val evidencePoints: List<String> = emptyList(),
    val recommendationType: String = "TIME_SLOT_SUGGESTION"
)
