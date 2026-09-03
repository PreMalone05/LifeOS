package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object PredictiveEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Deterministically predicts whether a given date is likely to become overloaded.
     */
    fun predictScheduleOverload(
        date: String,
        tasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        capacityModel: PersonalCapacityModel,
        accuracyReport: PlanningAccuracyReport,
        performanceRecords: List<TaskPerformanceRecordEntity>,
        userProfile: UserProfileEntity? = null
    ): ScheduleOverloadPrediction {
        val dateTasks = tasks.filter { it.date == date }
        val plannedHours = dateTasks.sumOf { it.durationHours }.toFloat()

        // Determine if date is weekend or vacation
        val cal = Calendar.getInstance().apply {
            try {
                time = dateFormat.parse(date) ?: Date()
            } catch (e: Exception) {
                time = Date()
            }
        }
        val isWeekend = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        val isVacation = userProfile?.isVacationMode == true

        val baseCapacity = when {
            isVacation -> 1.0f
            isWeekend -> capacityModel.weekendCapacityHours.coerceAtLeast(1.5f)
            else -> capacityModel.workdayCapacityHours.coerceAtLeast(3.0f)
        }

        // Calendar commitment hours
        val calCommitmentHours = calendarEvents.sumOf { (it.durationMinutes.toDouble() / 60.0) }.toFloat()
        val availableFocusCapacity = (baseCapacity - calCommitmentHours).coerceAtLeast(0.5f)

        // Duration estimation bias adjustment
        val underestimationRate = if (capacityModel.averageEstimationErrorPercent > 0) {
            1.0f + (capacityModel.averageEstimationErrorPercent / 100f).coerceIn(0f, 0.40f)
        } else 1.10f

        // Adjust each task's expected duration based on historical category averages
        var expectedTaskHours = 0f
        val underestimatedCats = accuracyReport.factors.find { it.title.contains("Duration") }?.status == "NEEDS_IMPROVEMENT"
        val factors = mutableListOf<String>()

        dateTasks.forEach { task ->
            val catRecords = performanceRecords.filter { it.category.equals(task.category, ignoreCase = true) }
            if (catRecords.size >= 3) {
                val avgActualHours = (catRecords.map { it.actualMinutes }.average() / 60.0).toFloat()
                expectedTaskHours += maxOf(task.durationHours.toFloat(), avgActualHours)
            } else {
                expectedTaskHours += task.durationHours * (if (underestimatedCats) underestimationRate else 1.05f)
            }
        }

        // Add 15m buffer per task for switching context if > 3 tasks
        if (dateTasks.size > 3) {
            expectedTaskHours += ((dateTasks.size - 3) * 0.25f)
        }

        expectedTaskHours = (expectedTaskHours * 10).roundToInt() / 10f
        val remainingBuffer = ((availableFocusCapacity - expectedTaskHours) * 10).roundToInt() / 10f

        // Rollover count
        val rolloverCount = dateTasks.count { it.isRollover || it.rescheduleCount > 0 }
        val criticalOrImportantCount = dateTasks.count { it.priority == "CRITICAL" || it.priority == "IMPORTANT" }
        val flexibleCount = dateTasks.count { it.priority == "FLEXIBLE" || it.priority == "OPTIONAL" }

        // Risk Level Evaluation
        val riskLevel = when {
            expectedTaskHours > (availableFocusCapacity * 1.25f) || remainingBuffer <= -1.0f -> OverloadRiskLevel.HIGH
            expectedTaskHours > (availableFocusCapacity * 0.95f) || remainingBuffer <= 0.2f -> OverloadRiskLevel.MODERATE
            else -> OverloadRiskLevel.LOW
        }

        // Compile contributing factors
        factors.add("Expected workload is ${expectedTaskHours}h against a typical available capacity of ${availableFocusCapacity}h.")
        if (calCommitmentHours > 0f) {
            factors.add("${String.format(Locale.getDefault(), "%.1f", calCommitmentHours)}h of calendar commitments reduce focused work time.")
        }
        if (rolloverCount > 0) {
            factors.add("$rolloverCount task(s) rolled over from previous sessions.")
        }
        if (dateTasks.any { it.energyLevel == "HIGH" }) {
            val highEnergyCount = dateTasks.count { it.energyLevel == "HIGH" }
            factors.add("$highEnergyCount high-energy task(s) require peak cognitive bandwidth.")
        }
        if (underestimatedCats && performanceRecords.isNotEmpty()) {
            factors.add("Tasks in your active categories frequently require 15-25% more time than planned.")
        }

        val suggestedAction = when (riskLevel) {
            OverloadRiskLevel.HIGH -> if (flexibleCount > 0) {
                "Move $flexibleCount FLEXIBLE task(s) to a later date to recover ~${String.format(Locale.getDefault(), "%.1f", maxOf(1.0f, abs(remainingBuffer)))}h buffer."
            } else {
                "Reschedule or shorten lower priority commitments to avoid cognitive burnout."
            }
            OverloadRiskLevel.MODERATE -> "Keep focus windows protected; consider moving 1 non-essential task if unexpected delays occur."
            OverloadRiskLevel.LOW -> "Workload is well-balanced within your verified focus capacity."
        }

        return ScheduleOverloadPrediction(
            date = date,
            riskLevel = riskLevel,
            plannedHours = (plannedHours * 10).roundToInt() / 10f,
            expectedHours = expectedTaskHours,
            typicalCapacityHours = (baseCapacity * 10).roundToInt() / 10f,
            calendarCommitmentHours = (calCommitmentHours * 10).roundToInt() / 10f,
            remainingBufferHours = remainingBuffer,
            confidence = capacityModel.confidence,
            contributingFactors = factors,
            suggestedAction = suggestedAction
        )
    }

    /**
     * Deterministically predicts whether tasks, milestones, or goals are at risk of missing deadlines.
     */
    fun predictDeadlineRisk(
        todayDate: String,
        tasks: List<TaskEntity>,
        goals: List<GoalEntity>,
        milestones: List<MilestoneEntity>,
        capacityModel: PersonalCapacityModel,
        performanceRecords: List<TaskPerformanceRecordEntity>
    ): List<DeadlineRiskPrediction> {
        val predictions = mutableListOf<DeadlineRiskPrediction>()
        val todayCal = Calendar.getInstance().apply {
            try {
                time = dateFormat.parse(todayDate) ?: Date()
            } catch (e: Exception) {
                time = Date()
            }
        }

        // 1. Evaluate Milestones with Due Dates
        milestones.filter { it.status != "COMPLETED" && !it.dueDate.isNullOrBlank() }.forEach { milestone ->
            val dueCal = Calendar.getInstance().apply {
                try {
                    time = dateFormat.parse(milestone.dueDate!!) ?: Date()
                } catch (e: Exception) {
                    time = Date()
                }
            }

            val diffMillis = dueCal.timeInMillis - todayCal.timeInMillis
            val daysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

            // Estimate remaining work hours (approx 3h per milestone default if no subtasks)
            val estimatedHours = 3.0f
            val dailyCapacity = capacityModel.averageFocusCapacityHours.coerceAtLeast(2.0f)
            val totalFutureCapacity = (daysRemaining.coerceAtLeast(0) * dailyCapacity * 0.6f) // 60% dedicated to this milestone

            val (riskLevel, explanation) = when {
                daysRemaining < 0 -> Pair(DeadlineRiskLevel.OVERDUE, "Due date passed ${abs(daysRemaining)} day(s) ago.")
                daysRemaining == 0 -> Pair(DeadlineRiskLevel.HIGH_RISK, "Due today with ~${estimatedHours}h of remaining focus work.")
                daysRemaining in 1..2 && estimatedHours > totalFutureCapacity -> Pair(
                    DeadlineRiskLevel.HIGH_RISK,
                    "Due in $daysRemaining day(s). Requires ~${estimatedHours}h against ~${String.format(Locale.getDefault(), "%.1f", totalFutureCapacity)}h available focus capacity."
                )
                daysRemaining in 1..4 -> Pair(
                    DeadlineRiskLevel.AT_RISK,
                    "Due in $daysRemaining days. Plan dedicated focus blocks to ensure timely completion."
                )
                else -> Pair(
                    DeadlineRiskLevel.ON_TRACK,
                    "Due in $daysRemaining days with adequate capacity buffer."
                )
            }

            predictions.add(
                DeadlineRiskPrediction(
                    targetId = milestone.id,
                    title = milestone.title,
                    targetType = "MILESTONE",
                    dueDate = milestone.dueDate ?: todayDate,
                    riskLevel = riskLevel,
                    daysRemaining = daysRemaining,
                    estimatedHoursRemaining = estimatedHours,
                    availableCapacityBeforeDeadline = totalFutureCapacity,
                    confidence = capacityModel.confidence,
                    explanation = explanation
                )
            )
        }

        // 2. Evaluate Tasks due today that are incomplete
        tasks.filter { !it.isCompleted && it.date == todayDate && (it.priority == "CRITICAL" || it.priority == "IMPORTANT") }.forEach { task ->
            val isRolledOver = task.isRollover || task.rescheduleCount > 0
            val riskLevel = if (isRolledOver) DeadlineRiskLevel.HIGH_RISK else DeadlineRiskLevel.AT_RISK
            val explanation = if (isRolledOver) {
                "High priority task already rolled over ${task.rescheduleCount} time(s). Requires immediate scheduling."
            } else {
                "Priority ${task.priority} item scheduled for today (${task.durationHours}h estimated)."
            }

            predictions.add(
                DeadlineRiskPrediction(
                    targetId = task.id,
                    title = task.title,
                    targetType = "TASK",
                    dueDate = todayDate,
                    riskLevel = riskLevel,
                    daysRemaining = 0,
                    estimatedHoursRemaining = task.durationHours.toFloat(),
                    availableCapacityBeforeDeadline = capacityModel.averageFocusCapacityHours,
                    confidence = capacityModel.confidence,
                    explanation = explanation
                )
            )
        }

        return predictions.sortedByDescending { it.riskLevel.severityRank }
    }

    /**
     * Calculates deterministic probability (0..100) of completing a specific task in its planned window.
     */
    fun predictTaskCompletionProbability(
        task: TaskEntity,
        records: List<TaskPerformanceRecordEntity>,
        patterns: ProductivityPatternsReport,
        capacityModel: PersonalCapacityModel,
        dayTasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>
    ): TaskCompletionProbability {
        val categoryRecords = records.filter { it.category.equals(task.category, ignoreCase = true) }
        val sampleSize = categoryRecords.size
        val confidence = ConfidenceLevel.fromCount(sampleSize)

        if (sampleSize < 2 && records.size < 4) {
            return TaskCompletionProbability(
                taskId = task.id,
                taskTitle = task.title,
                probabilityPercent = when (task.priority) {
                    "CRITICAL" -> 88
                    "IMPORTANT" -> 78
                    "FLEXIBLE" -> 65
                    else -> 50
                },
                confidence = ConfidenceLevel.INSUFFICIENT_DATA,
                isSufficientData = false,
                supportingFactors = listOf("Calibrating model. Baseline probability assigned based on ${task.priority} priority tier.")
            )
        }

        val factors = mutableListOf<String>()

        // 1. Base rate by priority
        var probability = when (task.priority.uppercase()) {
            "CRITICAL" -> patterns.priorityCompletionRates["CRITICAL"] ?: 92
            "IMPORTANT" -> patterns.priorityCompletionRates["IMPORTANT"] ?: 80
            "FLEXIBLE" -> patterns.priorityCompletionRates["FLEXIBLE"] ?: 68
            else -> patterns.priorityCompletionRates["OPTIONAL"] ?: 50
        }
        factors.add("Base completion rate for ${task.priority} tasks is $probability%.")

        // 2. Category historical adjustment
        if (patterns.frequentlyPostponedCategories.contains(task.category.uppercase())) {
            probability -= 12
            factors.add("Category ${task.category.uppercase()} is frequently postponed (-12%).")
        }

        // 3. Underestimation adjustment
        if (patterns.frequentlyUnderestimatedCategories.contains(task.category.uppercase())) {
            probability -= 8
            factors.add("${task.category.uppercase()} tasks frequently take longer than planned (-8%).")
        }

        // 4. Energy alignment
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        if (task.energyLevel == "HIGH") {
            if (currentHour in 8..12) {
                probability += 8
                factors.add("Morning focus aligns with high-energy requirement (+8%).")
            } else if (currentHour in 14..17) {
                probability -= 10
                factors.add("High-energy task scheduled in historical afternoon dip (-10%).")
            }
        }

        // 5. Day overload impact
        val totalPlannedHours = dayTasks.sumOf { it.durationHours }
        val availableCap = capacityModel.workdayCapacityHours
        if (totalPlannedHours > availableCap * 1.2f) {
            probability -= 12
            factors.add("Day workload (${totalPlannedHours}h) exceeds normal capacity of ${availableCap}h (-12%).")
        }

        // 6. Rollover penalty
        if (task.isRollover || task.rescheduleCount > 0) {
            probability -= 10
            factors.add("Task has been postponed ${task.rescheduleCount} time(s) previously (-10%).")
        }

        val finalProbability = probability.coerceIn(15, 98)

        return TaskCompletionProbability(
            taskId = task.id,
            taskTitle = task.title,
            probabilityPercent = finalProbability,
            confidence = confidence,
            isSufficientData = true,
            supportingFactors = factors,
            recommendedWindow = patterns.mostProductiveHours
        )
    }

    /**
     * Extends habit intelligence to predict risk of missing a habit today without guilt-based language.
     */
    fun predictHabitRisk(
        habits: List<HabitEntity>,
        calendarEvents: List<CalendarEvent>,
        events: List<BehavioralEventEntity>,
        patterns: ProductivityPatternsReport
    ): List<HabitRiskPrediction> {
        val predictions = mutableListOf<HabitRiskPrediction>()
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)

        habits.filter { !it.isCompleted }.forEach { habit ->
            // Determine usual habit time window
            val isMorningHabit = habit.name.contains("Morning", ignoreCase = true) ||
                    habit.name.contains("Water", ignoreCase = true) ||
                    habit.name.contains("Meditate", ignoreCase = true) ||
                    habit.iconName == "water_drop" || habit.iconName == "self_improvement"

            val isEveningHabit = habit.name.contains("Read", ignoreCase = true) ||
                    habit.name.contains("Journal", ignoreCase = true) ||
                    habit.iconName == "menu_book"

            val calendarHeavyMorning = calendarEvents.any { it.startHour in 8..11 }
            val calendarHeavyEvening = calendarEvents.any { it.startHour in 18..21 }

            val (riskLevel, explanation, conflict) = when {
                isMorningHabit && calendarHeavyMorning && currentHour >= 11 -> {
                    Triple(
                        HabitRiskLevel.HIGH,
                        "Higher-than-usual chance of being missed because your morning focus window overlapped with consecutive meetings.",
                        "Morning calendar density"
                    )
                }
                isMorningHabit && currentHour in 6..10 && calendarHeavyMorning -> {
                    Triple(
                        HabitRiskLevel.MODERATE,
                        "Morning commitments are dense today. Completing this habit in the next 30 minutes prevents end-of-day rush.",
                        "Upcoming morning meetings"
                    )
                }
                isEveningHabit && calendarHeavyEvening -> {
                    Triple(
                        HabitRiskLevel.MODERATE,
                        "Evening schedule contains obligations. An earlier completion window is recommended.",
                        "Evening commitments"
                    )
                }
                currentHour >= 20 && !habit.isCompleted -> {
                    Triple(
                        HabitRiskLevel.HIGH,
                        "Late in the day with unlogged habit progress. A quick 5-minute session keeps your ${habit.streak}-day streak alive.",
                        "End of day approaching"
                    )
                }
                else -> {
                    Triple(
                        HabitRiskLevel.LOW,
                        "Normal completion trajectory on schedule.",
                        null
                    )
                }
            }

            predictions.add(
                HabitRiskPrediction(
                    habitId = habit.id,
                    habitName = habit.name,
                    riskLevel = riskLevel,
                    confidence = ConfidenceLevel.MODERATE_CONFIDENCE,
                    explanation = explanation,
                    conflictingCommitment = conflict
                )
            )
        }

        return predictions.sortedByDescending { it.riskLevel.severityRank }
    }

    /**
     * Detects optimal predictive focus windows based on historical productivity, calendar commitments, and uncompleted tasks.
     */
    fun detectPredictiveFocusWindows(
        tasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        patterns: ProductivityPatternsReport,
        capacityModel: PersonalCapacityModel
    ): List<PredictiveFocusWindow> {
        val windows = mutableListOf<PredictiveFocusWindow>()
        val pendingTasks = tasks.filter { !it.isCompleted }.sortedByDescending {
            when (it.priority) {
                "CRITICAL" -> 4
                "IMPORTANT" -> 3
                "FLEXIBLE" -> 2
                else -> 1
            }
        }

        // Standard work slots to evaluate: 09:00-10:30, 11:00-12:30, 14:00-15:30, 16:00-17:30
        val candidateSlots = listOf(
            Triple("09:00", "10:30", 90),
            Triple("11:00", "12:15", 75),
            Triple("14:00", "15:15", 75),
            Triple("16:00", "17:00", 60)
        )

        candidateSlots.forEachIndexed { index, (start, end, duration) ->
            val startHour = start.substringBefore(":").toInt()
            val endHour = end.substringBefore(":").toInt()

            // Check if there is any calendar meeting overlap
            val hasConflict = calendarEvents.any { event ->
                val evStart = event.startHour
                val evEnd = evStart + (event.durationMinutes / 60)
                (evStart in startHour until endHour) || (evEnd in (startHour + 1)..endHour)
            }

            if (!hasConflict) {
                val matchedTask = pendingTasks.getOrNull(index) ?: pendingTasks.firstOrNull()
                val isMorning = startHour < 12
                val energy = if (isMorning) "HIGH" else "MEDIUM"
                val reason = if (isMorning) {
                    "Strong historical peak performance period ($start - $end) with uninterrupted focus."
                } else {
                    "Dedicated $duration-minute afternoon window suited for structured task execution."
                }

                windows.add(
                    PredictiveFocusWindow(
                        startTime = start,
                        endTime = end,
                        durationMinutes = duration,
                        recommendedTaskId = matchedTask?.id,
                        recommendedTaskTitle = matchedTask?.title ?: "Dedicated Focus Block",
                        energyLevel = energy,
                        reason = reason,
                        confidence = patterns.confidence
                    )
                )
            }
        }

        return windows
    }

    /**
     * Deterministically answers "What Should I Do Now?" with ONE highly targeted action.
     */
    fun calculateWhatShouldIDoNow(
        currentTime: Calendar,
        todayTasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        patterns: ProductivityPatternsReport,
        performanceRecords: List<TaskPerformanceRecordEntity>
    ): WhatShouldIDoNowResult {
        val pendingTasks = todayTasks.filter { !it.isCompleted }
        if (pendingTasks.isEmpty()) {
            return WhatShouldIDoNowResult(
                recommendedTaskId = null,
                recommendedTaskTitle = "All Daily Tasks Completed",
                priority = "OPTIMAL",
                durationMinutes = 0,
                energyLevel = "LOW",
                focusWindowAvailableMinutes = 60,
                reason = "You have finished all planned tasks for today. Take time to recharge or review tomorrow's plan.",
                isActionable = false
            )
        }

        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val currentTotalMinutes = currentHour * 60 + currentMinute

        // Find time until next calendar event
        val upcomingEvents = calendarEvents.filter { (it.startHour * 60) > currentTotalMinutes }
            .sortedBy { it.startHour }

        val nextEvent = upcomingEvents.firstOrNull()
        val availableMinutes = if (nextEvent != null) {
            ((nextEvent.startHour * 60) - currentTotalMinutes).coerceAtLeast(15)
        } else {
            90 // Open window
        }

        // Score each pending task
        data class ScoredTask(val task: TaskEntity, val score: Int, val reason: String)
        val scoredTasks = pendingTasks.map { task ->
            var score = 0
            val reasons = mutableListOf<String>()

            // 1. Priority scoring (40% weight)
            when (task.priority.uppercase()) {
                "CRITICAL" -> { score += 50; reasons.add("CRITICAL priority") }
                "IMPORTANT" -> { score += 35; reasons.add("IMPORTANT priority") }
                "FLEXIBLE" -> { score += 15; reasons.add("FLEXIBLE priority") }
                else -> { score += 5 }
            }

            // 2. Duration / Window fit (30% weight)
            val taskMinutes = task.durationHours * 60
            if (taskMinutes <= availableMinutes) {
                score += 30
                reasons.add("fits comfortably inside your ${availableMinutes}m open window")
            } else if (taskMinutes <= availableMinutes + 15) {
                score += 15
                reasons.add("estimated ${taskMinutes}m matches available time")
            } else {
                score -= 10
            }

            // 3. Energy fit (20% weight)
            if (currentHour in 8..12 && task.energyLevel == "HIGH") {
                score += 20
                reasons.add("matches your peak morning cognitive energy")
            } else if (currentHour in 13..17 && task.energyLevel == "MEDIUM") {
                score += 15
                reasons.add("suits current afternoon rhythm")
            }

            // 4. Rollover bonus (10% weight)
            if (task.isRollover || task.rescheduleCount > 0) {
                score += 15
                reasons.add("avoids secondary postponement")
            }

            val reasonSummary = "${task.title} is ${reasons.joinToString(", ")}."
            ScoredTask(task, score, reasonSummary)
        }.sortedByDescending { it.score }

        val best = scoredTasks.first()
        val secondary = scoredTasks.getOrNull(1)

        val taskEstMinutes = best.task.durationHours * 60
        val detailedReason = if (nextEvent != null) {
            "It is ${best.task.priority}, estimated at $taskEstMinutes minutes, and you have a $availableMinutes-minute focus window before ${nextEvent.title}."
        } else {
            "It is ${best.task.priority}, estimated at $taskEstMinutes minutes, matching your available open block."
        }

        return WhatShouldIDoNowResult(
            recommendedTaskId = best.task.id,
            recommendedTaskTitle = best.task.title,
            priority = best.task.priority,
            durationMinutes = taskEstMinutes,
            energyLevel = best.task.energyLevel,
            focusWindowAvailableMinutes = availableMinutes,
            reason = detailedReason,
            secondaryOptionTaskId = secondary?.task?.id,
            secondaryOptionTitle = secondary?.task?.title,
            isActionable = true
        )
    }

    /**
     * Midday Plan Divergence Detection: Detects when reality significantly diverges from the original plan.
     */
    fun detectPlanDivergence(
        currentTime: Calendar,
        todayTasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        events: List<BehavioralEventEntity>
    ): PlanDivergenceReport {
        val totalPlanned = todayTasks.size
        if (totalPlanned == 0) {
            return PlanDivergenceReport(
                isDiverged = false,
                divergenceScore = 0,
                originalPlannedCount = 0,
                completedCount = 0,
                missedOrDelayedCount = 0,
                unexpectedCalendarMinutes = 0,
                reasons = emptyList(),
                suggestedAction = "Plan is fully aligned."
            )
        }

        val completed = todayTasks.count { it.isCompleted }
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val reasons = mutableListOf<String>()

        // 1. Check tasks that were scheduled before current hour but are still pending
        var missedCount = 0
        todayTasks.filter { !it.isCompleted }.forEach { task ->
            val slotHour = extractStartHour(task.timeSlot)
            if (slotHour in 1..currentHour) {
                missedCount++
            }
        }

        if (missedCount >= 2) {
            reasons.add("$missedCount scheduled task(s) have passed their planned start time without completion.")
        }

        // 2. Check postponements logged today
        val postponementsToday = events.count {
            (it.eventType == BehavioralEventType.TASK_POSTPONED || it.eventType == BehavioralEventType.TASK_ROLLED_OVER)
        }
        if (postponementsToday >= 2) {
            reasons.add("$postponementsToday task(s) were deferred or rescheduled today.")
        }

        // 3. Check unexpected calendar commitments
        val totalCalendarMinutes = calendarEvents.sumOf { it.durationMinutes }
        if (totalCalendarMinutes >= 120 && completed < (totalPlanned / 2) && currentHour >= 14) {
            reasons.add("${totalCalendarMinutes}m of calendar commitments consumed available focus blocks.")
        }

        val divergenceScore = ((missedCount * 25) + (postponementsToday * 20) + (if (totalCalendarMinutes >= 120) 25 else 0)).coerceIn(0, 100)
        val isDiverged = divergenceScore >= 45 && currentHour >= 12

        val suggestedAction = if (isDiverged) {
            "Run Dynamic Rebalancing to redistribute your remaining uncompleted tasks realistically."
        } else {
            "Continue with current schedule."
        }

        return PlanDivergenceReport(
            isDiverged = isDiverged,
            divergenceScore = divergenceScore,
            originalPlannedCount = totalPlanned,
            completedCount = completed,
            missedOrDelayedCount = missedCount,
            unexpectedCalendarMinutes = totalCalendarMinutes,
            reasons = reasons,
            suggestedAction = suggestedAction
        )
    }

    /**
     * Deterministically generates Tomorrow Preview report for evening workflows.
     */
    fun generateTomorrowPreview(
        tomorrowDate: String,
        tasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        capacityModel: PersonalCapacityModel,
        accuracyReport: PlanningAccuracyReport,
        performanceRecords: List<TaskPerformanceRecordEntity>,
        userProfile: UserProfileEntity? = null
    ): TomorrowPreviewReport {
        val overloadPrediction = predictScheduleOverload(
            date = tomorrowDate,
            tasks = tasks,
            calendarEvents = calendarEvents,
            capacityModel = capacityModel,
            accuracyReport = accuracyReport,
            performanceRecords = performanceRecords,
            userProfile = userProfile
        )

        val tomorrowTasks = tasks.filter { it.date == tomorrowDate }
        val conflicts = mutableListOf<String>()
        val tasksToPostpone = mutableListOf<String>()

        if (overloadPrediction.riskLevel == OverloadRiskLevel.HIGH) {
            conflicts.add("Workload (${overloadPrediction.expectedHours}h) exceeds safe focus capacity (${overloadPrediction.typicalCapacityHours}h).")
            tomorrowTasks.filter { it.priority == "FLEXIBLE" || it.priority == "OPTIONAL" }.forEach {
                tasksToPostpone.add(it.title)
            }
        }

        if (overloadPrediction.calendarCommitmentHours >= 2.0f) {
            conflicts.add("${String.format(Locale.getDefault(), "%.1f", overloadPrediction.calendarCommitmentHours)}h calendar commitments during active hours.")
        }

        val deadlines = tomorrowTasks.filter { it.priority == "CRITICAL" }.map { it.title }

        return TomorrowPreviewReport(
            date = tomorrowDate,
            expectedCapacityHours = overloadPrediction.typicalCapacityHours,
            plannedWorkloadHours = overloadPrediction.plannedHours,
            calendarLoadHours = overloadPrediction.calendarCommitmentHours,
            overloadRisk = overloadPrediction.riskLevel,
            importantDeadlines = deadlines,
            recommendedFocusPeriod = "Morning (09:00 - 11:30 AM)",
            potentialConflicts = conflicts,
            tasksToPostpone = tasksToPostpone
        )
    }

    /**
     * Generates a deterministic Morning AI Briefing.
     */
    fun generateMorningBriefing(
        todayDate: String,
        todayTasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        capacityModel: PersonalCapacityModel,
        accuracyReport: PlanningAccuracyReport,
        performanceRecords: List<TaskPerformanceRecordEntity>
    ): MorningBriefing {
        val overload = predictScheduleOverload(
            date = todayDate,
            tasks = todayTasks,
            calendarEvents = calendarEvents,
            capacityModel = capacityModel,
            accuracyReport = accuracyReport,
            performanceRecords = performanceRecords
        )

        val capacityStatus = when (overload.riskLevel) {
            OverloadRiskLevel.LOW -> "OPTIMAL"
            OverloadRiskLevel.MODERATE -> "MODERATE LOAD"
            OverloadRiskLevel.HIGH -> "HIGH LOAD"
        }

        val topPriority = todayTasks.filter { !it.isCompleted }
            .sortedByDescending { if (it.priority == "CRITICAL") 3 else if (it.priority == "IMPORTANT") 2 else 1 }
            .firstOrNull()?.title ?: "Plan Daily Objectives"

        val bestWindow = "09:00 - 10:30 AM"

        val potentialIssue = if (overload.riskLevel == OverloadRiskLevel.HIGH) {
            "Your afternoon has heavy calendar density and high workload."
        } else if (overload.calendarCommitmentHours >= 2.0f) {
            "Meetings consume ~${String.format(Locale.getDefault(), "%.1f", overload.calendarCommitmentHours)}h of focus time today."
        } else null

        val aiRec = when (overload.riskLevel) {
            OverloadRiskLevel.HIGH -> "Complete your critical task ($topPriority) before lunch to avoid afternoon overflow."
            OverloadRiskLevel.MODERATE -> "Focus on executing $topPriority during your 09:00 morning block."
            OverloadRiskLevel.LOW -> "Your schedule is well-spaced. Great day for deep, uninterrupted progress."
        }

        return MorningBriefing(
            date = todayDate,
            capacityStatus = capacityStatus,
            mainPriorityTask = topPriority,
            bestFocusWindow = bestWindow,
            potentialIssue = potentialIssue,
            aiRecommendation = aiRec
        )
    }

    /**
     * Synthesizes and ranks deterministic recommendations for the user.
     * Enforces strict display limits (max 2-3 top alerts) to prevent notification/UI fatigue.
     */
    fun generateRankedRecommendations(
        todayDate: String,
        tomorrowDate: String,
        tasks: List<TaskEntity>,
        calendarEvents: List<CalendarEvent>,
        habits: List<HabitEntity>,
        goals: List<GoalEntity>,
        milestones: List<MilestoneEntity>,
        capacityModel: PersonalCapacityModel,
        accuracyReport: PlanningAccuracyReport,
        performanceRecords: List<TaskPerformanceRecordEntity>,
        events: List<BehavioralEventEntity>,
        feedbackList: List<RecommendationFeedbackEntity>
    ): List<PredictiveRecommendation> {
        val recommendations = mutableListOf<PredictiveRecommendation>()
        val blockedTypes = feedbackList.filter { it.feedback == "DONT_SUGGEST_AGAIN" }.map { it.recommendationType }.toSet()

        // 1. Check Today / Tomorrow Overload
        val todayOverload = predictScheduleOverload(todayDate, tasks, calendarEvents, capacityModel, accuracyReport, performanceRecords)
        if (todayOverload.riskLevel == OverloadRiskLevel.HIGH && !blockedTypes.contains("CAPACITY_WARNING")) {
            recommendations.add(
                PredictiveRecommendation(
                    id = "rec_overload_today_$todayDate",
                    type = RecommendationType.CAPACITY_WARNING,
                    priority = "CRITICAL",
                    confidence = todayOverload.confidence,
                    title = "Today is Overloaded",
                    explanation = "Expected workload is ${todayOverload.expectedHours}h against a capacity of ${todayOverload.typicalCapacityHours}h.",
                    suggestedAction = "Rebalance your day to protect important milestones.",
                    actionType = "REBALANCE"
                )
            )
        }

        val tomorrowOverload = predictScheduleOverload(tomorrowDate, tasks, emptyList(), capacityModel, accuracyReport, performanceRecords)
        if (tomorrowOverload.riskLevel == OverloadRiskLevel.HIGH && !blockedTypes.contains("CAPACITY_WARNING")) {
            recommendations.add(
                PredictiveRecommendation(
                    id = "rec_overload_tomorrow_$tomorrowDate",
                    type = RecommendationType.CAPACITY_WARNING,
                    priority = "IMPORTANT",
                    confidence = tomorrowOverload.confidence,
                    title = "Tomorrow Exceeds Capacity",
                    explanation = "Planned load (${tomorrowOverload.plannedHours}h) exceeds your typical threshold (${tomorrowOverload.typicalCapacityHours}h).",
                    suggestedAction = "Move flexible items ahead of time.",
                    actionType = "REBALANCE"
                )
            )
        }

        // 2. Check Deadline Risks
        val deadlineRisks = predictDeadlineRisk(todayDate, tasks, goals, milestones, capacityModel, performanceRecords)
        deadlineRisks.filter { it.riskLevel == DeadlineRiskLevel.HIGH_RISK || it.riskLevel == DeadlineRiskLevel.OVERDUE }
            .take(1)
            .forEach { risk ->
                if (!blockedTypes.contains("DEADLINE_WARNING")) {
                    recommendations.add(
                        PredictiveRecommendation(
                            id = "rec_deadline_${risk.targetType}_${risk.targetId}",
                            type = RecommendationType.DEADLINE_WARNING,
                            priority = "CRITICAL",
                            confidence = risk.confidence,
                            title = "${risk.title} is ${risk.riskLevel.displayName}",
                            explanation = risk.explanation,
                            suggestedAction = "Prioritize this item in your next focus window.",
                            actionType = "START_TASK",
                            relatedTaskId = if (risk.targetType == "TASK") risk.targetId else null
                        )
                    )
                }
            }

        // 3. Check Habit Risks
        val habitRisks = predictHabitRisk(habits, calendarEvents, events, ProductivityPatternsReport(
            mostProductiveHours = "09:00", leastProductiveHours = "15:00", bestFocusPeriod = "Morning", strongestDayOfWeek = "Tuesday",
            weekdayCompletionRate = 80, weekendCompletionRate = 70, frequentlyPostponedCategories = emptyList(), frequentlyUnderestimatedCategories = emptyList(),
            priorityCompletionRates = emptyMap(), energyCompletionRates = emptyMap(), overplanningTendencyPercent = 0, confidence = ConfidenceLevel.MODERATE_CONFIDENCE, totalEventsAnalyzed = 5
        ))
        habitRisks.filter { it.riskLevel == HabitRiskLevel.HIGH }.take(1).forEach { habitRisk ->
            if (!blockedTypes.contains("HABIT_RISK")) {
                recommendations.add(
                    PredictiveRecommendation(
                        id = "rec_habit_${habitRisk.habitId}_$todayDate",
                        type = RecommendationType.HABIT_RISK,
                        priority = "FLEXIBLE",
                        confidence = habitRisk.confidence,
                        title = "${habitRisk.habitName} At Risk",
                        explanation = habitRisk.explanation,
                        suggestedAction = "Log a quick check-in before evening.",
                        actionType = "VIEW_HABIT"
                    )
                )
            }
        }

        // 4. Plan Divergence
        val divergence = detectPlanDivergence(Calendar.getInstance(), tasks.filter { it.date == todayDate }, calendarEvents, events)
        if (divergence.isDiverged && !blockedTypes.contains("PLAN_DIVERGENCE")) {
            recommendations.add(
                PredictiveRecommendation(
                    id = "rec_divergence_$todayDate",
                    type = RecommendationType.PLAN_DIVERGENCE,
                    priority = "CRITICAL",
                    confidence = ConfidenceLevel.HIGH_CONFIDENCE,
                    title = "Your Day Has Changed",
                    explanation = divergence.reasons.firstOrNull() ?: "Schedule has diverged significantly from the original plan.",
                    suggestedAction = "Rebalance remaining tasks for today.",
                    actionType = "REBALANCE"
                )
            )
        }

        return rankAndDeduplicateRecommendations(recommendations)
    }

    /**
     * Deterministically ranks and deduplicates recommendations, keeping only the top 2-3 most critical items.
     */
    fun rankAndDeduplicateRecommendations(recommendations: List<PredictiveRecommendation>): List<PredictiveRecommendation> {
        val unique = recommendations.distinctBy { it.id }

        return unique.sortedWith(
            compareByDescending<PredictiveRecommendation> {
                when (it.priority) {
                    "CRITICAL" -> 4
                    "IMPORTANT" -> 3
                    "FLEXIBLE" -> 2
                    else -> 1
                }
            }.thenByDescending {
                when (it.confidence) {
                    ConfidenceLevel.HIGH_CONFIDENCE -> 3
                    ConfidenceLevel.MODERATE_CONFIDENCE -> 2
                    ConfidenceLevel.LOW_CONFIDENCE -> 1
                    ConfidenceLevel.INSUFFICIENT_DATA -> 0
                }
            }
        ).take(3)
    }

    private fun extractStartHour(timeSlot: String): Int {
        if (timeSlot.isBlank()) return 9
        return try {
            val startPart = timeSlot.substringBefore("-").trim()
            val hourStr = startPart.substringBefore(":").trim()
            val isPM = startPart.contains("PM", ignoreCase = true) || timeSlot.contains("PM", ignoreCase = true)
            var h = hourStr.toInt()
            if (isPM && h < 12) h += 12
            if (!isPM && h == 12) h = 0
            h
        } catch (e: Exception) {
            9
        }
    }
}
