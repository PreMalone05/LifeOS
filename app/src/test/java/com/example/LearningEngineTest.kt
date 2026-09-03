package com.example

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class LearningEngineTest {

    @Test
    fun `calculatePersonalCapacity with empty history returns sensible defaults with insufficient data`() {
        val model = LearningEngine.calculatePersonalCapacity(
            tasks = emptyList(),
            performanceRecords = emptyList(),
            dailyReviews = emptyList(),
            events = emptyList()
        )

        assertEquals(4.5f, model.averageRealisticDailyHours, 0.01f)
        assertEquals(3.0f, model.averageFocusCapacityHours, 0.01f)
        assertEquals(15, model.averageRolloverRatePercent)
        assertEquals(5.0f, model.workdayCapacityHours, 0.01f)
        assertEquals(2.5f, model.weekendCapacityHours, 0.01f)
        assertEquals(ConfidenceLevel.INSUFFICIENT_DATA, model.confidence)
        assertEquals(0, model.daysAnalyzed)
    }

    @Test
    fun `calculatePersonalCapacity calculates realistic capacity and upgrades confidence with history`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Task 1", category = "WORK", timeSlot = "09:00 - 11:00 AM", description = "Work task", isCompleted = true, durationHours = 2, date = "2026-08-20", energyLevel = "HIGH"),
            TaskEntity(id = 2, title = "Task 2", category = "WORK", timeSlot = "11:00 - 01:00 PM", description = "Work task", isCompleted = true, durationHours = 2, date = "2026-08-20", energyLevel = "MEDIUM"),
            TaskEntity(id = 3, title = "Task 3", category = "GROWTH", timeSlot = "02:00 - 05:00 PM", description = "Growth task", isCompleted = true, durationHours = 3, date = "2026-08-21", energyLevel = "HIGH"),
            TaskEntity(id = 4, title = "Task 4", category = "HEALTH", timeSlot = "05:00 - 08:00 PM", description = "Health task", isCompleted = true, durationHours = 3, date = "2026-08-21", energyLevel = "LOW"),
            TaskEntity(id = 5, title = "Task 5", category = "WORK", timeSlot = "09:00 - 01:00 PM", description = "Work task", isCompleted = true, durationHours = 4, date = "2026-08-22", energyLevel = "HIGH"),
            TaskEntity(id = 6, title = "Task 6", category = "ADMIN", timeSlot = "02:00 - 04:00 PM", description = "Admin task", isCompleted = true, durationHours = 2, date = "2026-08-23", energyLevel = "LOW")
        )

        val records = listOf(
            TaskPerformanceRecordEntity(taskId = 1, category = "WORK", estimatedMinutes = 120, actualMinutes = 120, estimationErrorMinutes = 0, priority = "CRITICAL", energyLevel = "HIGH", timeSlotHour = 9, dayOfWeek = 4, date = "2026-08-20"),
            TaskPerformanceRecordEntity(taskId = 2, category = "WORK", estimatedMinutes = 120, actualMinutes = 120, estimationErrorMinutes = 0, priority = "IMPORTANT", energyLevel = "MEDIUM", timeSlotHour = 14, dayOfWeek = 4, date = "2026-08-20"),
            TaskPerformanceRecordEntity(taskId = 3, category = "GROWTH", estimatedMinutes = 180, actualMinutes = 180, estimationErrorMinutes = 0, priority = "IMPORTANT", energyLevel = "HIGH", timeSlotHour = 10, dayOfWeek = 5, date = "2026-08-21")
        )

        val reviews = listOf(
            DailyReviewEntity(date = "2026-08-20", scoreRating = "BALANCED", summaryNotes = "Good day", completedCount = 2, deferredCount = 0),
            DailyReviewEntity(date = "2026-08-21", scoreRating = "AMBITIOUS", summaryNotes = "Strong deep work", completedCount = 2, deferredCount = 0),
            DailyReviewEntity(date = "2026-08-22", scoreRating = "BALANCED", summaryNotes = "Solid progress", completedCount = 1, deferredCount = 0),
            DailyReviewEntity(date = "2026-08-23", scoreRating = "RECOVERY", summaryNotes = "Light admin", completedCount = 1, deferredCount = 0)
        )

        val model = LearningEngine.calculatePersonalCapacity(
            tasks = tasks,
            performanceRecords = records,
            dailyReviews = reviews,
            events = emptyList()
        )

        assertEquals(4, model.daysAnalyzed)
        assertTrue(model.averageRealisticDailyHours > 0f)
        assertTrue(model.averageFocusCapacityHours > 0f)
        assertTrue(model.confidence == ConfidenceLevel.LOW_CONFIDENCE || model.confidence == ConfidenceLevel.MODERATE_CONFIDENCE)
    }

    @Test
    fun `calculatePlanningAccuracy correctly calculates weighted score and explainable factors`() {
        val tasks = listOf(
            TaskEntity(id = 1, title = "Task 1", category = "WORK", timeSlot = "09:00 - 10:00 AM", description = "Description", isCompleted = true, durationHours = 1, date = "2026-08-20", isRollover = false),
            TaskEntity(id = 2, title = "Task 2", category = "WORK", timeSlot = "10:00 - 12:00 PM", description = "Description", isCompleted = true, durationHours = 2, date = "2026-08-20", isRollover = false),
            TaskEntity(id = 3, title = "Task 3", category = "WORK", timeSlot = "01:00 - 02:00 PM", description = "Description", isCompleted = false, durationHours = 1, date = "2026-08-20", isRollover = true)
        )

        val records = listOf(
            TaskPerformanceRecordEntity(taskId = 1, category = "WORK", estimatedMinutes = 60, actualMinutes = 60, estimationErrorMinutes = 0, priority = "CRITICAL", energyLevel = "HIGH", timeSlotHour = 9, dayOfWeek = 4),
            TaskPerformanceRecordEntity(taskId = 2, category = "WORK", estimatedMinutes = 120, actualMinutes = 120, estimationErrorMinutes = 0, priority = "IMPORTANT", energyLevel = "MEDIUM", timeSlotHour = 14, dayOfWeek = 4)
        )

        val report = LearningEngine.calculatePlanningAccuracy(
            tasks = tasks,
            events = emptyList(),
            performanceRecords = records,
            dailyReviews = emptyList()
        )

        assertEquals(3, report.totalObservations)
        assertTrue(report.overallScore in 50..100)
        assertEquals(4, report.factors.size)
        assertTrue(report.factors.any { it.title == "Workload Completion" })
        assertTrue(report.factors.any { it.title == "Duration Estimation" })
        assertTrue(report.factors.any { it.title == "Rollover Control" })
        assertTrue(report.factors.any { it.title == "Schedule Adherence" })
    }

    @Test
    fun `detectProductivityPatterns identifies peak flow hour and strongest day`() {
        val events = listOf(
            BehavioralEventEntity(eventType = BehavioralEventType.TASK_COMPLETED, timeOfDayHour = 9, dayOfWeek = 2),
            BehavioralEventEntity(eventType = BehavioralEventType.TASK_COMPLETED, timeOfDayHour = 9, dayOfWeek = 2),
            BehavioralEventEntity(eventType = BehavioralEventType.TASK_COMPLETED, timeOfDayHour = 9, dayOfWeek = 3),
            BehavioralEventEntity(eventType = BehavioralEventType.TASK_COMPLETED, timeOfDayHour = 10, dayOfWeek = 2),
            BehavioralEventEntity(eventType = BehavioralEventType.TASK_COMPLETED, timeOfDayHour = 15, dayOfWeek = 4)
        )

        val patterns = LearningEngine.detectProductivityPatterns(
            events = events,
            performanceRecords = emptyList(),
            tasks = emptyList()
        )

        assertTrue(patterns.mostProductiveHours.contains("09:00"))
        assertEquals("Tuesday", patterns.strongestDayOfWeek)
        assertEquals(5, patterns.totalEventsAnalyzed)
    }

    @Test
    fun `predictTaskDuration falls back to category defaults with insufficient data`() {
        val prediction = LearningEngine.predictTaskDuration(
            category = "WORK",
            priority = "IMPORTANT",
            energyLevel = "MEDIUM",
            title = "Code Review",
            records = emptyList()
        )

        assertEquals(60, prediction.predictedMinutes)
        assertEquals(ConfidenceLevel.INSUFFICIENT_DATA, prediction.confidence)
        assertFalse(prediction.isSufficientData)
        assertEquals(0, prediction.sampleSize)
    }

    @Test
    fun `predictTaskDuration predicts trimmed average with sufficient performance records`() {
        val records = listOf(
            TaskPerformanceRecordEntity(taskId = 1, category = "WORK", estimatedMinutes = 30, actualMinutes = 45, estimationErrorMinutes = 15, priority = "IMPORTANT", energyLevel = "MEDIUM", timeSlotHour = 9, dayOfWeek = 1),
            TaskPerformanceRecordEntity(taskId = 2, category = "WORK", estimatedMinutes = 30, actualMinutes = 50, estimationErrorMinutes = 20, priority = "IMPORTANT", energyLevel = "MEDIUM", timeSlotHour = 10, dayOfWeek = 2),
            TaskPerformanceRecordEntity(taskId = 3, category = "WORK", estimatedMinutes = 30, actualMinutes = 40, estimationErrorMinutes = 10, priority = "IMPORTANT", energyLevel = "MEDIUM", timeSlotHour = 11, dayOfWeek = 3),
            TaskPerformanceRecordEntity(taskId = 4, category = "WORK", estimatedMinutes = 30, actualMinutes = 45, estimationErrorMinutes = 15, priority = "IMPORTANT", energyLevel = "MEDIUM", timeSlotHour = 14, dayOfWeek = 4)
        )

        val prediction = LearningEngine.predictTaskDuration(
            category = "WORK",
            priority = "IMPORTANT",
            energyLevel = "MEDIUM",
            title = "API Optimization",
            records = records
        )

        assertTrue(prediction.isSufficientData)
        assertEquals(4, prediction.sampleSize)
        assertTrue(prediction.predictedMinutes in 40..55)
    }

    @Test
    fun `filterRecommendationsWithFeedback blocks user dismissed recommendation types`() {
        val recommendations = listOf(
            PersonalizedInsightItem(
                id = "1",
                title = "Peak Morning Block",
                insightText = "Move work to 9am",
                whyExplanation = "High morning velocity",
                category = "TIMING",
                confidence = ConfidenceLevel.MODERATE_CONFIDENCE,
                evidencePoints = listOf("High completion"),
                recommendationType = "TIME_SLOT_SUGGESTION"
            ),
            PersonalizedInsightItem(
                id = "2",
                title = "Reduce Overplanning",
                insightText = "Cap tasks at 4 per day",
                whyExplanation = "Elevated rollover",
                category = "CAPACITY",
                confidence = ConfidenceLevel.MODERATE_CONFIDENCE,
                evidencePoints = listOf("High rollover"),
                recommendationType = "CAPACITY_LIMIT"
            )
        )

        val feedback = listOf(
            RecommendationFeedbackEntity(
                recommendationType = "CAPACITY_LIMIT",
                recommendationText = "Reduce Overplanning",
                feedback = "DONT_SUGGEST_AGAIN"
            )
        )

        val filtered = LearningEngine.filterRecommendationsWithFeedback(recommendations, feedback)
        assertEquals(1, filtered.size)
        assertEquals("TIME_SLOT_SUGGESTION", filtered[0].recommendationType)
    }
}
