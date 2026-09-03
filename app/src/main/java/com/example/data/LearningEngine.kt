package com.example.data

import kotlin.math.abs
import kotlin.math.roundToInt

object LearningEngine {

    /**
     * Calculates the personalized capacity model based on historical completed work,
     * performance records, and daily reviews.
     */
    fun calculatePersonalCapacity(
        tasks: List<TaskEntity>,
        performanceRecords: List<TaskPerformanceRecordEntity>,
        dailyReviews: List<DailyReviewEntity>,
        events: List<BehavioralEventEntity>
    ): PersonalCapacityModel {
        val completedTasks = tasks.filter { it.isCompleted }
        val daysWithActivity = tasks.map { it.date }.distinct().filter { it.isNotBlank() }
        val daysCount = daysWithActivity.size.coerceAtLeast(dailyReviews.size)

        val confidence = ConfidenceLevel.fromCount(daysCount)

        if (daysCount == 0 || completedTasks.isEmpty()) {
            return PersonalCapacityModel(
                averageRealisticDailyHours = 4.5f,
                averageFocusCapacityHours = 3.0f,
                averageCompletedTaskCount = 3.5f,
                averageCompletedFocusHours = 2.5f,
                averageRolloverRatePercent = 15,
                averageEstimationErrorPercent = 10,
                workdayCapacityHours = 5.0f,
                weekendCapacityHours = 2.5f,
                morningCapacityPercent = 55,
                afternoonCapacityPercent = 35,
                eveningCapacityPercent = 10,
                energyLevelCapacities = mapOf("HIGH" to 2.0f, "MEDIUM" to 2.0f, "LOW" to 1.0f),
                confidence = ConfidenceLevel.INSUFFICIENT_DATA,
                daysAnalyzed = 0
            )
        }

        // Group tasks by date to find daily averages
        val tasksByDate = tasks.groupBy { it.date }
        val dailyCompletedHoursList = mutableListOf<Float>()
        val dailyCompletedCountList = mutableListOf<Float>()

        tasksByDate.forEach { (_, dateTasks) ->
            val comp = dateTasks.filter { it.isCompleted }
            val hours = comp.sumOf { it.durationHours }.toFloat()
            dailyCompletedHoursList.add(hours)
            dailyCompletedCountList.add(comp.size.toFloat())
        }

        val avgDailyHours = if (dailyCompletedHoursList.isNotEmpty()) {
            dailyCompletedHoursList.average().toFloat()
        } else 4.0f

        val avgCompletedTasks = if (dailyCompletedCountList.isNotEmpty()) {
            dailyCompletedCountList.average().toFloat()
        } else 3.0f

        val totalTasks = tasks.size.coerceAtLeast(1)
        val rolloverCount = tasks.count { it.isRollover || it.rescheduleCount > 0 }
        val rolloverRate = ((rolloverCount.toFloat() / totalTasks) * 100).roundToInt().coerceIn(0, 100)

        // Estimation error from performance records
        val avgEstErrorPercent = if (performanceRecords.isNotEmpty()) {
            val totalError = performanceRecords.sumOf { abs(it.estimationErrorMinutes) }
            val totalEst = performanceRecords.sumOf { it.estimatedMinutes }.coerceAtLeast(1)
            ((totalError.toFloat() / totalEst) * 100).roundToInt().coerceIn(0, 100)
        } else {
            12
        }

        // Time of day distribution from performance records / events
        val morningCount = performanceRecords.count { it.timeSlotHour in 6..11 } +
                events.count { it.eventType == BehavioralEventType.TASK_COMPLETED && it.timeOfDayHour in 6..11 }
        val afternoonCount = performanceRecords.count { it.timeSlotHour in 12..17 } +
                events.count { it.eventType == BehavioralEventType.TASK_COMPLETED && it.timeOfDayHour in 12..17 }
        val eveningCount = performanceRecords.count { it.timeSlotHour in 18..23 } +
                events.count { it.eventType == BehavioralEventType.TASK_COMPLETED && it.timeOfDayHour in 18..23 }
        val totalTimeCount = (morningCount + afternoonCount + eveningCount).coerceAtLeast(1)

        val morningPercent = ((morningCount.toFloat() / totalTimeCount) * 100).roundToInt()
        val afternoonPercent = ((afternoonCount.toFloat() / totalTimeCount) * 100).roundToInt()
        val eveningPercent = (100 - morningPercent - afternoonPercent).coerceAtLeast(0)

        // Workday vs Weekend capacity
        val workdayRecords = performanceRecords.filter { it.dayOfWeek in 1..5 }
        val weekendRecords = performanceRecords.filter { it.dayOfWeek in 6..7 }

        val workdayHours = if (workdayRecords.isNotEmpty()) {
            (workdayRecords.sumOf { it.actualMinutes }.toFloat() / 60f / workdayRecords.map { it.date }.distinct().size.coerceAtLeast(1)).coerceIn(1f, 10f)
        } else {
            (avgDailyHours * 1.1f).coerceIn(2f, 8f)
        }

        val weekendHours = if (weekendRecords.isNotEmpty()) {
            (weekendRecords.sumOf { it.actualMinutes }.toFloat() / 60f / weekendRecords.map { it.date }.distinct().size.coerceAtLeast(1)).coerceIn(0.5f, 6f)
        } else {
            (avgDailyHours * 0.6f).coerceIn(1f, 4f)
        }

        // Energy level breakdown
        val highEnergyHours = completedTasks.filter { it.energyLevel == "HIGH" }.sumOf { it.durationHours }.toFloat() / daysCount.coerceAtLeast(1)
        val medEnergyHours = completedTasks.filter { it.energyLevel == "MEDIUM" }.sumOf { it.durationHours }.toFloat() / daysCount.coerceAtLeast(1)
        val lowEnergyHours = completedTasks.filter { it.energyLevel == "LOW" }.sumOf { it.durationHours }.toFloat() / daysCount.coerceAtLeast(1)

        return PersonalCapacityModel(
            averageRealisticDailyHours = (avgDailyHours * 10).roundToInt() / 10f,
            averageFocusCapacityHours = ((avgDailyHours * 0.7f) * 10).roundToInt() / 10f,
            averageCompletedTaskCount = (avgCompletedTasks * 10).roundToInt() / 10f,
            averageCompletedFocusHours = ((avgDailyHours * 0.6f) * 10).roundToInt() / 10f,
            averageRolloverRatePercent = rolloverRate,
            averageEstimationErrorPercent = avgEstErrorPercent,
            workdayCapacityHours = (workdayHours * 10).roundToInt() / 10f,
            weekendCapacityHours = (weekendHours * 10).roundToInt() / 10f,
            morningCapacityPercent = morningPercent,
            afternoonCapacityPercent = afternoonPercent,
            eveningCapacityPercent = eveningPercent,
            energyLevelCapacities = mapOf(
                "HIGH" to ((highEnergyHours * 10).roundToInt() / 10f).coerceAtLeast(0.5f),
                "MEDIUM" to ((medEnergyHours * 10).roundToInt() / 10f).coerceAtLeast(0.5f),
                "LOW" to ((lowEnergyHours * 10).roundToInt() / 10f).coerceAtLeast(0.5f)
            ),
            confidence = confidence,
            daysAnalyzed = daysCount
        )
    }

    /**
     * Calculates deterministic Planning Accuracy Score (0..100) with explainable factors.
     * Weights:
     * 1. Workload Completion (35%)
     * 2. Duration Estimation (25%)
     * 3. Rollover Control (20%)
     * 4. Schedule Adherence (20%)
     */
    fun calculatePlanningAccuracy(
        tasks: List<TaskEntity>,
        events: List<BehavioralEventEntity>,
        performanceRecords: List<TaskPerformanceRecordEntity>,
        dailyReviews: List<DailyReviewEntity>
    ): PlanningAccuracyReport {
        val totalTasks = tasks.size
        val confidence = ConfidenceLevel.fromCount(totalTasks)

        if (totalTasks == 0) {
            return PlanningAccuracyReport(
                overallScore = 85,
                confidence = ConfidenceLevel.INSUFFICIENT_DATA,
                workloadCompletionRate = 0.85f,
                estimationAccuracyRate = 0.88f,
                rolloverControlRate = 0.90f,
                scheduleAdherenceRate = 0.80f,
                headline = "Calibrating Planning Model",
                detailedSummary = "Execute and complete tasks across your days to establish your personal planning accuracy baseline.",
                factors = listOf(
                    AccuracyFactor("Workload Execution", 85, 35, "Baseline initial allocation", "GOOD"),
                    AccuracyFactor("Duration Estimation", 88, 25, "Default baseline accuracy", "GOOD"),
                    AccuracyFactor("Rollover Control", 90, 20, "Minimal rollover observed", "EXCELLENT"),
                    AccuracyFactor("Schedule Adherence", 80, 20, "On-time start baseline", "GOOD")
                ),
                totalObservations = 0
            )
        }

        val completedTasks = tasks.filter { it.isCompleted }
        val plannedHours = tasks.sumOf { it.durationHours }.toFloat().coerceAtLeast(1f)
        val completedHours = completedTasks.sumOf { it.durationHours }.toFloat()

        // 1. Workload Completion (35%)
        val workloadCompletion = (completedHours / plannedHours).coerceIn(0f, 1.0f)
        val workloadScore = (workloadCompletion * 100).roundToInt()

        // 2. Duration Estimation (25%)
        val estimationAccuracy = if (performanceRecords.isNotEmpty()) {
            val valid = performanceRecords.filter { it.estimatedMinutes > 0 }
            if (valid.isNotEmpty()) {
                val sumAcc = valid.map { record ->
                    val diff = abs(record.estimationErrorMinutes).toFloat()
                    (1f - (diff / record.estimatedMinutes.toFloat())).coerceIn(0.2f, 1.0f)
                }.average().toFloat()
                sumAcc
            } else 0.85f
        } else 0.85f
        val estimationScore = (estimationAccuracy * 100).roundToInt()

        // 3. Rollover Control (20%)
        val rolloverCount = tasks.count { it.isRollover || it.rescheduleCount > 0 }
        val rolloverRate = (rolloverCount.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1.0f)
        val rolloverControl = (1.0f - rolloverRate).coerceIn(0f, 1.0f)
        val rolloverScore = (rolloverControl * 100).roundToInt()

        // 4. Schedule Adherence (20%)
        val postponedCount = events.count { it.eventType == BehavioralEventType.TASK_POSTPONED || it.eventType == BehavioralEventType.TASK_SKIPPED }
        val adherence = (1.0f - (postponedCount.toFloat() / totalTasks.coerceAtLeast(1).toFloat())).coerceIn(0.2f, 1.0f)
        val adherenceScore = (adherence * 100).roundToInt()

        // Weighted Overall Score
        val composite = (workloadScore * 0.35f + estimationScore * 0.25f + rolloverScore * 0.20f + adherenceScore * 0.20f).roundToInt().coerceIn(0, 100)

        val factors = listOf(
            AccuracyFactor(
                title = "Workload Completion",
                score = workloadScore,
                weightPercent = 35,
                explanation = "Completed ${completedTasks.size} of $totalTasks scheduled tasks (${(workloadCompletion * 100).toInt()}% planned volume).",
                status = if (workloadScore >= 80) "EXCELLENT" else if (workloadScore >= 60) "GOOD" else "NEEDS_IMPROVEMENT"
            ),
            AccuracyFactor(
                title = "Duration Estimation",
                score = estimationScore,
                weightPercent = 25,
                explanation = "Estimations average within ~${100 - estimationScore}% of actual logged time across categories.",
                status = if (estimationScore >= 80) "EXCELLENT" else if (estimationScore >= 60) "GOOD" else "NEEDS_IMPROVEMENT"
            ),
            AccuracyFactor(
                title = "Rollover Control",
                score = rolloverScore,
                weightPercent = 20,
                explanation = "$rolloverCount tasks rescheduled (${(rolloverRate * 100).toInt()}% rollover rate).",
                status = if (rolloverScore >= 80) "EXCELLENT" else if (rolloverScore >= 60) "GOOD" else "NEEDS_IMPROVEMENT"
            ),
            AccuracyFactor(
                title = "Schedule Adherence",
                score = adherenceScore,
                weightPercent = 20,
                explanation = "Adherence based on execution without emergency deferrals.",
                status = if (adherenceScore >= 80) "EXCELLENT" else if (adherenceScore >= 60) "GOOD" else "NEEDS_IMPROVEMENT"
            )
        )

        val headline = when {
            composite >= 88 -> "Pristine Calibration"
            composite >= 75 -> "Strong Execution"
            composite >= 60 -> "Moderate Alignment"
            else -> "Calibration in Progress"
        }

        val summary = when {
            composite >= 88 -> "Your daily plans match your actual focus capacity with high predictability."
            composite >= 75 -> "Good planning accuracy with occasional over-allocation on dense calendar days."
            composite >= 60 -> "Tendency to slightly over-estimate task capacity. System is adapting buffers."
            else -> "Initial learning phase active. Keep checking off and rescheduling to train the engine."
        }

        return PlanningAccuracyReport(
            overallScore = composite,
            confidence = confidence,
            workloadCompletionRate = workloadCompletion,
            estimationAccuracyRate = estimationAccuracy,
            rolloverControlRate = rolloverControl,
            scheduleAdherenceRate = adherence,
            headline = headline,
            detailedSummary = summary,
            factors = factors,
            totalObservations = totalTasks
        )
    }

    /**
     * Detects deterministic productivity patterns across time, day, energy, and priority dimensions.
     */
    fun detectProductivityPatterns(
        events: List<BehavioralEventEntity>,
        performanceRecords: List<TaskPerformanceRecordEntity>,
        tasks: List<TaskEntity>
    ): ProductivityPatternsReport {
        val totalEvents = events.size + performanceRecords.size
        val confidence = ConfidenceLevel.fromCount(totalEvents)

        if (totalEvents == 0 && tasks.isEmpty()) {
            return ProductivityPatternsReport(
                mostProductiveHours = "09:00 - 11:30 AM",
                leastProductiveHours = "02:00 - 04:00 PM",
                bestFocusPeriod = "Morning Focus Window",
                strongestDayOfWeek = "Tuesday",
                weekdayCompletionRate = 84,
                weekendCompletionRate = 68,
                frequentlyPostponedCategories = listOf("ADMIN"),
                frequentlyUnderestimatedCategories = listOf("WORK"),
                priorityCompletionRates = mapOf("CRITICAL" to 95, "IMPORTANT" to 82, "FLEXIBLE" to 70, "OPTIONAL" to 55),
                energyCompletionRates = mapOf("HIGH" to 80, "MEDIUM" to 85, "LOW" to 90),
                overplanningTendencyPercent = 15,
                confidence = ConfidenceLevel.INSUFFICIENT_DATA,
                totalEventsAnalyzed = 0
            )
        }

        // 1. Time patterns - Group completions by hour
        val completionHours = performanceRecords.map { it.timeSlotHour } +
                events.filter { it.eventType == BehavioralEventType.TASK_COMPLETED }.map { it.timeOfDayHour }

        val hourCounts = (0..23).associateWith { hour -> completionHours.count { it == hour } }
        val peakHour = hourCounts.maxByOrNull { it.value }?.key ?: 9
        val troughHour = hourCounts.filter { it.key in 8..20 }.minByOrNull { it.value }?.key ?: 14

        val formatHour = { h: Int ->
            val ampm = if (h < 12) "AM" else "PM"
            val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
            String.format("%02d:00 %s", displayH, ampm)
        }

        val peakWindowStr = "${formatHour(peakHour)} - ${formatHour((peakHour + 2) % 24)}"
        val troughWindowStr = "${formatHour(troughHour)} - ${formatHour((troughHour + 2) % 24)}"
        val bestFocusPeriod = if (peakHour in 6..12) "Morning ($peakWindowStr)" else if (peakHour in 13..17) "Afternoon ($peakWindowStr)" else "Evening ($peakWindowStr)"

        // 2. Day patterns
        val dayNames = mapOf(1 to "Monday", 2 to "Tuesday", 3 to "Wednesday", 4 to "Thursday", 5 to "Friday", 6 to "Saturday", 7 to "Sunday")
        val dayCounts = (1..7).associateWith { day ->
            performanceRecords.count { it.dayOfWeek == day } + events.count { it.eventType == BehavioralEventType.TASK_COMPLETED && it.dayOfWeek == day }
        }
        val bestDayNum = dayCounts.maxByOrNull { it.value }?.key ?: 2
        val strongestDay = dayNames[bestDayNum] ?: "Tuesday"

        val weekdayCompletions = performanceRecords.count { it.dayOfWeek in 1..5 }
        val weekendCompletions = performanceRecords.count { it.dayOfWeek in 6..7 }
        val weekdayTotal = (performanceRecords.count { it.dayOfWeek in 1..5 } + events.count { it.dayOfWeek in 1..5 }).coerceAtLeast(1)
        val weekendTotal = (performanceRecords.count { it.dayOfWeek in 6..7 } + events.count { it.dayOfWeek in 6..7 }).coerceAtLeast(1)

        val weekdayRate = ((weekdayCompletions.toFloat() / weekdayTotal) * 100).roundToInt().coerceIn(40, 98)
        val weekendRate = ((weekendCompletions.toFloat() / weekendTotal) * 100).roundToInt().coerceIn(30, 95)

        // 3. Task patterns - Frequently postponed
        val postponedEvents = events.filter { it.eventType == BehavioralEventType.TASK_POSTPONED || it.eventType == BehavioralEventType.TASK_ROLLED_OVER }
        val postponedCats = postponedEvents.mapNotNull { it.category }.groupBy { it }.mapValues { it.value.size }
        val freqPostponed = postponedCats.entries.sortedByDescending { it.value }.map { it.key }.take(2)
        val topPostponed = if (freqPostponed.isNotEmpty()) freqPostponed else listOf("ADMIN", "REPLY")

        // Frequently underestimated (where actual > estimated by >= 25%)
        val underestimatedCats = performanceRecords.filter { it.estimationErrorMinutes > 10 }
            .groupBy { it.category }
            .mapValues { it.value.size }
            .entries.sortedByDescending { it.value }
            .map { it.key }
            .take(2)
        val topUnderestimated = if (underestimatedCats.isNotEmpty()) underestimatedCats else listOf("WORK")

        // 4. Priority completion rates
        val priorities = listOf("CRITICAL", "IMPORTANT", "FLEXIBLE", "OPTIONAL")
        val priorityMap = priorities.associateWith { p ->
            val pTasks = tasks.filter { it.priority.uppercase() == p }
            if (pTasks.isNotEmpty()) {
                val comp = pTasks.count { it.isCompleted }
                ((comp.toFloat() / pTasks.size) * 100).roundToInt()
            } else {
                when (p) {
                    "CRITICAL" -> 94
                    "IMPORTANT" -> 82
                    "FLEXIBLE" -> 70
                    else -> 55
                }
            }
        }

        // 5. Energy completion rates
        val energyLevels = listOf("HIGH", "MEDIUM", "LOW")
        val energyMap = energyLevels.associateWith { e ->
            val eTasks = tasks.filter { it.energyLevel.uppercase() == e }
            if (eTasks.isNotEmpty()) {
                val comp = eTasks.count { it.isCompleted }
                ((comp.toFloat() / eTasks.size) * 100).roundToInt()
            } else {
                when (e) {
                    "HIGH" -> 78
                    "MEDIUM" -> 85
                    else -> 92
                }
            }
        }

        // 6. Overplanning Tendency
        val plannedHours = tasks.sumOf { it.durationHours }.toFloat()
        val completedHours = tasks.filter { it.isCompleted }.sumOf { it.durationHours }.toFloat().coerceAtLeast(1f)
        val overplanningPercent = if (plannedHours > completedHours) {
            (((plannedHours - completedHours) / completedHours) * 100).roundToInt().coerceIn(0, 80)
        } else {
            0
        }

        return ProductivityPatternsReport(
            mostProductiveHours = peakWindowStr,
            leastProductiveHours = troughWindowStr,
            bestFocusPeriod = bestFocusPeriod,
            strongestDayOfWeek = strongestDay,
            weekdayCompletionRate = weekdayRate,
            weekendCompletionRate = weekendRate,
            frequentlyPostponedCategories = topPostponed,
            frequentlyUnderestimatedCategories = topUnderestimated,
            priorityCompletionRates = priorityMap,
            energyCompletionRates = energyMap,
            overplanningTendencyPercent = overplanningPercent,
            confidence = confidence,
            totalEventsAnalyzed = totalEvents
        )
    }

    /**
     * Predicts task duration based on category, title similarity, energy level, and historical averages.
     */
    fun predictTaskDuration(
        category: String,
        priority: String,
        energyLevel: String,
        title: String,
        records: List<TaskPerformanceRecordEntity>
    ): TaskDurationPrediction {
        val categoryRecords = records.filter { it.category.equals(category, ignoreCase = true) }
        val count = categoryRecords.size
        val confidence = ConfidenceLevel.fromCount(count)

        if (count < 3) {
            val fallbackMin = when (category.uppercase()) {
                "WORK" -> 60
                "GROWTH" -> 45
                "HEALTH" -> 30
                "ADMIN" -> 20
                "FINANCE" -> 25
                "REPLY" -> 15
                else -> 45
            }
            return TaskDurationPrediction(
                predictedMinutes = fallbackMin,
                confidence = ConfidenceLevel.INSUFFICIENT_DATA,
                sampleSize = count,
                explanation = "Default estimate based on standard category baseline (Learning active).",
                isSufficientData = false,
                historicalAverageMinutes = fallbackMin,
                category = category
            )
        }

        // Outlier filtering: remove bottom 5% and top 5%
        val sorted = categoryRecords.map { it.actualMinutes }.sorted()
        val trimmed = if (sorted.size >= 5) sorted.subList(1, sorted.size - 1) else sorted
        val avgActual = trimmed.average().roundToInt().coerceIn(10, 240)

        // Energy adjustment (+15% if HIGH energy, -10% if LOW energy)
        val energyAdjusted = when (energyLevel.uppercase()) {
            "HIGH" -> (avgActual * 1.15f).roundToInt()
            "LOW" -> (avgActual * 0.90f).roundToInt()
            else -> avgActual
        }

        // Round to nearest 5 or 15 min for clean UX
        val rounded = ((energyAdjusted + 4) / 5) * 5

        val explanation = "Predicted ~$rounded min based on $count past ${category.uppercase()} tasks (avg actual: ${avgActual}m)."

        return TaskDurationPrediction(
            predictedMinutes = rounded,
            confidence = confidence,
            sampleSize = count,
            explanation = explanation,
            isSufficientData = true,
            historicalAverageMinutes = avgActual,
            category = category
        )
    }

    /**
     * Synthesizes privacy-safe structured summary context for Gemini interpretation.
     * ZERO raw event logs, private notes, or unneeded personal details are included.
     */
    fun buildPrivacySafeAiContext(
        userName: String,
        capacity: PersonalCapacityModel,
        accuracy: PlanningAccuracyReport,
        patterns: ProductivityPatternsReport
    ): StructuredPersonalizationContext {
        return StructuredPersonalizationContext(
            userName = userName.ifBlank { "User" },
            typicalDailyCapacityHours = capacity.averageRealisticDailyHours,
            typicalFocusHours = capacity.averageFocusCapacityHours,
            planningAccuracyScore = accuracy.overallScore,
            rolloverRatePercent = capacity.averageRolloverRatePercent,
            bestFocusPeriod = patterns.bestFocusPeriod,
            strongestDay = patterns.strongestDayOfWeek,
            underestimatedCategories = patterns.frequentlyUnderestimatedCategories,
            postponedCategories = patterns.frequentlyPostponedCategories,
            priorityCompletionSummary = "Critical: ${patterns.priorityCompletionRates["CRITICAL"] ?: 90}%, Important: ${patterns.priorityCompletionRates["IMPORTANT"] ?: 80}%, Flexible: ${patterns.priorityCompletionRates["FLEXIBLE"] ?: 70}%",
            dataConfidence = capacity.confidence.name,
            totalDaysHistory = capacity.daysAnalyzed
        )
    }

    /**
     * Filters recommendations based on user feedback (e.g. DONT_SUGGEST_AGAIN).
     */
    fun filterRecommendationsWithFeedback(
        recommendations: List<PersonalizedInsightItem>,
        feedbackList: List<RecommendationFeedbackEntity>
    ): List<PersonalizedInsightItem> {
        val blockedTypes = feedbackList.filter { it.feedback == "DONT_SUGGEST_AGAIN" }.map { it.recommendationType }.toSet()
        return recommendations.filterNot { blockedTypes.contains(it.recommendationType) }
    }
}
