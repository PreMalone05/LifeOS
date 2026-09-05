package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val DEFAULT_MODEL = "gemini-3.5-flash"
    private const val FALLBACK_MODEL = "gemini-flash-latest"
    private val CANDIDATE_MODELS = listOf(
        "gemini-3.5-flash",
        "gemini-flash-latest",
        "gemini-3.1-flash-lite-preview",
        "gemini-2.5-flash"
    )
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generateContent(prompt, systemInstruction)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or is placeholder.")
            return@withContext "API Key is missing. Please configure GEMINI_API_KEY in the Secrets panel."
        }

        var lastError = ""

        for (model in CANDIDATE_MODELS) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            try {
                val requestBodyJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                val partObj = JSONObject().apply {
                                    put("text", prompt)
                                }
                                put(partObj)
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)

                    if (systemInstruction != null) {
                        val sysInstObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                val partObj = JSONObject().apply {
                                    put("text", systemInstruction)
                                }
                                put(partObj)
                            }
                            put("parts", partsArray)
                        }
                        put("systemInstruction", sysInstObj)
                    }
                }

                val requestBody = requestBodyJson.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: return@use
                        val jsonResponse = JSONObject(bodyString)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            if (content != null) {
                                val parts = content.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                                }
                            }
                        }
                    } else {
                        val errBody = response.body?.string() ?: ""
                        Log.w(TAG, "Request failed for model $model with code: ${response.code}, attempting fallback model. body: $errBody")
                        lastError = "Code ${response.code}"
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception during content generation on model $model, trying fallback", e)
                lastError = e.localizedMessage ?: "Unknown error"
            }
        }

        return@withContext "Focus on your #1 high-impact priority today. Clear distractions during peak focus hours."
    }

    suspend fun generateChatResponse(
        history: List<ChatMessageEntity>,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generateChatResponse(history, systemInstruction)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or is placeholder.")
            return@withContext "API Key is missing. Please configure GEMINI_API_KEY in the Secrets panel."
        }

        for (model in CANDIDATE_MODELS) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            try {
                val requestBodyJson = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        history.forEach { msg ->
                            val contentObj = JSONObject().apply {
                                put("role", msg.role)
                                val partsArray = JSONArray().apply {
                                    val partObj = JSONObject().apply {
                                        put("text", msg.text)
                                    }
                                    put(partObj)
                                }
                                put("parts", partsArray)
                            }
                            put(contentObj)
                        }
                    }
                    put("contents", contentsArray)

                    if (systemInstruction != null) {
                        val sysInstObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                val partObj = JSONObject().apply {
                                    put("text", systemInstruction)
                                }
                                put(partObj)
                            }
                            put("parts", partsArray)
                        }
                        put("systemInstruction", sysInstObj)
                    }
                }

                val requestBody = requestBodyJson.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: return@use
                        val jsonResponse = JSONObject(bodyString)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            if (content != null) {
                                val parts = content.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    return@withContext parts.getJSONObject(0).optString("text", "No text generated.")
                                }
                            }
                        }
                    } else {
                        val errBody = response.body?.string() ?: ""
                        Log.w(TAG, "Chat request failed for model $model with code: ${response.code}, trying fallback. body: $errBody")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception during chatbot response generation on model $model", e)
            }
        }

        return@withContext "I experienced a temporary connection hiccup with the free tier API. Let's stay focused on your primary objective for today!"
    }

    suspend fun generateNextAdaptiveInterviewQuestion(
        userName: String,
        selectedInterests: List<String>,
        history: List<InterviewHistoryItem>,
        questionIndex: Int
    ): AdaptiveInterviewQuestion = withContext(Dispatchers.IO) {
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generateNextAdaptiveInterviewQuestion(userName, selectedInterests, history, questionIndex)
        }

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
        try {
            val dto = AiJsonParser.decode<AdaptiveInterviewQuestionDto>(rawResponse)
            val id = dto.id?.takeIf { it.isNotBlank() } ?: "q_$questionIndex"
            val question = dto.question?.takeIf { it.isNotBlank() } ?: getDefaultQuestionText(selectedInterests, questionIndex)
            val contextTopic = dto.contextTopic?.takeIf { it.isNotBlank() } ?: getDefaultTopic(questionIndex)
            val options = dto.options.ifEmpty { getDefaultOptions(questionIndex) }

            return@withContext AdaptiveInterviewQuestion(
                id = id,
                question = question,
                contextTopic = contextTopic,
                options = options,
                allowCustomInput = true,
                isFinalQuestion = dto.isFinalQuestion,
                questionIndex = questionIndex,
                totalEstimatedQuestions = totalEstimated
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse interview question JSON via AiJsonParser", e)
            return@withContext fallbackQuestion(selectedInterests, questionIndex, isFinal, totalEstimated)
        }
    }

    suspend fun generatePersonalizedPlannerConfig(
        userName: String,
        selectedInterests: List<String>,
        history: List<InterviewHistoryItem>
    ): PersonalizedPlannerConfig = withContext(Dispatchers.IO) {
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generatePersonalizedPlannerConfig(userName, selectedInterests, history)
        }

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
        try {
            val dto = AiJsonParser.decode<PersonalizedPlannerConfigDto>(rawResponse)
            val focusSummary = dto.focusSummary?.takeIf { it.isNotBlank() }
                ?: "Personalized focus balancing ${selectedInterests.take(2).joinToString(" & ")}"
            val topPriority = dto.topPriority?.takeIf { it.isNotBlank() }
                ?: "Build momentum and daily consistency"
            val planningStyle = dto.planningStyle?.takeIf { it.isNotBlank() }
                ?: "Time Blocking & Priority Checklist"
            val scheduleConstraints = dto.scheduleConstraints?.takeIf { it.isNotBlank() }
                ?: "Standard daily routine with focused priority blocks"
            val reminderIntensity = dto.reminderIntensity.ifBlank { "Balanced" }

            val categoriesList = dto.suggestedStarterCategories.ifEmpty {
                listOf("WORK", "HEALTH", "GROWTH", "ROUTINE")
            }

            val habitsList = dto.suggestedStarterHabits.map { h ->
                SuggestedHabitItem(
                    name = h.name?.takeIf { it.isNotBlank() } ?: "Daily Focus Block",
                    targetValue = h.targetValue,
                    unit = h.unit,
                    iconName = h.iconName,
                    isSelected = h.isSelected
                )
            }.ifEmpty {
                listOf(
                    SuggestedHabitItem("Daily Focus Block", 45f, "min", "self_improvement", true),
                    SuggestedHabitItem("Daily Hydration", 2.5f, "L", "water_drop", true)
                )
            }

            val goalItem = dto.suggestedStarterGoal?.let { g ->
                SuggestedGoalItem(
                    title = g.title?.takeIf { it.isNotBlank() } ?: "Establish Core Daily Routine",
                    domain = g.domain,
                    horizon = g.horizon,
                    firstMilestoneTitle = g.firstMilestoneTitle.ifBlank { "Define Weekly Target Blocks" },
                    firstMilestoneDesc = g.firstMilestoneDesc.ifBlank { "Set up recurring focus times for high-impact priorities." },
                    isSelected = g.isSelected
                )
            }

            return@withContext PersonalizedPlannerConfig(
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
            Log.w(TAG, "Failed to parse planner config JSON via AiJsonParser", e)
            return@withContext fallbackPlannerConfig(selectedInterests, history)
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        return AiJsonParser.extractStructuredJson(raw)
    }

    private fun getDefaultTopic(index: Int): String {
        return when (index) {
            1 -> "PRIMARY TARGET"
            2 -> "DAILY SCHEDULE & CONSTRAINTS"
            else -> "PLANNING STYLE & NUDGES"
        }
    }

    private fun getDefaultQuestionText(interests: List<String>, index: Int): String {
        val primary = interests.firstOrNull() ?: "Personal Growth"
        return when (index) {
            1 -> "What is your main target or biggest objective right now regarding $primary?"
            2 -> "How do your work or study hours look during typical weekdays?"
            else -> "How do you prefer to structure and review your daily tasks?"
        }
    }

    private fun getDefaultOptions(index: Int): List<String> {
        return when (index) {
            1 -> listOf(
                "Build consistent daily momentum without burnout",
                "Execute on a major high-stakes milestone or project",
                "Establish disciplined morning & evening routines",
                "Optimize my schedule for deep focus and fewer distractions"
            )
            2 -> listOf(
                "Full-time commitments (9 AM - 5 PM) with evenings free",
                "Flexible/Freelance hours with variable daily rhythms",
                "Intensive studying with dedicated lecture & review blocks",
                "Part-time work with large dedicated focus blocks"
            )
            else -> listOf(
                "Time-blocked schedule with designated start/end slots",
                "Simple prioritized checklist (Top 3 must-win tasks)",
                "Habit & streak-focused consistency loop",
                "Hybrid: Calendar events combined with flexible focus queues"
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
            id = "q_$index",
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
        interests: List<String>,
        history: List<InterviewHistoryItem>
    ): PersonalizedPlannerConfig {
        val firstInterest = interests.firstOrNull() ?: "Productivity"
        val habits = mutableListOf<SuggestedHabitItem>()
        if (interests.any { it.contains("Fitness", true) || it.contains("Health", true) }) {
            habits.add(SuggestedHabitItem("Daily Movement / Workout", 45f, "min", "fitness_center", true))
            habits.add(SuggestedHabitItem("Hydration Goal", 3.0f, "L", "water_drop", true))
        }
        if (interests.any { it.contains("Study", true) || it.contains("Reading", true) || it.contains("Development", true) }) {
            habits.add(SuggestedHabitItem("Reading & Knowledge Log", 20f, "min", "menu_book", true))
        }
        if (habits.isEmpty()) {
            habits.add(SuggestedHabitItem("Morning Deep Work Session", 60f, "min", "self_improvement", true))
            habits.add(SuggestedHabitItem("Daily Hydration", 2.5f, "L", "water_drop", true))
        }

        return PersonalizedPlannerConfig(
            focusSummary = "Personalized focus tailored for ${interests.take(3).joinToString(", ")}",
            topPriority = "Consistent execution on core daily objectives",
            planningStyle = "Time Blocking + Prioritized Tasks",
            scheduleConstraints = "Structured around your primary work and focus hours",
            reminderIntensity = "Balanced",
            suggestedStarterCategories = listOf("WORK", "HEALTH", "GROWTH", "ADMIN"),
            suggestedStarterHabits = habits,
            suggestedStarterGoal = SuggestedGoalItem(
                title = "Achieve Mastery in $firstInterest",
                domain = "Growth",
                horizon = "Quarterly",
                firstMilestoneTitle = "Phase 1: Foundation & Habit Consistency",
                firstMilestoneDesc = "Build structured daily execution blocks and lock in the routine.",
                isSelected = true
            )
        )
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
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generateAdaptiveRebalancePlan(userName, coachPersonality, currentTasks, calendarEvents, rebalanceTrigger, availableHours, currentLocalTime)
        }

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
        try {
            val dto = AiJsonParser.decode<AdaptiveRebalanceResultDto>(raw)
            val summary = dto.summary?.takeIf { it.isNotBlank() } ?: "Schedule adjusted to accommodate $rebalanceTrigger."

            fun mapTasks(list: List<RebalanceTaskProposalDto>): List<RebalanceTaskProposal> {
                return list.map {
                    RebalanceTaskProposal(
                        taskId = it.taskId,
                        title = it.title,
                        originalTimeSlot = it.originalTimeSlot,
                        newTimeSlot = it.newTimeSlot,
                        priority = it.priority.ifBlank { "IMPORTANT" },
                        action = it.action.ifBlank { "KEEP_TODAY" },
                        reason = it.reason
                    )
                }
            }

            return@withContext AdaptiveRebalanceResult(
                summary = summary,
                triggerReason = rebalanceTrigger,
                keptTasks = mapTasks(dto.keptTasks),
                deferredTasks = mapTasks(dto.deferredTasks),
                droppedTasks = mapTasks(dto.droppedTasks),
                bufferRestoredMinutes = dto.bufferRestoredMinutes
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse rebalance JSON via AiJsonParser, using deterministic fallback", e)
            return@withContext fallbackRebalance(currentTasks, rebalanceTrigger)
        }
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

    suspend fun generateCapacityAnalysis(
        userName: String,
        plannedTasks: List<TaskEntity>,
        historicalAvgHours: Float,
        calendarMeetingsHours: Float,
        totalWorkWindowHours: Float
    ): AdaptiveCapacityReport = withContext(Dispatchers.IO) {
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generateCapacityAnalysis(userName, plannedTasks, historicalAvgHours, calendarMeetingsHours, totalWorkWindowHours)
        }

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
        try {
            val dto = AiJsonParser.decode<AdaptiveCapacityReportDto>(raw)
            val status = dto.capacityStatus?.takeIf { it.isNotBlank() }
                ?: if (totalPlannedHours > availableHours) "OVERCOMMITTED" else "OPTIMAL"
            val cap = if (dto.realisticTaskCap > 0) dto.realisticTaskCap else 4
            val insight = dto.realityInsight?.takeIf { it.isNotBlank() }
                ?: "Planned workload is balanced within your energy baseline."
            
            val obs = dto.transparentObservations.ifEmpty {
                listOf(
                    "Observed average: $historicalAvgHours h completed daily",
                    "Planned today: $totalPlannedHours h across ${plannedTasks.size} tasks"
                )
            }

            val tips = dto.actionableTips.ifEmpty {
                listOf(
                    "Protect Critical tasks first",
                    "Maintain 15-minute buffers between blocks"
                )
            }

            return@withContext AdaptiveCapacityReport(
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
            return@withContext AdaptiveCapacityReport(
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
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generateEveningReview(userName, coachPersonality, completedTasks, pendingTasks, habitsCompletedCount, totalHabitsCount, focusPointsEarned)
        }

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
        try {
            val dto = AiJsonParser.decode<EveningReviewSummaryDto>(raw)
            return@withContext EveningReviewSummary(
                suggestedScore = dto.suggestedScore?.takeIf { it.isNotBlank() } ?: "BALANCED",
                coachPraise = dto.coachPraise?.takeIf { it.isNotBlank() } ?: "Solid effort today. You moved the needle on your key objectives.",
                completionSummary = dto.completionSummary?.takeIf { it.isNotBlank() } ?: "Completed ${completedTasks.size} tasks and $habitsCompletedCount habits.",
                leftoverTriageAdvice = dto.leftoverTriageAdvice?.takeIf { it.isNotBlank() } ?: if (pendingTasks.isNotEmpty()) "Roll remaining items to tomorrow's focus blocks." else "Clean slate achieved!",
                tomorrowSetupAdvice = dto.tomorrowSetupAdvice?.takeIf { it.isNotBlank() } ?: "Get quality rest and begin tomorrow with your highest-priority focus task."
            )
        } catch (e: Exception) {
            return@withContext EveningReviewSummary(
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
        val aiManager = com.example.ai.AiManager.getInstanceOrNull()
        if (aiManager != null) {
            return@withContext aiManager.generatePersonalizedInsights(context)
        }

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
            val dtos = AiJsonParser.decode<List<PersonalizedInsightItemDto>>(response)
            val results = dtos.mapIndexed { i, dto ->
                val conf = try { ConfidenceLevel.valueOf(dto.confidence) } catch (e: Exception) { ConfidenceLevel.MODERATE_CONFIDENCE }
                PersonalizedInsightItem(
                    id = dto.id?.takeIf { it.isNotBlank() } ?: "insight_$i",
                    title = dto.title?.takeIf { it.isNotBlank() } ?: "Productivity Insight",
                    insightText = dto.insightText.orEmpty(),
                    whyExplanation = dto.whyExplanation?.takeIf { it.isNotBlank() } ?: "Derived from your planning history.",
                    category = dto.category.ifBlank { "PLANNING" },
                    confidence = conf,
                    evidencePoints = dto.evidencePoints.ifEmpty { listOf("Accuracy Score: ${context.planningAccuracyScore}/100") },
                    recommendationType = dto.recommendationType.ifBlank { "TIME_SLOT_SUGGESTION" }
                )
            }

            if (results.isNotEmpty()) {
                return@withContext results
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Failed to generate personalized insights: ${e.message}")
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

    suspend fun generateAiEnhancedBriefing(
        briefing: MorningBriefing,
        userContext: StructuredPersonalizationContext
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are a calm, highly capable executive life coach for ${userContext.userName}.
            Interpret this morning briefing and provide a 2-sentence proactive, encouraging orientation for their day.
            
            Deterministic Briefing Data:
            - Capacity Status: ${briefing.capacityStatus}
            - Main Priority: ${briefing.mainPriorityTask}
            - Recommended Focus Window: ${briefing.bestFocusWindow}
            - Potential Conflict: ${briefing.potentialIssue ?: "None"}
            - Historical Daily Capacity: ${userContext.typicalDailyCapacityHours}h
            - Planning Accuracy: ${userContext.planningAccuracyScore}%
            
            Rules:
            1. Return ONLY 2 concise sentences.
            2. Be empowering, realistic, and completely free of guilt or artificial hype.
        """.trimIndent()

        try {
            val response = generateContent(prompt)
            if (response.isNotBlank() && !response.contains("API Key is missing", ignoreCase = true)) {
                response.trim()
            } else {
                "${briefing.aiRecommendation} Protect your ${briefing.bestFocusWindow} window for ${briefing.mainPriorityTask}."
            }
        } catch (e: Exception) {
            "${briefing.aiRecommendation} Protect your ${briefing.bestFocusWindow} window for ${briefing.mainPriorityTask}."
        }
    }

    suspend fun generatePredictiveCoaching(
        recommendation: PredictiveRecommendation,
        userName: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are an executive coach for $userName.
            Provide a 1-2 sentence supportive, pragmatic explanation for this recommendation:
            Title: ${recommendation.title}
            Reason: ${recommendation.explanation}
            Suggested Action: ${recommendation.suggestedAction}
            
            Be crisp, constructive, and action-oriented. Return plain text only.
        """.trimIndent()

        try {
            val res = generateContent(prompt)
            if (res.isNotBlank() && !res.contains("API Key is missing", ignoreCase = true)) {
                res.trim()
            } else {
                "${recommendation.explanation} ${recommendation.suggestedAction}"
            }
        } catch (e: Exception) {
            "${recommendation.explanation} ${recommendation.suggestedAction}"
        }
    }
}

data class RebalanceTaskProposal(
    val taskId: Int,
    val title: String,
    val originalTimeSlot: String,
    val newTimeSlot: String,
    val priority: String,
    val action: String,
    val reason: String
)

data class AdaptiveRebalanceResult(
    val summary: String,
    val triggerReason: String,
    val keptTasks: List<RebalanceTaskProposal>,
    val deferredTasks: List<RebalanceTaskProposal>,
    val droppedTasks: List<RebalanceTaskProposal>,
    val bufferRestoredMinutes: Int
)

data class AdaptiveCapacityReport(
    val capacityStatus: String,
    val plannedHours: Float,
    val availableHours: Float,
    val realisticTaskCap: Int,
    val realityInsight: String,
    val transparentObservations: List<String>,
    val actionableTips: List<String>
)

data class EveningReviewSummary(
    val suggestedScore: String,
    val coachPraise: String,
    val completionSummary: String,
    val leftoverTriageAdvice: String,
    val tomorrowSetupAdvice: String
)
