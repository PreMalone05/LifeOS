package com.example

import com.example.ai.AiContextSummaryBuilder
import com.example.data.GoalEntity
import com.example.data.HabitEntity
import com.example.data.TaskEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class AiContextSummaryBuilderTest {

    @Test
    fun `buildSummary with empty data returns friendly fallback messages`() {
        val summary = AiContextSummaryBuilder.buildSummary(
            goals = emptyList(),
            habits = emptyList(),
            allTasks = emptyList(),
            todayDate = "2026-09-05"
        )

        assertTrue(summary.contains("Goals: None defined yet"))
        assertTrue(summary.contains("Habits: None active yet"))
        assertTrue(summary.contains("Today's Tasks (2026-09-05): No tasks scheduled"))
        assertTrue(summary.contains("Recent Completions: None recorded yet"))
    }

    @Test
    fun `buildSummary correctly formats goals, habits, today tasks, and recent completions`() {
        val goals = listOf(
            GoalEntity(id = 1, title = "Launch Startup MVP", domain = "Career", progressPercent = 75, targetTimeline = "Est. 3 Months"),
            GoalEntity(id = 2, title = "Run Half Marathon", domain = "Health", progressPercent = 40, targetTimeline = "Est. 6 Months")
        )

        val habits = listOf(
            HabitEntity(id = 1, name = "Morning Hydration", currentValue = 1f, targetValue = 1f, unit = "Done", isCompleted = true, iconName = "water_drop", streak = 14),
            HabitEntity(id = 2, name = "Deep Work 90m", currentValue = 0f, targetValue = 90f, unit = "min", isCompleted = false, iconName = "terminal", streak = 5)
        )

        val tasks = listOf(
            TaskEntity(id = 1, title = "Finalize API Contract", category = "WORK", timeSlot = "09:00 - 10:30", description = "Core endpoints", isCompleted = false, priority = "CRITICAL", date = "2026-09-05"),
            TaskEntity(id = 2, title = "Morning Yoga", category = "HEALTH", timeSlot = "07:30 - 08:00", description = "Stretch", isCompleted = true, priority = "FLEXIBLE", date = "2026-09-05"),
            TaskEntity(id = 3, title = "Quarterly Taxes", category = "FINANCE", timeSlot = "", description = "Submit", isCompleted = true, priority = "IMPORTANT", date = "2026-09-04")
        )

        val summary = AiContextSummaryBuilder.buildSummary(
            goals = goals,
            habits = habits,
            allTasks = tasks,
            todayDate = "2026-09-05"
        )

        // Verify goals
        assertTrue(summary.contains("Launch Startup MVP"))
        assertTrue(summary.contains("75%"))
        assertTrue(summary.contains("Run Half Marathon"))

        // Verify habits + streaks
        assertTrue(summary.contains("Morning Hydration"))
        assertTrue(summary.contains("14d streak, Done today"))
        assertTrue(summary.contains("Deep Work 90m"))
        assertTrue(summary.contains("5d streak, Pending"))

        // Verify today's tasks
        assertTrue(summary.contains("1/2 done"))
        assertTrue(summary.contains("Finalize API Contract"))
        assertTrue(summary.contains("Morning Yoga"))

        // Verify recent completions
        assertTrue(summary.contains("Quarterly Taxes"))
        assertTrue(summary.contains("2026-09-04"))
    }
}
