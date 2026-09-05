package com.example.ai

import com.example.data.GoalEntity
import com.example.data.HabitEntity
import com.example.data.TaskEntity

/**
 * Lightweight Context Summary Builder for LifeOS AI Coach Continuity.
 *
 * Compiles a compact, token-efficient, high-signal summary of the user's
 * current goals, active habits + streaks, today's tasks, and recent task completions.
 * Included in system instructions for AI chat conversations and morning briefings
 * across all active AI providers (Gemini, OpenRouter, OpenAI, Custom/Local).
 */
object AiContextSummaryBuilder {

    fun buildSummary(
        goals: List<GoalEntity>,
        habits: List<HabitEntity>,
        allTasks: List<TaskEntity>,
        todayDate: String
    ): String {
        val sb = StringBuilder()
        sb.append("--- USER LIVE CONTEXT SUMMARY ---\n")

        // 1. Current Goals
        if (goals.isEmpty()) {
            sb.append("• Goals: None defined yet.\n")
        } else {
            val goalItems = goals.take(4).map { g ->
                val domainStr = if (g.domain.isNotBlank()) " [${g.domain}]" else ""
                val timelineStr = if (g.targetTimeline.isNotBlank()) ", ${g.targetTimeline}" else ""
                "\"${g.title}\"$domainStr (${g.progressPercent}%$timelineStr)"
            }
            val more = if (goals.size > 4) " (+${goals.size - 4} more)" else ""
            sb.append("• Goals (${goals.size} active): ${goalItems.joinToString(", ")}$more\n")
        }

        // 2. Active Habits & Streaks
        if (habits.isEmpty()) {
            sb.append("• Habits: None active yet.\n")
        } else {
            val habitItems = habits.take(6).map { h ->
                val status = if (h.isCompleted) "Done today" else "Pending"
                "${h.name} (${h.streak}d streak, $status)"
            }
            val more = if (habits.size > 6) " (+${habits.size - 6} more)" else ""
            sb.append("• Habits (${habits.size}): ${habitItems.joinToString(", ")}$more\n")
        }

        // 3. Today's Tasks
        val todayTasks = allTasks.filter { it.date == todayDate }
        if (todayTasks.isEmpty()) {
            sb.append("• Today's Tasks ($todayDate): No tasks scheduled.\n")
        } else {
            val completedCount = todayTasks.count { it.isCompleted }
            val pending = todayTasks.filter { !it.isCompleted }
            val done = todayTasks.filter { it.isCompleted }

            val pendingStr = if (pending.isNotEmpty()) {
                "Pending: " + pending.take(4).joinToString("; ") { t ->
                    val time = if (t.timeSlot.isNotBlank()) " @ ${t.timeSlot}" else ""
                    val priority = if (t.priority.equals("CRITICAL", ignoreCase = true) || t.priority.equals("IMPORTANT", ignoreCase = true)) " [${t.priority}]" else ""
                    "${t.title}$priority$time"
                } + if (pending.size > 4) " (+${pending.size - 4} more)" else ""
            } else {
                "All tasks completed!"
            }

            val doneStr = if (done.isNotEmpty()) {
                " | Done: " + done.take(3).joinToString(", ") { it.title } + if (done.size > 3) " (+${done.size - 3})" else ""
            } else ""

            sb.append("• Today's Tasks ($completedCount/${todayTasks.size} done): $pendingStr$doneStr\n")
        }

        // 4. Recent Completions (prior days)
        val priorCompletions = allTasks
            .filter { it.isCompleted && it.date != todayDate }
            .sortedByDescending { it.date }
            .take(4)

        if (priorCompletions.isNotEmpty()) {
            val recentStr = priorCompletions.joinToString(", ") { t ->
                "\"${t.title}\" (${t.date})"
            }
            sb.append("• Recent Completions: $recentStr\n")
        } else {
            val anyDone = allTasks.filter { it.isCompleted }.take(3)
            if (anyDone.isNotEmpty()) {
                sb.append("• Recent Completions: ${anyDone.joinToString(", ") { "\"${it.title}\"" }}\n")
            } else {
                sb.append("• Recent Completions: None recorded yet.\n")
            }
        }

        sb.append("----------------------------------")
        return sb.toString()
    }
}
