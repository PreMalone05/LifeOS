package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex",
    val level: Int = 12,
    val xp: Int = 2450,
    val maxXp: Int = 3000,
    val focusPoints: Float = 48.2f,
    val streak: Int = 14,
    val uptime: Int = 94,
    val rankPercent: Int = 2,
    val coachPersonality: String = "The Stoic Mentor",
    val currentVibe: String = "Deep Work & Clarity"
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "WORK", "HEALTH", "GROWTH", "FINANCE", "ADMIN", "REPLY"
    val timeSlot: String, // e.g., "09:00 - 10:30 AM"
    val description: String,
    val isCompleted: Boolean = false,
    val isRollover: Boolean = false,
    val date: String = "2024-10-24", // YYYY-MM-DD
    val durationHours: Int = 1,
    val location: String? = null
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val currentValue: Float,
    val targetValue: Float,
    val unit: String, // "L", "min", "Done"
    val isCompleted: Boolean = false,
    val iconName: String, // "water_drop", "menu_book", "self_improvement", "fitness_center"
    val streak: Int = 12
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetTimeline: String = "Est. 6 Months",
    val domain: String, // "Career", "Health", "Wealth", "Growth"
    val horizon: String, // "Monthly", "Quarterly", "Yearly"
    val visionImage: String? = null,
    val progressPercent: Int = 0
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val title: String,
    val description: String,
    val status: String, // "ACTIVE", "LOCKED", "COMPLETED"
    val dueDate: String? = null,
    val iconName: String // "payments", "sports_motorsports", "shield", "two_wheeler", "architecture", "groups", "terminal", "workspace_premium"
)

@Entity(tableName = "sub_tasks")
data class SubTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val milestoneId: Int,
    val title: String,
    val isCompleted: Boolean = false
)
