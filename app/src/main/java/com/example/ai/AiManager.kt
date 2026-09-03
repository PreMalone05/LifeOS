package com.example.ai

import android.content.Context
import android.util.Log
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Central orchestrator and Service Locator for Multi-Provider AI capabilities.
 * Decouples business logic from specific AI vendors (Gemini, OpenRouter, OpenAI, Local).
 * Handles automatic fallback and robust parsing.
 */
class AiManager private constructor(private val context: Context) {

    val keyStorage = SecureKeyStorage.getInstance(context)

    val geminiProvider = GeminiProvider(keyStorage)
    val openRouterProvider = OpenRouterProvider(keyStorage)
    val openAiProvider = GenericOpenAIProvider(
        keyStorage = keyStorage,
        providerId = ProviderType.OPENAI.id,
        displayName = ProviderType.OPENAI.displayName
    )
    val customProvider = GenericOpenAIProvider(
        keyStorage = keyStorage,
        providerId = ProviderType.CUSTOM.id,
        displayName = ProviderType.CUSTOM.displayName
    )

    private val providers = mapOf(
        ProviderType.GEMINI.id to geminiProvider,
        ProviderType.OPENROUTER.id to openRouterProvider,
        ProviderType.OPENAI.id to openAiProvider,
        ProviderType.CUSTOM.id to customProvider
    )

    companion object {
        private const val TAG = "AiManager"

        @Volatile
        private var INSTANCE: AiManager? = null

        fun initialize(context: Context): AiManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getInstance(): AiManager {
            return INSTANCE ?: throw IllegalStateException("AiManager has not been initialized. Call initialize(context) first.")
        }

        fun getInstanceOrNull(): AiManager? = INSTANCE
    }

    fun getProvider(providerId: String): AiProvider {
        return providers[providerId.lowercase()] ?: geminiProvider
    }

    fun getActiveProvider(): AiProvider {
        val activeId = keyStorage.getActiveProvider()
        return getProvider(activeId)
    }

    fun getAllProviders(): List<AiProvider> = providers.values.toList()

    suspend fun testConnection(providerId: String, model: String? = null): ConnectionTestResult {
        return getProvider(providerId).testConnection(model)
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        model: String? = null
    ): String = withContext(Dispatchers.IO) {
        val active = getActiveProvider()
        val response = active.generateText(prompt, systemInstruction, model)

        if (response.isSuccess && response.text.isNotBlank()) {
            return@withContext response.text
        }

        Log.w(TAG, "Active provider (${active.providerId}) failed: ${response.errorMessage}. Attempting Gemini fallback if different.")
        if (active.providerId != ProviderType.GEMINI.id) {
            val fallbackResponse = geminiProvider.generateText(prompt, systemInstruction)
            if (fallbackResponse.isSuccess && fallbackResponse.text.isNotBlank()) {
                return@withContext fallbackResponse.text
            }
        }

        return@withContext response.text.ifBlank {
            response.errorMessage ?: "Unable to generate AI content. Please verify your provider settings."
        }
    }

    suspend fun generateStructuredJson(
        prompt: String,
        systemInstruction: String? = null,
        schemaHint: String? = null,
        model: String? = null
    ): String = withContext(Dispatchers.IO) {
        val active = getActiveProvider()
        val response = active.generateStructuredJson(prompt, systemInstruction, schemaHint, model)

        if (response.isSuccess && response.text.isNotBlank()) {
            return@withContext cleanJsonResponse(response.text)
        }

        if (active.providerId != ProviderType.GEMINI.id) {
            val fallbackResponse = geminiProvider.generateStructuredJson(prompt, systemInstruction, schemaHint)
            if (fallbackResponse.isSuccess && fallbackResponse.text.isNotBlank()) {
                return@withContext cleanJsonResponse(fallbackResponse.text)
            }
        }

        return@withContext cleanJsonResponse(response.text)
    }

    suspend fun generateChatResponse(
        messages: List<ChatMessageEntity>,
        systemInstruction: String? = null,
        model: String? = null
    ): String = withContext(Dispatchers.IO) {
        val active = getActiveProvider()
        val response = active.generateChat(messages, systemInstruction, model)

        if (response.isSuccess && response.text.isNotBlank()) {
            return@withContext response.text
        }

        if (active.providerId != ProviderType.GEMINI.id) {
            val fallbackResponse = geminiProvider.generateChat(messages, systemInstruction)
            if (fallbackResponse.isSuccess && fallbackResponse.text.isNotBlank()) {
                return@withContext fallbackResponse.text
            }
        }

        return@withContext response.text.ifBlank {
            response.errorMessage ?: "I had trouble contacting the AI provider. Please verify your API key and provider configuration."
        }
    }

    // --- High-Level LifeOS Application Intelligent Features ---

    suspend fun generateNextAdaptiveInterviewQuestion(
        userName: String,
        selectedInterests: List<String>,
        history: List<InterviewHistoryItem>,
        questionIndex: Int
    ): AdaptiveInterviewQuestion = withContext(Dispatchers.IO) {
        val totalEstimated = 3
        val isFinal = questionIndex >= totalEstimated

        val interestsStr = selectedInterests.joinToString(", ")
        val historyStr = if (history.isEmpty()) {
            "No prior answers yet (this is question 1)."
        } else {
            history.joinToString("\n") { item ->
                "Q: ${item.question.question} -> A: ${item.selectedAnswer}"
            }
        }

        val prompt = """
            You are LifeOS, an elite AI productivity architect conducting a short, personalized onboarding interview.
            User Name: $userName
            User's Selected Focus Interests: [$interestsStr]
            Previous Q&A History:
            $historyStr
            
            Current Step: Question $questionIndex of $totalEstimated.
            Is this the final question: $isFinal.
            
            Based on the user's specific interests and previous answers, generate ONE highly relevant, punchy question with 3-4 realistic multiple-choice options.
            Focus on uncovering their true day-to-day commitments, peak energy hours, preferred planning style (e.g. time-blocking vs checklist), or primary current objective.
            
            Return ONLY a valid JSON object matching this schema (do NOT include markdown backticks or commentary):
            {
                "id": "q_$questionIndex",
                "question": "Clear, direct question text tailored to the user",
                "contextTopic": "Short topic header (e.g. PRIMARY OBJECTIVE, DAILY SCHEDULE, PLANNING STYLE)",
                "options": [
                    "Option 1",
                    "Option 2",
                    "Option 3",
                    "Option 4"
                ],
                "isFinalQuestion": $isFinal
            }
        """.trimIndent()

        val rawResponse = generateContent(prompt)
        val cleaned = cleanJsonResponse(rawResponse)

        try {
            val json = JSONObject(cleaned)
            val id = json.optString("id", "q_$questionIndex")
            val question = json.optString("question", getDefaultQuestionText(selectedInterests, questionIndex))
            val contextTopic = json.optString("contextTopic", getDefaultTopic(questionIndex))
            val optsArray = json.optJSONArray("options")
            val options = mutableListOf<String>()
            if (optsArray != null) {
                for (i in 0 until optsArray.length()) {
                    options.add(optsArray.getString(i))
                }
            }
            if (options.isEmpty()) {
                options.addAll(getDefaultOptions(questionIndex))
            }
            val finalQ = json.optBoolean("isFinalQuestion", isFinal)

            AdaptiveInterviewQuestion(
                id = id,
                question = question,
                contextTopic = contextTopic,
                options = options,
                allowCustomInput = true,
                isFinalQuestion = finalQ,
                questionIndex = questionIndex,
                totalEstimatedQuestions = totalEstimated
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse interview question JSON: $cleaned", e)
            fallbackQuestion(selectedInterests, questionIndex, isFinal, totalEstimated)
        }
    }

    suspend fun generatePersonalizedPlannerConfig(
        userName: String,
        selectedInterests: List<String>,
        history: List<InterviewHistoryItem>
    ): PersonalizedPlannerConfig = withContext(Dispatchers.IO) {
        val interestsStr = selectedInterests.joinToString(", ")
        val answersStr = history.joinToString("\n") { item ->
            "- Topic: ${item.question.contextTopic} | Question: ${item.question.question} | User Answer: ${item.selectedAnswer}"
        }

        val prompt = """
            You are LifeOS AI. Analyze the user's selected interests and interview answers to synthesize a complete personalized planner configuration.
            
            User Name: $userName
            Selected Interests: [$interestsStr]
            Interview Responses:
            $answersStr
            
            Synthesize a cohesive, high-impact plan configuration. ONLY use facts the user affirmed.
            
            Return ONLY a valid JSON object matching this schema (do NOT include markdown backticks or commentary):
            {
                "focusSummary": "A concise 1-sentence summary of their primary daily focus",
                "topPriority": "Their main priority or upcoming milestone target",
                "planningStyle": "Time Blocking / Structured Checklist / High-Flexibility Focus Blocks",
                "scheduleConstraints": "Summary of their daily availability & work commitments",
                "reminderIntensity": "Minimal / Balanced / High Accountability",
                "suggestedStarterCategories": ["WORK", "HEALTH", "GROWTH", "ROUTINE"],
                "suggestedStarterHabits": [
                    {
                        "name": "Habit Name",
                        "targetValue": 30.0,
                        "unit": "min",
                        "iconName": "fitness_center"
                    },
                    {
                        "name": "Habit Name 2",
                        "targetValue": 2.0,
                        "unit": "L",
                        "iconName": "water_drop"
                    }
                ],
                "suggestedStarterGoal": {
                    "title": "A meaningful starting goal based on their stated interest",
                    "domain": "Growth",
                    "horizon": "Quarterly",
                    "firstMilestoneTitle": "First actionable phase",
                    "firstMilestoneDesc": "Clear description of initial setup"
                }
            }
        """.trimIndent()

        val rawResponse = generateContent(prompt)
        val cleaned = cleanJsonResponse(rawResponse)

        try {
            val json = JSONObject(cleaned)
            val focusSummary = json.optString("focusSummary", "Personalized focus balancing ${selectedInterests.take(2).joinToString(" & ")}")
            val topPriority = json.optString("topPriority", "Build momentum and daily consistency")
            val planningStyle = json.optString("planningStyle", "Time Blocking & Priority Checklist")
            val scheduleConstraints = json.optString("scheduleConstraints", "Standard daily routine with focused priority blocks")
            val reminderIntensity = json.optString("reminderIntensity", "Balanced")

            val categoriesList = mutableListOf<String>()
            val catArr = json.optJSONArray("suggestedStarterCategories")
            if (catArr != null) {
                for (i in 0 until catArr.length()) {
                    categoriesList.add(catArr.getString(i))
                }
            }
            if (categoriesList.isEmpty()) {
                categoriesList.addAll(listOf("WORK", "HEALTH", "GROWTH", "ROUTINE"))
            }

            val habitsList = mutableListOf<SuggestedHabitItem>()
            val habitArr = json.optJSONArray("suggestedStarterHabits")
            if (habitArr != null) {
                for (i in 0 until habitArr.length()) {
                    val h = habitArr.getJSONObject(i)
                    habitsList.add(
                        SuggestedHabitItem(
                            name = h.optString("name", "Daily Habit"),
                            targetValue = h.optDouble("targetValue", 1.0).toFloat(),
                            unit = h.optString("unit", "reps"),
                            iconName = h.optString("iconName", "check_circle"),
                            isSelected = true
                        )
                    )
                }
            }
            if (habitsList.isEmpty()) {
                habitsList.addAll(getDefaultSuggestedHabits(selectedInterests))
            }

            val goalObj = json.optJSONObject("suggestedStarterGoal")
            val goalItem = if (goalObj != null) {
                SuggestedGoalItem(
                    title = goalObj.optString("title", "Establish Core Daily Focus"),
                    domain = goalObj.optString("domain", "Growth"),
                    horizon = goalObj.optString("horizon", "Quarterly"),
                    firstMilestoneTitle = goalObj.optString("firstMilestoneTitle", "Foundation Setup"),
                    firstMilestoneDesc = goalObj.optString("firstMilestoneDesc", "Set up primary routines and baseline materials"),
                    isSelected = true
                )
            } else {
                getDefaultSuggestedGoal(selectedInterests)
            }

            PersonalizedPlannerConfig(
                focusSummary = focusSummary,
                topPriority = topPriority,
                planningStyle = planningStyle,
                scheduleConstraints = scheduleConstraints,
                reminderIntensity = reminderIntensity,
                suggestedStarterCategories = categoriesList,
                suggestedStarterHabits = habitsList,
                suggestedStarterGoal = goalItem
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse planner config JSON: $cleaned", e)
            fallbackPlannerConfig(userName, selectedInterests, history)
        }
    }

    suspend fun generateAdaptiveRebalancePlan(
        userName: String,
        coachPersonality: String,
        currentTasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        rebalanceTrigger: String,
        availableHours: Float,
        currentLocalTime: String
    ): AdaptiveRebalanceResult = withContext(Dispatchers.IO) {
        val tasksFormatted = currentTasks.joinToString("\n") { task ->
            "- [ID: ${task.id}] Title: ${task.title} | Priority: ${task.priority} | Time: ${task.timeSlot} | Duration: ${task.durationHours}h | Status: ${if (task.isCompleted) "COMPLETED" else "PENDING"} | Reschedule Count: ${task.rescheduleCount}"
        }

        val eventsFormatted = if (calendarEvents.isNotEmpty()) {
            calendarEvents.joinToString("\n") { "- Event: ${it.title} | Time: ${it.formattedTime}" }
        } else {
            "No conflicting calendar events."
        }

        val prompt = """
            You are $coachPersonality, an elite AI adaptive planning engine.
            The user $userName encountered a schedule change: "$rebalanceTrigger".
            Current Local Time: $currentLocalTime.
            Available Working Window: $availableHours hours.
            
            Current Tasks:
            $tasksFormatted
            
            Fixed Calendar Events:
            $eventsFormatted
            
            OBJECTIVE:
            Rebalance the remainder of the day realistically:
            1. PROTECT CRITICAL TASKS AT ALL COSTS. Keep them today and shift timeslots forward cleanly.
            2. For IMPORTANT tasks, fit them if capacity allows, adjusting start times.
            3. For FLEXIBLE tasks, adjust or defer to tomorrow if running tight.
            4. For OPTIONAL tasks, postpone to tomorrow or drop to prevent late-night stress/burnout.
            5. Ensure adequate 15-30 minute transition buffers between blocks.
            
            Return ONLY a valid JSON object matching this schema (no markdown, no explanations outside JSON):
            {
                "summary": "1-2 sentence strategic explanation of how the schedule was rebalanced",
                "bufferRestoredMinutes": 30,
                "keptTasks": [
                    {
                        "taskId": 1,
                        "title": "Task title",
                        "originalTimeSlot": "09:00 - 10:30 AM",
                        "newTimeSlot": "10:00 - 11:30 AM",
                        "priority": "CRITICAL",
                        "action": "KEEP_TODAY",
                        "reason": "Protected critical objective, shifted start forward by 60 min"
                    }
                ],
                "deferredTasks": [
                    {
                        "taskId": 2,
                        "title": "Task title",
                        "originalTimeSlot": "02:00 - 03:00 PM",
                        "newTimeSlot": "Tomorrow 09:00 AM",
                        "priority": "OPTIONAL",
                        "action": "DEFER_TOMORROW",
                        "reason": "Deferred to protect evening buffer and prevent rollover fatigue"
                    }
                ],
                "droppedTasks": []
            }
        """.trimIndent()

        val raw = generateContent(prompt)
        val cleaned = cleanJsonResponse(raw)
        try {
            val json = JSONObject(cleaned)
            val summary = json.optString("summary", "Schedule adjusted to accommodate $rebalanceTrigger.")
            val bufferRestored = json.optInt("bufferRestoredMinutes", 30)

            fun parseTaskList(key: String): List<RebalanceTaskProposal> {
                val list = mutableListOf<RebalanceTaskProposal>()
                val arr = json.optJSONArray(key) ?: return list
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        RebalanceTaskProposal(
                            taskId = obj.optInt("taskId", 0),
                            title = obj.optString("title", ""),
                            originalTimeSlot = obj.optString("originalTimeSlot", ""),
                            newTimeSlot = obj.optString("newTimeSlot", ""),
                            priority = obj.optString("priority", "IMPORTANT"),
                            action = obj.optString("action", "KEEP_TODAY"),
                            reason = obj.optString("reason", "")
                        )
                    )
                }
                return list
            }

            AdaptiveRebalanceResult(
                summary = summary,
                triggerReason = rebalanceTrigger,
                keptTasks = parseTaskList("keptTasks"),
                deferredTasks = parseTaskList("deferredTasks"),
                droppedTasks = parseTaskList("droppedTasks"),
                bufferRestoredMinutes = bufferRestored
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse rebalance JSON, using deterministic fallback", e)
            fallbackRebalance(currentTasks, rebalanceTrigger)
        }
    }

    suspend fun generateCapacityAnalysis(
        userName: String,
        plannedTasks: List<TaskEntity>,
        historicalAvgHours: Float,
        calendarMeetingsHours: Float,
        totalWorkWindowHours: Float
    ): AdaptiveCapacityReport = withContext(Dispatchers.IO) {
        val totalPlannedHours = plannedTasks.sumOf { it.durationHours }.toFloat()
        val criticalHours = plannedTasks.filter { it.priority == "CRITICAL" }.sumOf { it.durationHours }.toFloat()
        val availableHours = (totalWorkWindowHours - calendarMeetingsHours).coerceAtLeast(0f)

        val prompt = """
            You are an adaptive AI capacity intelligence model.
            User: $userName
            Total Planned Task Hours: $totalPlannedHours h (${plannedTasks.size} tasks)
            Critical Hours: $criticalHours h
            Calendar Meetings: $calendarMeetingsHours h
            Net Available Focus Window: $availableHours h
            Historical Observed Daily Completion Avg: $historicalAvgHours h / day
            
            Evaluate if the day is OPTIMAL, HEAVY, OVERCOMMITTED, or LIGHT.
            Highlight transparent facts:
            - Clearly cite observed past averages (e.g. 7-day average: $historicalAvgHours h)
            - Show estimated buffer
            
            Return ONLY a valid JSON object matching this schema (no markdown):
            {
                "capacityStatus": "OPTIMAL", // "OPTIMAL" / "HEAVY" / "OVERCOMMITTED" / "LIGHT"
                "realisticTaskCap": 4,
                "realityInsight": "1-2 sentence evidence-based reality check on workload",
                "transparentObservations": [
                    "Observed: You average $historicalAvgHours h of completed deep work daily",
                    "Estimated: Today's scheduled commitments total $totalPlannedHours h"
                ],
                "actionableTips": [
                    "Protect the 2 Critical tasks first",
                    "Leave 45 min buffer before afternoon meetings"
                ]
            }
        """.trimIndent()

        val raw = generateContent(prompt)
        val cleaned = cleanJsonResponse(raw)
        try {
            val json = JSONObject(cleaned)
            val status = json.optString("capacityStatus", if (totalPlannedHours > availableHours) "OVERCOMMITTED" else "OPTIMAL")
            val cap = json.optInt("realisticTaskCap", 4)
            val insight = json.optString("realityInsight", "Planned workload is balanced within your energy baseline.")

            val obsArray = json.optJSONArray("transparentObservations")
            val obs = mutableListOf<String>()
            if (obsArray != null) {
                for (i in 0 until obsArray.length()) obs.add(obsArray.getString(i))
            }
            if (obs.isEmpty()) {
                obs.add("Observed average: $historicalAvgHours h completed daily")
                obs.add("Planned today: $totalPlannedHours h across ${plannedTasks.size} tasks")
            }

            val tipsArray = json.optJSONArray("actionableTips")
            val tips = mutableListOf<String>()
            if (tipsArray != null) {
                for (i in 0 until tipsArray.length()) tips.add(tipsArray.getString(i))
            }

            AdaptiveCapacityReport(
                capacityStatus = status,
                plannedHours = totalPlannedHours,
                availableHours = availableHours,
                realisticTaskCap = cap,
                realityInsight = insight,
                transparentObservations = obs,
                actionableTips = tips
            )
        } catch (e: Exception) {
            val status = if (totalPlannedHours > availableHours + 0.5f) "OVERCOMMITTED" else if (totalPlannedHours > availableHours - 1.0f) "HEAVY" else "OPTIMAL"
            AdaptiveCapacityReport(
                capacityStatus = status,
                plannedHours = totalPlannedHours,
                availableHours = availableHours,
                realisticTaskCap = 4,
                realityInsight = if (status == "OVERCOMMITTED") "Planned tasks exceed your net focus window. Consider shifting flexible tasks." else "Your planned day fits comfortably within your focus capacity.",
                transparentObservations = listOf(
                    "Observed average: $historicalAvgHours h completed daily",
                    "Planned today: $totalPlannedHours h across ${plannedTasks.size} tasks",
                    "Net available focus window: $availableHours h"
                ),
                actionableTips = listOf(
                    "Execute Critical priorities before midday",
                    "Maintain 15-minute buffers between blocks"
                )
            )
        }
    }

    suspend fun generateEveningReview(
        userName: String,
        coachPersonality: String,
        completedTasks: List<TaskEntity>,
        pendingTasks: List<TaskEntity>,
        habitsCompletedCount: Int,
        totalHabitsCount: Int,
        focusPointsEarned: Float
    ): EveningReviewSummary = withContext(Dispatchers.IO) {
        val completedStr = completedTasks.joinToString(", ") { it.title }.ifBlank { "None" }
        val pendingStr = pendingTasks.joinToString(", ") { "${it.title} (${it.priority})" }.ifBlank { "None" }

        val prompt = """
            You are $coachPersonality, reviewing the user's completed day.
            User: $userName
            Completed Tasks (${completedTasks.size}): $completedStr
            Pending / Leftover Tasks (${pendingTasks.size}): $pendingStr
            Habits: $habitsCompletedCount of $totalHabitsCount finished
            Focus Points Gained: $focusPointsEarned pts
            
            Synthesize an evening wind-down summary:
            1. Praise what was accomplished without inflated fluff.
            2. Give practical advice on what to do with pending tasks (roll to tomorrow vs discard).
            3. Suggest a realistic day rating: "DOMINANT", "BALANCED", "RECOVERY", or "TOUGH".
            
            Return ONLY a valid JSON object matching this schema (no markdown):
            {
                "suggestedScore": "BALANCED",
                "coachPraise": "Direct praise on effort and execution",
                "completionSummary": "Summary of achievements today",
                "leftoverTriageAdvice": "Recommendation for pending tasks",
                "tomorrowSetupAdvice": "1-sentence evening setup tip for tomorrow"
            }
        """.trimIndent()

        val raw = generateContent(prompt)
        val cleaned = cleanJsonResponse(raw)
        try {
            val json = JSONObject(cleaned)
            EveningReviewSummary(
                suggestedScore = json.optString("suggestedScore", "BALANCED"),
                coachPraise = json.optString("coachPraise", "Solid effort today. You moved the needle on your key objectives."),
                completionSummary = json.optString("completionSummary", "Completed ${completedTasks.size} tasks and $habitsCompletedCount habits."),
                leftoverTriageAdvice = json.optString("leftoverTriageAdvice", if (pendingTasks.isNotEmpty()) "Roll remaining items to tomorrow's focus blocks." else "Clean slate achieved!"),
                tomorrowSetupAdvice = json.optString("tomorrowSetupAdvice", "Get quality rest and begin tomorrow with your highest-priority focus task.")
            )
        } catch (e: Exception) {
            EveningReviewSummary(
                suggestedScore = if (completedTasks.isNotEmpty()) "BALANCED" else "RECOVERY",
                coachPraise = "You showed up and executed. Consistency builds mastery over time.",
                completionSummary = "Completed ${completedTasks.size} tasks today ($focusPointsEarned focus points earned).",
                leftoverTriageAdvice = if (pendingTasks.isNotEmpty()) "Roll ${pendingTasks.size} pending tasks to tomorrow." else "All scheduled tasks completed!",
                tomorrowSetupAdvice = "Wind down and prepare your focus blocks for tomorrow morning."
            )
        }
    }

    suspend fun generatePersonalizedInsights(
        context: StructuredPersonalizationContext
    ): List<PersonalizedInsightItem> = withContext(Dispatchers.IO) {
        val prompt = """
            You are the AI Personalization & Learning Engine for LifeOS.
            Analyze the following summarized behavioral profile and generate 2 to 4 actionable, highly personalized productivity insights.
            
            IMPORTANT PRIVACY RULE: Only use the summarized numerical metrics below.
            
            USER PROFILE SUMMARY:
            - User: ${context.userName}
            - Typical Realistic Daily Capacity: ${context.typicalDailyCapacityHours} hours
            - Typical Focus Capacity: ${context.typicalFocusHours} hours
            - Planning Accuracy Score: ${context.planningAccuracyScore}/100
            - Average Rollover Rate: ${context.rolloverRatePercent}%
            - Peak Focus Period: ${context.bestFocusPeriod}
            - Strongest Execution Day: ${context.strongestDay}
            - Frequently Underestimated Categories: ${context.underestimatedCategories.joinToString(", ")}
            - Frequently Postponed Categories: ${context.postponedCategories.joinToString(", ")}
            - Priority Completion Summary: ${context.priorityCompletionSummary}
            - Data Confidence Level: ${context.dataConfidence}
            - Recorded History Window: ${context.totalDaysHistory} active days

            OUTPUT REQUIREMENT:
            Return ONLY a valid JSON array of objects with the exact schema:
            [
              {
                "id": "timing_peak_flow",
                "title": "Short punchy title (2-4 words)",
                "insightText": "Actionable personalized advice directly referencing the user's data.",
                "whyExplanation": "Clear, transparent explanation of WHY this insight was generated based on the numbers.",
                "category": "TIMING", // One of: "TIMING", "CAPACITY", "PLANNING", "ENERGY"
                "confidence": "${context.dataConfidence}", // INSUFFICIENT_DATA, LOW_CONFIDENCE, MODERATE_CONFIDENCE, HIGH_CONFIDENCE
                "evidencePoints": ["Key metric 1", "Key metric 2"],
                "recommendationType": "TIME_SLOT_SUGGESTION" // e.g. "TIME_SLOT_SUGGESTION", "TASK_DURATION", "DEEP_WORK_BLOCK", "REBALANCE_ADVICE"
              }
            ]
        """.trimIndent()

        try {
            val response = generateContent(prompt)
            val cleaned = cleanJsonResponse(response)
            val jsonArray = JSONArray(cleaned)
            val results = mutableListOf<PersonalizedInsightItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", "insight_$i")
                val title = obj.optString("title", "Productivity Insight")
                val insightText = obj.optString("insightText", "")
                val whyExplanation = obj.optString("whyExplanation", "Derived from your planning history.")
                val category = obj.optString("category", "PLANNING")
                val confStr = obj.optString("confidence", context.dataConfidence)
                val conf = try { ConfidenceLevel.valueOf(confStr) } catch (e: Exception) { ConfidenceLevel.MODERATE_CONFIDENCE }

                val evidencePoints = mutableListOf<String>()
                val evArray = obj.optJSONArray("evidencePoints")
                if (evArray != null) {
                    for (j in 0 until evArray.length()) {
                        evidencePoints.add(evArray.getString(j))
                    }
                }
                if (evidencePoints.isEmpty()) {
                    evidencePoints.add("Accuracy Score: ${context.planningAccuracyScore}/100")
                }
                val recType = obj.optString("recommendationType", "REBALANCE_ADVICE")

                results.add(
                    PersonalizedInsightItem(
                        id = id,
                        title = title,
                        insightText = insightText,
                        whyExplanation = whyExplanation,
                        category = category,
                        confidence = conf,
                        evidencePoints = evidencePoints,
                        recommendationType = recType
                    )
                )
            }

            if (results.isNotEmpty()) {
                return@withContext results
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate personalized insights: ${e.message}")
        }

        // Deterministic fallback based on actual context
        return@withContext listOf(
            PersonalizedInsightItem(
                id = "timing_flow_window",
                title = "Peak Focus Window",
                insightText = "Block high-cognitive tasks in your ${context.bestFocusPeriod} for peak completion velocity.",
                whyExplanation = "You consistently finish complex tasks faster and with lower postponement during morning focus blocks.",
                category = "TIMING",
                confidence = try { ConfidenceLevel.valueOf(context.dataConfidence) } catch (e: Exception) { ConfidenceLevel.MODERATE_CONFIDENCE },
                evidencePoints = listOf("Best Focus Period: ${context.bestFocusPeriod}", "Strongest day: ${context.strongestDay}"),
                recommendationType = "TIME_SLOT_SUGGESTION"
            ),
            PersonalizedInsightItem(
                id = "capacity_calibration",
                title = "Realistic Daily Load",
                insightText = "Aim for ${context.typicalDailyCapacityHours}h of scheduled tasks per day to maintain a ${context.planningAccuracyScore}% planning accuracy score.",
                whyExplanation = "Days planned beyond ${context.typicalDailyCapacityHours + 1.0f}h correlate with an elevated rollover rate of ~${context.rolloverRatePercent}%.",
                category = "CAPACITY",
                confidence = try { ConfidenceLevel.valueOf(context.dataConfidence) } catch (e: Exception) { ConfidenceLevel.MODERATE_CONFIDENCE },
                evidencePoints = listOf("Daily capacity: ${context.typicalDailyCapacityHours}h", "Rollover rate: ${context.rolloverRatePercent}%"),
                recommendationType = "DEEP_WORK_BLOCK"
            ),
            PersonalizedInsightItem(
                id = "estimation_buffer",
                title = "Category Buffer Tuning",
                insightText = "Add 15m buffer when scheduling ${context.underestimatedCategories.firstOrNull() ?: "WORK"} tasks.",
                whyExplanation = "${context.underestimatedCategories.firstOrNull() ?: "WORK"} tasks historically take 20-30% longer than initial estimates.",
                category = "PLANNING",
                confidence = try { ConfidenceLevel.valueOf(context.dataConfidence) } catch (e: Exception) { ConfidenceLevel.MODERATE_CONFIDENCE },
                evidencePoints = listOf("Underestimated: ${context.underestimatedCategories.joinToString()}", "Accuracy score: ${context.planningAccuracyScore}/100"),
                recommendationType = "TASK_DURATION"
            )
        )
    }

    private fun cleanJsonResponse(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json").trim()
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```").trim()
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```").trim()
        }
        return clean
    }

    private fun getDefaultQuestionText(interests: List<String>, index: Int): String {
        return when (index) {
            1 -> "What is your primary focus for this season?"
            2 -> "How do you prefer structuring your day?"
            else -> "What is your current daily work schedule?"
        }
    }

    private fun getDefaultTopic(index: Int): String {
        return when (index) {
            1 -> "PRIMARY FOCUS"
            2 -> "PLANNING STYLE"
            else -> "DAILY SCHEDULE"
        }
    }

    private fun getDefaultOptions(index: Int): List<String> {
        return when (index) {
            1 -> listOf(
                "Career & Project Sprints",
                "Physical Health & Energy",
                "Deep Learning & Skill Mastery",
                "Work-Life Balance & Recovery"
            )
            2 -> listOf(
                "Time-blocking with strict calendar slots",
                "Prioritized daily checklist",
                "Flexible 90-minute deep focus sprints",
                "Light flow state with minimal rigidity"
            )
            else -> listOf(
                "Standard 9-to-5 workday",
                "Flexible freelance hours",
                "Split morning & evening blocks",
                "Full focus (no fixed job constraints)"
            )
        }
    }

    private fun fallbackQuestion(
        interests: List<String>,
        index: Int,
        isFinal: Boolean,
        total: Int
    ): AdaptiveInterviewQuestion {
        return AdaptiveInterviewQuestion(
            id = "fallback_q_$index",
            question = getDefaultQuestionText(interests, index),
            contextTopic = getDefaultTopic(index),
            options = getDefaultOptions(index),
            allowCustomInput = true,
            isFinalQuestion = isFinal,
            questionIndex = index,
            totalEstimatedQuestions = total
        )
    }

    private fun fallbackPlannerConfig(
        userName: String,
        interests: List<String>,
        history: List<InterviewHistoryItem>
    ): PersonalizedPlannerConfig {
        return PersonalizedPlannerConfig(
            focusSummary = "Personalized focus tailored for ${interests.take(2).joinToString(" & ")}",
            topPriority = "Build daily consistency and complete high-leverage milestones",
            planningStyle = "Time Blocking + Priority Checklist",
            scheduleConstraints = "Standard daily routine with focused priority blocks",
            reminderIntensity = "Balanced",
            suggestedStarterCategories = listOf("WORK", "HEALTH", "GROWTH", "ROUTINE"),
            suggestedStarterHabits = getDefaultSuggestedHabits(interests),
            suggestedStarterGoal = getDefaultSuggestedGoal(interests)
        )
    }

    private fun fallbackRebalance(tasks: List<TaskEntity>, trigger: String): AdaptiveRebalanceResult {
        val pending = tasks.filter { !it.isCompleted }
        val kept = mutableListOf<RebalanceTaskProposal>()
        val deferred = mutableListOf<RebalanceTaskProposal>()

        pending.forEachIndexed { index, task ->
            if (task.priority == "CRITICAL" || index < 2) {
                kept.add(
                    RebalanceTaskProposal(
                        taskId = task.id,
                        title = task.title,
                        originalTimeSlot = task.timeSlot,
                        newTimeSlot = task.timeSlot,
                        priority = task.priority,
                        action = "KEEP_TODAY",
                        reason = "High priority focus area retained for today"
                    )
                )
            } else if (task.priority == "OPTIONAL" || task.priority == "FLEXIBLE") {
                deferred.add(
                    RebalanceTaskProposal(
                        taskId = task.id,
                        title = task.title,
                        originalTimeSlot = task.timeSlot,
                        newTimeSlot = "Tomorrow",
                        priority = task.priority,
                        action = "DEFER_TOMORROW",
                        reason = "Shifted to tomorrow to preserve focus and buffer time"
                    )
                )
            } else {
                kept.add(
                    RebalanceTaskProposal(
                        taskId = task.id,
                        title = task.title,
                        originalTimeSlot = task.timeSlot,
                        newTimeSlot = task.timeSlot,
                        priority = task.priority,
                        action = "KEEP_TODAY",
                        reason = "Retained in schedule"
                    )
                )
            }
        }

        return AdaptiveRebalanceResult(
            summary = "Rebalanced your daily focus for: $trigger. Critical objectives remain protected.",
            triggerReason = trigger,
            keptTasks = kept,
            deferredTasks = deferred,
            droppedTasks = emptyList(),
            bufferRestoredMinutes = 30
        )
    }

    private fun getDefaultSuggestedHabits(interests: List<String>): List<SuggestedHabitItem> {
        val habits = mutableListOf<SuggestedHabitItem>()
        if (interests.any { it.contains("Fitness", ignoreCase = true) || it.contains("Health", ignoreCase = true) }) {
            habits.add(SuggestedHabitItem(name = "Daily Workout & Movement", targetValue = 45f, unit = "min", iconName = "fitness_center", isSelected = true))
            habits.add(SuggestedHabitItem(name = "Optimal Hydration", targetValue = 2.5f, unit = "L", iconName = "water_drop", isSelected = true))
        } else {
            habits.add(SuggestedHabitItem(name = "Deep Work Block", targetValue = 90f, unit = "min", iconName = "bolt", isSelected = true))
            habits.add(SuggestedHabitItem(name = "Daily Reading & Growth", targetValue = 20f, unit = "pages", iconName = "menu_book", isSelected = true))
        }
        return habits
    }

    private fun getDefaultSuggestedGoal(interests: List<String>): SuggestedGoalItem {
        return SuggestedGoalItem(
            title = "Mastery of ${interests.firstOrNull() ?: "Core Priorities"}",
            domain = if (interests.contains("Health & Fitness")) "Health" else "Career",
            horizon = "Quarterly",
            firstMilestoneTitle = "Establish Baseline Foundation",
            firstMilestoneDesc = "Audit existing workflows and set up dedicated daily deep work blocks.",
            isSelected = true
        )
    }
}
