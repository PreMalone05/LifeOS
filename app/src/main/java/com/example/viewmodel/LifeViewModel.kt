package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LifeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifeRepository

    init {
        val database = LifeDatabase.getDatabase(application)
        repository = LifeRepository(database.lifeDao())

        // Check and pre-populate if empty
        viewModelScope.launch {
            repository.userProfile.firstOrNull()?.let {
                // Profile exists, no action needed
            } ?: run {
                repository.populateDefaultData()
            }
        }
    }

    // Navigation State (Simple, robust screen state holder)
    private val _currentScreen = MutableStateFlow("TODAY")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Shared navigation arguments
    private val _selectedGoalId = MutableStateFlow<Int?>(1)
    val selectedGoalId: StateFlow<Int?> = _selectedGoalId.asStateFlow()

    private val _selectedMilestoneId = MutableStateFlow<Int?>(null)
    val selectedMilestoneId: StateFlow<Int?> = _selectedMilestoneId.asStateFlow()

    // Celebration state
    private val _celebrationMessage = MutableStateFlow<String?>(null)
    val celebrationMessage: StateFlow<String?> = _celebrationMessage.asStateFlow()

    fun navigateTo(screen: String, goalId: Int? = null, milestoneId: Int? = null) {
        goalId?.let { _selectedGoalId.value = it }
        milestoneId?.let { _selectedMilestoneId.value = it }
        _currentScreen.value = screen
    }

    fun triggerCelebration(message: String) {
        _celebrationMessage.value = message
        _currentScreen.value = "MISSION_ACCOMPLISHED"
    }

    fun dismissCelebration() {
        _celebrationMessage.value = null
        _currentScreen.value = "TODAY"
    }

    // UI Streams
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Goal's Milestones
    val selectedMilestones: StateFlow<List<MilestoneEntity>> = selectedGoalId
        .flatMapLatest { id ->
            if (id != null) repository.getMilestonesForGoal(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Milestone's Sub-tasks
    val selectedSubTasks: StateFlow<List<SubTaskEntity>> = selectedMilestoneId
        .flatMapLatest { id ->
            if (id != null) repository.getSubTasksForMilestone(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Task & Planner Actions
    fun addTask(
        title: String,
        category: String,
        timeSlot: String,
        description: String,
        durationHours: Int = 1,
        location: String? = null,
        date: String = "2024-10-24"
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    category = category,
                    timeSlot = timeSlot,
                    description = description,
                    durationHours = durationHours,
                    location = location,
                    date = date
                )
            )
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)

            if (updated.isCompleted) {
                // Reward XP (+50) and Focus Points (+10)
                rewardXpAndFocus(50, 10f)
                triggerCelebration(updated.title)
            }
        }
    }

    // Habit Actions
    fun checkInHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val newValue = if (habit.unit == "Done") {
                habit.targetValue
            } else {
                (habit.currentValue + (habit.targetValue / 3.0f)).coerceAtMost(habit.targetValue)
            }
            val completed = newValue >= habit.targetValue
            val updated = habit.copy(
                currentValue = newValue,
                isCompleted = completed,
                streak = if (completed && !habit.isCompleted) habit.streak + 1 else habit.streak
            )
            repository.updateHabit(updated)

            if (completed && !habit.isCompleted) {
                rewardXpAndFocus(30, 5f)
            }
        }
    }

    fun resetHabitValue(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(currentValue = 0f, isCompleted = false))
        }
    }

    // Goal Setup & AI Milestone Generation
    fun createGoalFromVision(
        title: String,
        domain: String,
        horizon: String,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            val defaultImg = imageUrl ?: when (domain) {
                "Career" -> "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=600&auto=format&fit=crop&q=80"
                "Health" -> "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80"
                "Wealth" -> "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600&auto=format&fit=crop&q=80"
                else -> "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80"
            }

            val goalId = repository.insertGoal(
                GoalEntity(
                    title = title,
                    domain = domain,
                    horizon = horizon,
                    visionImage = defaultImg,
                    progressPercent = 0
                )
            ).toInt()

            // Generate customized Milestones based on Goal Domain
            val milestones = when (domain) {
                "Career" -> listOf(
                    MilestoneEntity(goalId = goalId, title = "Skill Mastery Assessment", description = "Evaluate and identify core areas of professional development.", status = "ACTIVE", iconName = "architecture"),
                    MilestoneEntity(goalId = goalId, title = "Execute Major Enterprise Deliverable", description = "Deliver the foundational tech release scaling up to 1M requests.", status = "LOCKED", iconName = "groups"),
                    MilestoneEntity(goalId = goalId, title = "Acquire Industry Credentials", description = "Pass certifications validating professional standing.", status = "LOCKED", iconName = "workspace_premium")
                )
                "Health" -> listOf(
                    MilestoneEntity(goalId = goalId, title = "Establish Sleep & Hydration Loop", description = "Anchor consistent sleep schedules and hydrate at peak times.", status = "ACTIVE", iconName = "self_improvement"),
                    MilestoneEntity(goalId = goalId, title = "Conduct Biomarker Test", description = "Examine metabolic baseline profile metrics.", status = "LOCKED", iconName = "shield"),
                    MilestoneEntity(goalId = goalId, title = "Complete Half-Marathon Training", description = "Consistently run 10k loops for cardiorespiratory fitness.", status = "LOCKED", iconName = "fitness_center")
                )
                "Wealth" -> listOf(
                    MilestoneEntity(goalId = goalId, title = "Track Automated Net Worth Basis", description = "Synthesize and map all account balances local-first.", status = "ACTIVE", iconName = "payments"),
                    MilestoneEntity(goalId = goalId, title = "Secure Investment Allocation", description = "Lock in index fund distribution for maximum yield compound returns.", status = "LOCKED", iconName = "shield")
                )
                else -> listOf(
                    MilestoneEntity(goalId = goalId, title = "Initiate Growth Audit Log", description = "Review and inspect weekly reading volume metrics.", status = "ACTIVE", iconName = "terminal"),
                    MilestoneEntity(goalId = goalId, title = "Implement Focus Blocks", description = "Maintain 4 hours of daily uninterrupted deep work state.", status = "LOCKED", iconName = "workspace_premium")
                )
            }

            for (m in milestones) {
                repository.insertMilestone(m)
            }

            // Navigate to milestone overview of the newly created goal
            navigateTo("MILESTONE_PLAN", goalId = goalId)
        }
    }

    // Milestone Check-In and Sub-Task Checklist operations
    fun toggleSubTask(subTask: SubTaskEntity) {
        viewModelScope.launch {
            val updated = subTask.copy(isCompleted = !subTask.isCompleted)
            repository.updateSubTask(updated)

            // Dynamic recalculation of milestone progress
            recalculateMilestoneProgress(updated.milestoneId)
        }
    }

    fun addSubTaskToMilestone(milestoneId: Int, title: String) {
        viewModelScope.launch {
            repository.insertSubTask(SubTaskEntity(milestoneId = milestoneId, title = title, isCompleted = false))
            recalculateMilestoneProgress(milestoneId)
        }
    }

    private suspend fun recalculateMilestoneProgress(milestoneId: Int) {
        val subTasks = repository.getSubTasksForMilestone(milestoneId).firstOrNull() ?: emptyList()
        if (subTasks.isNotEmpty()) {
            val completedCount = subTasks.count { it.isCompleted }
            val progressPercent = (completedCount * 100) / subTasks.size

            // Update parent Goal progress
            repository.getMilestoneById(milestoneId)?.let { milestone ->
                val parentGoalId = milestone.goalId
                val goalMilestones = repository.getMilestonesForGoal(parentGoalId).firstOrNull() ?: emptyList()
                if (goalMilestones.isNotEmpty()) {
                    // Estimate parent goal progress based on active milestones
                    val totalMilestoneProgress = goalMilestones.sumOf { m ->
                        if (m.id == milestoneId) progressPercent
                        else if (m.status == "COMPLETED") 100 else 0
                    }
                    val parentGoalProgress = totalMilestoneProgress / goalMilestones.size
                    repository.allGoals.firstOrNull()?.find { it.id == parentGoalId }?.let { goal ->
                        repository.updateGoal(goal.copy(progressPercent = parentGoalProgress))
                    }
                }
            }
        }
    }

    fun markMilestoneAsComplete(milestoneId: Int) {
        viewModelScope.launch {
            repository.getMilestoneById(milestoneId)?.let { m ->
                repository.updateMilestone(m.copy(status = "COMPLETED"))
                rewardXpAndFocus(200, 25f)
                triggerCelebration("Milestone: ${m.title}")
            }
        }
    }

    // Tuning Archetype
    fun setCoachPersonality(name: String) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                repository.updateUserProfile(profile.copy(coachPersonality = name))
            }
        }
    }

    // Reset All System Core Data
    fun resetSystemData() {
        viewModelScope.launch {
            repository.resetAllData()
            navigateTo("TODAY")
        }
    }

    // Utility: XP and Level-up Logic
    private suspend fun rewardXpAndFocus(xpReward: Int, focusReward: Float) {
        userProfile.value?.let { profile ->
            var newXp = profile.xp + xpReward
            var currentLevel = profile.level
            var maxXp = profile.maxXp

            // Level Up logic
            while (newXp >= maxXp) {
                newXp -= maxXp
                currentLevel += 1
                maxXp = (maxXp * 1.2).toInt() // Incremental difficulty
            }

            repository.updateUserProfile(
                profile.copy(
                    level = currentLevel,
                    xp = newXp,
                    maxXp = maxXp,
                    focusPoints = profile.focusPoints + focusReward
                )
            )
        }
    }
}
