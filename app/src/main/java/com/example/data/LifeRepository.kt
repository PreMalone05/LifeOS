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
    val allChatMessages: Flow<List<ChatMessageEntity>> = lifeDao.getAllChatMessages()
    val allRecurringAlarms: Flow<List<RecurringAlarmEntity>> = lifeDao.getAllRecurringAlarms()
    val allDailyReviews: Flow<List<DailyReviewEntity>> = lifeDao.getAllDailyReviews()
    val allBehavioralEvents: Flow<List<BehavioralEventEntity>> = lifeDao.getAllBehavioralEvents()
    val allTaskPerformanceRecords: Flow<List<TaskPerformanceRecordEntity>> = lifeDao.getAllTaskPerformanceRecords()
    val allRecommendationFeedback: Flow<List<RecommendationFeedbackEntity>> = lifeDao.getAllRecommendationFeedback()
    val allLearnedPatterns: Flow<List<LearnedPatternEntity>> = lifeDao.getAllLearnedPatterns()
    val activePredictiveRecommendations: Flow<List<PredictiveRecommendationEntity>> = lifeDao.getActivePredictiveRecommendations()

    suspend fun insertPredictiveRecommendation(recommendation: PredictiveRecommendationEntity) = withContext(Dispatchers.IO) {
        lifeDao.insertPredictiveRecommendation(recommendation)
    }

    suspend fun insertPredictiveRecommendations(recommendations: List<PredictiveRecommendationEntity>) = withContext(Dispatchers.IO) {
        lifeDao.insertPredictiveRecommendations(recommendations)
    }

    suspend fun updateRecommendationState(id: String, newState: String) = withContext(Dispatchers.IO) {
        lifeDao.updateRecommendationState(id, newState)
    }

    suspend fun clearPredictiveRecommendations() = withContext(Dispatchers.IO) {
        lifeDao.clearPredictiveRecommendations()
    }

    suspend fun insertBehavioralEvent(event: BehavioralEventEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertBehavioralEvent(event)
    }

    suspend fun insertBehavioralEvents(events: List<BehavioralEventEntity>) = withContext(Dispatchers.IO) {
        lifeDao.insertBehavioralEvents(events)
    }

    suspend fun insertTaskPerformanceRecord(record: TaskPerformanceRecordEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertTaskPerformanceRecord(record)
    }

    suspend fun insertTaskPerformanceRecords(records: List<TaskPerformanceRecordEntity>) = withContext(Dispatchers.IO) {
        lifeDao.insertTaskPerformanceRecords(records)
    }

    suspend fun insertRecommendationFeedback(feedback: RecommendationFeedbackEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertRecommendationFeedback(feedback)
    }

    suspend fun insertLearnedPattern(pattern: LearnedPatternEntity) = withContext(Dispatchers.IO) {
        lifeDao.insertLearnedPattern(pattern)
    }

    suspend fun insertLearnedPatterns(patterns: List<LearnedPatternEntity>) = withContext(Dispatchers.IO) {
        lifeDao.insertLearnedPatterns(patterns)
    }

    suspend fun getLearnedPatternByKey(key: String): LearnedPatternEntity? = withContext(Dispatchers.IO) {
        lifeDao.getLearnedPatternByKey(key)
    }

    suspend fun insertDailyReview(review: DailyReviewEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertDailyReview(review)
    }

    suspend fun getDailyReviewForDate(date: String): DailyReviewEntity? = withContext(Dispatchers.IO) {
        lifeDao.getDailyReviewForDate(date)
    }

    suspend fun updateTasks(tasks: List<TaskEntity>) = withContext(Dispatchers.IO) {
        lifeDao.updateTasks(tasks)
    }

    suspend fun insertRecurringAlarm(alarm: RecurringAlarmEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertRecurringAlarm(alarm)
    }

    suspend fun updateRecurringAlarm(alarm: RecurringAlarmEntity) = withContext(Dispatchers.IO) {
        lifeDao.updateRecurringAlarm(alarm)
    }

    suspend fun deleteRecurringAlarm(alarm: RecurringAlarmEntity) = withContext(Dispatchers.IO) {
        lifeDao.deleteRecurringAlarm(alarm)
    }

    suspend fun insertChatMessage(message: ChatMessageEntity): Long = withContext(Dispatchers.IO) {
        lifeDao.insertChatMessage(message)
    }

    suspend fun clearChatMessages() = withContext(Dispatchers.IO) {
        lifeDao.clearChatMessages()
    }

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

    suspend fun deleteHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        lifeDao.deleteHabit(habit)
    }

    suspend fun deleteGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        lifeDao.deleteGoal(goal)
    }

    suspend fun clearAllSystemData() = withContext(Dispatchers.IO) {
        lifeDao.clearAllTasks()
        lifeDao.clearAllGoals()
        lifeDao.clearAllHabits()
        lifeDao.clearAllMilestones()
        lifeDao.clearAllSubTasks()
        lifeDao.clearChatMessages()
        lifeDao.clearBehavioralEvents()
        lifeDao.clearTaskPerformanceRecords()
        lifeDao.clearRecommendationFeedback()
        lifeDao.clearPredictiveRecommendations()
        lifeDao.clearNotificationHistory()
        val profile = UserProfileEntity(
            id = 1,
            userId = "local_user",
            name = "",
            level = 1,
            xp = 0,
            maxXp = 1000,
            focusPoints = 0f,
            streak = 0,
            uptime = 100,
            rankPercent = 100,
            coachPersonality = "The Stoic Mentor",
            currentVibe = "Focused & Intentional",
            isGoogleLinked = false,
            isOnboarded = false,
            selectedInterests = "",
            planningStyle = "Time Blocking + Tasks",
            focusSummary = "",
            priorityStatement = "",
            availabilityWindow = "",
            reminderIntensity = "Balanced"
        )
        lifeDao.insertUserProfile(profile)
    }
}
