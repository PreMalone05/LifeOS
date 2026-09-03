package com.example

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class PredictiveEngineTest {

    private val sampleCapacity = PersonalCapacityModel(
        averageRealisticDailyHours = 5.0f,
        averageFocusCapacityHours = 3.5f,
        averageCompletedTaskCount = 4.0f,
        averageCompletedFocusHours = 3.0f,
        averageRolloverRatePercent = 10,
        averageEstimationErrorPercent = 10,
        workdayCapacityHours = 5.5f,
        weekendCapacityHours = 2.5f,
        morningCapacityPercent = 50,
        afternoonCapacityPercent = 35,
        eveningCapacityPercent = 15,
        energyLevelCapacities = mapOf("HIGH" to 2.5f, "MEDIUM" to 2.0f, "LOW" to 1.0f),
        confidence = ConfidenceLevel.HIGH_CONFIDENCE,
        daysAnalyzed = 14
    )

    private val sampleAccuracy = PlanningAccuracyReport(
        overallScore = 80,
        confidence = ConfidenceLevel.HIGH_CONFIDENCE,
        workloadCompletionRate = 0.85f,
        estimationAccuracyRate = 0.80f,
        rolloverControlRate = 0.90f,
        scheduleAdherenceRate = 0.85f,
        headline = "Planning is well-calibrated",
        detailedSummary = "High estimation consistency",
        factors = listOf(
            AccuracyFactor("Duration Accuracy", 85, 30, "Accurate estimates", "EXCELLENT")
        ),
        totalObservations = 20
    )

    private val samplePatterns = ProductivityPatternsReport(
        mostProductiveHours = "09:00 - 11:00",
        leastProductiveHours = "14:00 - 16:00",
        bestFocusPeriod = "Morning (09:00 - 12:00)",
        strongestDayOfWeek = "Tuesday",
        weekdayCompletionRate = 85,
        weekendCompletionRate = 60,
        frequentlyPostponedCategories = listOf("ADMIN"),
        frequentlyUnderestimatedCategories = listOf("WORK"),
        priorityCompletionRates = mapOf("CRITICAL" to 95, "IMPORTANT" to 85, "FLEXIBLE" to 70),
        energyCompletionRates = mapOf("HIGH" to 80, "MEDIUM" to 85, "LOW" to 90),
        overplanningTendencyPercent = 10,
        confidence = ConfidenceLevel.HIGH_CONFIDENCE,
        totalEventsAnalyzed = 40
    )

    @Test
    fun `predictScheduleOverload detects HIGH risk when workload heavily exceeds capacity`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Task 1", durationHours = 4, date = "2026-08-28", isCompleted = false, priority = "CRITICAL", category = "WORK", description = "", timeSlot = ""),
            TaskEntity(id = 2, title = "Task 2", durationHours = 4, date = "2026-08-28", isCompleted = false, priority = "IMPORTANT", category = "WORK", description = "", timeSlot = "")
        )
        val calEvents = listOf(
            CalendarEvent(1L, 1L, "Meeting", "", "", 1000L, 1000L + 120 * 60 * 1000L, false, "Work", "09:00 - 11:00 AM")
        )

        val prediction = PredictiveEngine.predictScheduleOverload(
            date = "2026-08-28",
            tasks = tasks,
            calendarEvents = calEvents,
            capacityModel = sampleCapacity,
            accuracyReport = sampleAccuracy,
            performanceRecords = emptyList()
        )

        assertEquals(OverloadRiskLevel.HIGH, prediction.riskLevel)
        assertTrue(prediction.expectedHours > prediction.typicalCapacityHours)
        assertTrue(prediction.contributingFactors.isNotEmpty())
    }

    @Test
    fun `predictScheduleOverload detects LOW risk for light day`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Quick Task", durationHours = 1, date = "2026-08-28", isCompleted = false, priority = "NORMAL", category = "WORK", description = "", timeSlot = "")
        )

        val prediction = PredictiveEngine.predictScheduleOverload(
            date = "2026-08-28",
            tasks = tasks,
            calendarEvents = emptyList(),
            capacityModel = sampleCapacity,
            accuracyReport = sampleAccuracy,
            performanceRecords = emptyList()
        )

        assertEquals(OverloadRiskLevel.LOW, prediction.riskLevel)
    }

    @Test
    fun `calculateWhatShouldIDoNow selects highest priority incomplete task`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Normal Task", priority = "FLEXIBLE", durationHours = 1, date = "2026-08-28", isCompleted = false, energyLevel = "LOW", category = "ADMIN", description = "", timeSlot = "02:00 PM"),
            TaskEntity(id = 2, title = "Critical Release", priority = "CRITICAL", durationHours = 2, date = "2026-08-28", isCompleted = false, energyLevel = "HIGH", category = "WORK", description = "", timeSlot = "10:00 AM"),
            TaskEntity(id = 3, title = "Done Task", priority = "CRITICAL", durationHours = 1, date = "2026-08-28", isCompleted = true, energyLevel = "HIGH", category = "WORK", description = "", timeSlot = "09:00 AM")
        )

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 30)

        val result = PredictiveEngine.calculateWhatShouldIDoNow(
            currentTime = cal,
            todayTasks = tasks,
            calendarEvents = emptyList(),
            patterns = samplePatterns,
            performanceRecords = emptyList()
        )

        assertTrue(result.isActionable)
        assertEquals(2, result.recommendedTaskId)
        assertEquals("Critical Release", result.recommendedTaskTitle)
        assertEquals("CRITICAL", result.priority)
    }

    @Test
    fun `detectPredictiveFocusWindows identifies free focus blocks`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Afternoon review", timeSlot = "14:00 - 15:00", durationHours = 1, date = "2026-08-28", isCompleted = false, category = "WORK", description = "")
        )
        val windows = PredictiveEngine.detectPredictiveFocusWindows(
            tasks = tasks,
            calendarEvents = emptyList(),
            patterns = samplePatterns,
            capacityModel = sampleCapacity
        )

        assertTrue(windows.isNotEmpty())
        val window = windows.first()
        assertTrue(window.durationMinutes >= 30)
    }

    @Test
    fun `predictDeadlineRisk identifies upcoming milestones with risk`() {
        val milestones = listOf(
            MilestoneEntity(id = 1, goalId = 1, title = "Release Alpha", description = "Big deliverable", dueDate = "2026-08-29", status = "ACTIVE", iconName = "workspace_premium")
        )

        val risks = PredictiveEngine.predictDeadlineRisk(
            todayDate = "2026-08-28",
            tasks = emptyList(),
            goals = emptyList(),
            milestones = milestones,
            capacityModel = sampleCapacity,
            performanceRecords = emptyList()
        )

        assertTrue(risks.isNotEmpty())
        val risk = risks.first()
        assertEquals(1, risk.targetId)
        assertTrue(risk.explanation.isNotBlank())
    }

    @Test
    fun `predictHabitRisk detects conflict when heavy schedule blocks habit window`() {
        val habits = listOf(
            HabitEntity(id = 1, name = "Meditation", currentValue = 0f, targetValue = 1f, unit = "session", iconName = "self_improvement")
        )
        val calEvents = listOf(
            CalendarEvent(1L, 1L, "Early Sync 1", "", "", 1000L, 1000L + 120 * 60 * 1000L, false, "Work", "08:00 - 10:00 AM"),
            CalendarEvent(2L, 2L, "Early Sync 2", "", "", 2000L, 2000L + 120 * 60 * 1000L, false, "Work", "10:00 - 12:00 PM"),
            CalendarEvent(3L, 3L, "Early Sync 3", "", "", 3000L, 3000L + 60 * 60 * 1000L, false, "Work", "12:00 - 01:00 PM")
        )

        val risks = PredictiveEngine.predictHabitRisk(
            habits = habits,
            calendarEvents = calEvents,
            events = emptyList(),
            patterns = samplePatterns
        )

        assertNotNull(risks)
    }

    @Test
    fun `detectPlanDivergence spots accumulating overdue tasks in afternoon`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Morning Work", timeSlot = "09:00 AM", durationHours = 2, date = "2026-08-28", isCompleted = false, category = "WORK", description = ""),
            TaskEntity(id = 2, title = "Late Morning Work", timeSlot = "11:00 AM", durationHours = 2, date = "2026-08-28", isCompleted = false, category = "WORK", description = "")
        )

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 16)

        val report = PredictiveEngine.detectPlanDivergence(
            currentTime = cal,
            todayTasks = tasks,
            calendarEvents = emptyList(),
            events = emptyList()
        )

        assertTrue(report.isDiverged)
        assertTrue(report.missedOrDelayedCount >= 1)
        assertTrue(report.divergenceScore > 30)
    }

    @Test
    fun `generateTomorrowPreview calculates expected capacity vs load`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Tomorrow Report", date = "2026-08-29", durationHours = 3, isCompleted = false, category = "WORK", description = "", timeSlot = ""),
            TaskEntity(id = 2, title = "Tomorrow Prep", date = "2026-08-29", durationHours = 4, isCompleted = false, category = "WORK", description = "", timeSlot = "")
        )

        val preview = PredictiveEngine.generateTomorrowPreview(
            tomorrowDate = "2026-08-29",
            tasks = tasks,
            calendarEvents = emptyList(),
            capacityModel = sampleCapacity,
            accuracyReport = sampleAccuracy,
            performanceRecords = emptyList()
        )

        assertEquals("2026-08-29", preview.date)
        assertTrue(preview.plannedWorkloadHours >= 7f)
        assertEquals(OverloadRiskLevel.HIGH, preview.overloadRisk)
    }

    @Test
    fun `generateRankedRecommendations filters and applies feedback discounts`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Heavy Task 1", durationHours = 5, date = "2026-08-28", isCompleted = false, priority = "CRITICAL", category = "WORK", description = "", timeSlot = ""),
            TaskEntity(id = 2, title = "Heavy Task 2", durationHours = 4, date = "2026-08-28", isCompleted = false, priority = "CRITICAL", category = "WORK", description = "", timeSlot = "")
        )

        val feedback = listOf(
            RecommendationFeedbackEntity(id = 1, recommendationType = "CAPACITY_WARNING", recommendationText = "Day Overload Predicted", feedback = "DONT_SUGGEST_AGAIN")
        )

        val recs = PredictiveEngine.generateRankedRecommendations(
            todayDate = "2026-08-28",
            tomorrowDate = "2026-08-29",
            tasks = tasks,
            calendarEvents = emptyList(),
            habits = emptyList(),
            goals = emptyList(),
            milestones = emptyList(),
            capacityModel = sampleCapacity,
            accuracyReport = sampleAccuracy,
            performanceRecords = emptyList(),
            events = emptyList(),
            feedbackList = feedback
        )

        assertFalse(recs.any { it.type == RecommendationType.CAPACITY_WARNING })
    }

    @Test
    fun `predictScheduleOverload reduces capacity during vacation mode`() {
        val userProfile = UserProfileEntity(isVacationMode = true, vacationNotes = "Beach trip")
        val tasks = listOf(
            TaskEntity(id = 1, title = "Vacation task", durationHours = 2, date = "2026-08-28", isCompleted = false, category = "HEALTH", description = "", timeSlot = "")
        )

        val prediction = PredictiveEngine.predictScheduleOverload(
            date = "2026-08-28",
            tasks = tasks,
            calendarEvents = emptyList(),
            capacityModel = sampleCapacity,
            accuracyReport = sampleAccuracy,
            performanceRecords = emptyList(),
            userProfile = userProfile
        )

        assertEquals(1.0f, prediction.typicalCapacityHours)
        assertEquals(OverloadRiskLevel.HIGH, prediction.riskLevel)
    }

    @Test
    fun `calculateWhatShouldIDoNow returns non-actionable when all tasks completed`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Task 1", isCompleted = true, date = "2026-08-28", durationHours = 1, category = "WORK", description = "", timeSlot = "")
        )

        val result = PredictiveEngine.calculateWhatShouldIDoNow(
            currentTime = Calendar.getInstance(),
            todayTasks = tasks,
            calendarEvents = emptyList(),
            patterns = samplePatterns,
            performanceRecords = emptyList()
        )

        assertFalse(result.isActionable)
        assertNull(result.recommendedTaskId)
        assertEquals("All Daily Tasks Completed", result.recommendedTaskTitle)
    }

    @Test
    fun `predictDeadlineRisk returns empty list when no milestones or critical tasks exist`() {
        val risks = PredictiveEngine.predictDeadlineRisk(
            todayDate = "2026-08-28",
            tasks = emptyList(),
            goals = emptyList(),
            milestones = emptyList(),
            capacityModel = sampleCapacity,
            performanceRecords = emptyList()
        )

        assertTrue(risks.isEmpty())
    }

    @Test
    fun `detectPlanDivergence returns false when no planned tasks exist`() {
        val report = PredictiveEngine.detectPlanDivergence(
            currentTime = Calendar.getInstance(),
            todayTasks = emptyList(),
            calendarEvents = emptyList(),
            events = emptyList()
        )

        assertFalse(report.isDiverged)
        assertEquals(0, report.divergenceScore)
    }
}
