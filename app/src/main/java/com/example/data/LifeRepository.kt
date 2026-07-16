package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class LifeRepository(private val lifeDao: LifeDao) {

    val userProfile: Flow<UserProfileEntity?> = lifeDao.getUserProfile()
    val allTasks: Flow<List<TaskEntity>> = lifeDao.getAllTasks()
    val allHabits: Flow<List<HabitEntity>> = lifeDao.getAllHabits()
    val allGoals: Flow<List<GoalEntity>> = lifeDao.getAllGoals()

    fun getTasksByDate(date: String): Flow<List<TaskEntity>> = lifeDao.getTasksByDate(date)
    fun getMilestonesForGoal(goalId: Int): Flow<List<MilestoneEntity>> = lifeDao.getMilestonesForGoal(goalId)
    fun getSubTasksForMilestone(milestoneId: Int): Flow<List<SubTaskEntity>> = lifeDao.getSubTasksForMilestone(milestoneId)

    suspend fun insertUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        lifeDao.insertUserProfile(profile)
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        lifeDao.updateUserProfile(profile)
    }

    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        lifeDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        lifeDao.deleteTask(task)
    }

    suspend fun insertHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        lifeDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        lifeDao.updateHabit(habit)
    }

    suspend fun insertGoal(goal: GoalEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        lifeDao.updateGoal(goal)
    }

    suspend fun getMilestoneById(id: Int): MilestoneEntity? = withContext(Dispatchers.IO) {
        lifeDao.getMilestoneById(id)
    }

    suspend fun updateMilestone(milestone: MilestoneEntity) = withContext(Dispatchers.IO) {
        lifeDao.updateMilestone(milestone)
    }

    suspend fun insertMilestone(milestone: MilestoneEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertMilestone(milestone)
    }

    suspend fun insertSubTask(subTask: SubTaskEntity) = withContext(Dispatchers.IO) {
        lifeDao.insertSubTask(subTask)
    }

    suspend fun updateSubTask(subTask: SubTaskEntity) = withContext(Dispatchers.IO) {
        lifeDao.updateSubTask(subTask)
    }

    suspend fun deleteSubTask(subTask: SubTaskEntity) = withContext(Dispatchers.IO) {
        lifeDao.deleteSubTask(subTask)
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        lifeDao.clearAllTasks()
        lifeDao.clearAllGoals()
        // Re-populate with defaults
        populateDefaultData()
    }

    suspend fun populateDefaultData() = withContext(Dispatchers.IO) {
        // 1. Profile Default
        val profile = UserProfileEntity(
            id = 1,
            name = "Julian Thorne",
            level = 14,
            xp = 12450,
            maxXp = 15000,
            focusPoints = 48200f,
            streak = 24,
            uptime = 94,
            rankPercent = 2,
            coachPersonality = "The Stoic Mentor",
            currentVibe = "Deep Work & Clarity"
        )
        lifeDao.insertUserProfile(profile)

        // 2. Habits Defaults
        val habits = listOf(
            HabitEntity(name = "Drink Water", currentValue = 2.1f, targetValue = 3.0f, unit = "L", isCompleted = false, iconName = "water_drop", streak = 12),
            HabitEntity(name = "Read", currentValue = 0f, targetValue = 30f, unit = "min", isCompleted = false, iconName = "menu_book", streak = 8),
            HabitEntity(name = "Meditate", currentValue = 10f, targetValue = 10f, unit = "Done", isCompleted = true, iconName = "self_improvement", streak = 15),
            HabitEntity(name = "Workout", currentValue = 25f, targetValue = 45f, unit = "min", isCompleted = false, iconName = "fitness_center", streak = 24)
        )
        for (h in habits) {
            lifeDao.insertHabit(h)
        }

        // 3. Tasks Defaults
        val tasks = listOf(
            TaskEntity(title = "Project Alpha Strategy", category = "WORK", timeSlot = "09:00 - 10:30 AM", description = "Q4 planning session with the engineering lead. High cognitive demand.", isCompleted = false, isRollover = false, date = "2024-10-24", durationHours = 2, location = "Conference Room A"),
            TaskEntity(title = "Deep Health Session", category = "HEALTH", timeSlot = "11:00 - 12:00 PM", description = "Active conditioning and strength circuit training.", isCompleted = false, isRollover = false, date = "2024-10-24", durationHours = 1, location = "Gym Floor"),
            TaskEntity(title = "Client Sync", category = "REPLY", timeSlot = "01:30 PM", description = "Reviewing the Q4 roadmap with the Stakeholder group.", isCompleted = false, isRollover = true, date = "2024-10-24", durationHours = 1, location = "Slack / Zoom"),
            TaskEntity(title = "Expense Audit", category = "ADMIN", timeSlot = "04:00 PM", description = "Automated audit log review of SaaS subscriptions.", isCompleted = false, isRollover = false, date = "2024-10-24", durationHours = 1, location = "Local First Dashboard"),
            // Let's add some for dynamic dates to play around
            TaskEntity(title = "System Core Sync", category = "WORK", timeSlot = "10:00 AM", description = "Review product roadmap and engineering team velocity.", isCompleted = false, isRollover = false, date = "2024-10-25", durationHours = 1, location = "Main Studio"),
            TaskEntity(title = "Deep Work Focus", category = "WORK", timeSlot = "09:00 AM", description = "Implement engine refactoring schemas.", isCompleted = false, isRollover = false, date = "2024-10-25", durationHours = 2, location = "Sleek Workspace")
        )
        for (t in tasks) {
            lifeDao.insertTask(t)
        }

        // 4. Goals & Milestones Defaults
        val goalId1 = lifeDao.insertGoal(
            GoalEntity(
                title = "Buy a Motorcycle",
                targetTimeline = "Est. 6 Months",
                domain = "Wealth",
                horizon = "Quarterly",
                progressPercent = 0,
                visionImage = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600&auto=format&fit=crop&q=80"
            )
        ).toInt()

        val goalId2 = lifeDao.insertGoal(
            GoalEntity(
                title = "Senior Software Architect",
                targetTimeline = "5 Years",
                domain = "Career",
                horizon = "Yearly",
                progressPercent = 14,
                visionImage = "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=600&auto=format&fit=crop&q=80"
            )
        ).toInt()

        // Milestones for Goal 1 (Motorcycle)
        val m1 = lifeDao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Save $5k", description = "Establish a dedicated high-yield savings account for the bike fund and automate monthly transfers.", status = "ACTIVE", dueDate = "Due in 3 mo", iconName = "payments")).toInt()
        lifeDao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Complete Riding Course", description = "Enroll in the Basic RiderCourse (BRC) to gain essential skills and earn your license waiver.", status = "LOCKED", iconName = "sports_motorsports"))
        lifeDao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Purchase Safety Gear", description = "Invest in ECE-rated helmet, armored jacket, gloves, and protective boots before the bike purchase.", status = "LOCKED", iconName = "shield"))
        lifeDao.insertMilestone(MilestoneEntity(goalId = goalId1, title = "Final Purchase", description = "Visit local dealers, test ride selected models, and finalize the purchase of your first motorcycle.", status = "LOCKED", iconName = "two_wheeler"))

        // Milestones for Goal 2 (Senior Software Architect)
        val m5 = lifeDao.insertMilestone(MilestoneEntity(goalId = goalId2, title = "Master System Design", description = "Phase 1 • 6 Months", status = "ACTIVE", iconName = "architecture")).toInt()
        lifeDao.insertMilestone(MilestoneEntity(goalId = goalId2, title = "Lead a Major Project", description = "Phase 2 • 12 Months", status = "LOCKED", iconName = "groups"))
        lifeDao.insertMilestone(MilestoneEntity(goalId = goalId2, title = "Open Source Contribution", description = "Phase 3 • 4 Months", status = "LOCKED", iconName = "terminal"))
        lifeDao.insertMilestone(MilestoneEntity(goalId = goalId2, title = "Architect Certification", description = "Phase 4 • 3 Months", status = "LOCKED", iconName = "workspace_premium"))

        // Add sub tasks for the active Master System Design milestone (m5) or general checklist demo
        val mOverhaul = lifeDao.insertMilestone(MilestoneEntity(goalId = goalId2, title = "Architectural System Overhaul", description = "Implement the core V2 engine logic to handle high-concurrency event streams and optimize database normalization.", status = "ACTIVE", dueDate = "DUE IN 4 DAYS", iconName = "terminal")).toInt()
        lifeDao.insertSubTask(SubTaskEntity(milestoneId = mOverhaul, title = "Design updated event interface", isCompleted = true))
        lifeDao.insertSubTask(SubTaskEntity(milestoneId = mOverhaul, title = "Prototype data migration script", isCompleted = true))
        lifeDao.insertSubTask(SubTaskEntity(milestoneId = mOverhaul, title = "Refactor schema for V2 normalization", isCompleted = false))
        lifeDao.insertSubTask(SubTaskEntity(milestoneId = mOverhaul, title = "Stress test concurrency limits", isCompleted = false))
    }
}
