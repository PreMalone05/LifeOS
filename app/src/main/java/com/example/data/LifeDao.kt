package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeDao {
    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY timeSlot ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY timeSlot ASC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    // Habits
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()

    @Query("DELETE FROM milestones")
    suspend fun clearAllMilestones()

    @Query("DELETE FROM sub_tasks")
    suspend fun clearAllSubTasks()

    // Goals
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Int): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun clearAllGoals()

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    // Milestones
    @Query("SELECT * FROM milestones WHERE goalId = :goalId")
    fun getMilestonesForGoal(goalId: Int): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE id = :id LIMIT 1")
    suspend fun getMilestoneById(id: Int): MilestoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    // Sub Tasks
    @Query("SELECT * FROM sub_tasks WHERE milestoneId = :milestoneId")
    fun getSubTasksForMilestone(milestoneId: Int): Flow<List<SubTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity)

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)

    @Delete
    suspend fun deleteSubTask(subTask: SubTaskEntity)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // Recurring Alarms
    @Query("SELECT * FROM recurring_alarms ORDER BY hour ASC, minute ASC")
    fun getAllRecurringAlarms(): Flow<List<RecurringAlarmEntity>>

    @Query("SELECT * FROM recurring_alarms WHERE isEnabled = 1")
    suspend fun getEnabledRecurringAlarms(): List<RecurringAlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringAlarm(alarm: RecurringAlarmEntity): Long

    @Update
    suspend fun updateRecurringAlarm(alarm: RecurringAlarmEntity)

    @Delete
    suspend fun deleteRecurringAlarm(alarm: RecurringAlarmEntity)

    // Daily Reviews
    @Query("SELECT * FROM daily_reviews ORDER BY timestamp DESC")
    fun getAllDailyReviews(): Flow<List<DailyReviewEntity>>

    @Query("SELECT * FROM daily_reviews WHERE date = :date LIMIT 1")
    suspend fun getDailyReviewForDate(date: String): DailyReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyReview(review: DailyReviewEntity): Long

    @Update
    suspend fun updateTasks(tasks: List<TaskEntity>)

    // Phase 7: Behavioral Events
    @Query("SELECT * FROM behavioral_events ORDER BY timestamp DESC")
    fun getAllBehavioralEvents(): Flow<List<BehavioralEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBehavioralEvent(event: BehavioralEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBehavioralEvents(events: List<BehavioralEventEntity>)

    @Query("SELECT * FROM behavioral_events WHERE eventType = :eventType")
    suspend fun getEventsByType(eventType: String): List<BehavioralEventEntity>

    @Query("DELETE FROM behavioral_events")
    suspend fun clearBehavioralEvents()

    // Phase 7: Task Performance Records
    @Query("SELECT * FROM task_performance_records ORDER BY completedTimestamp DESC")
    fun getAllTaskPerformanceRecords(): Flow<List<TaskPerformanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskPerformanceRecord(record: TaskPerformanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskPerformanceRecords(records: List<TaskPerformanceRecordEntity>)

    @Query("SELECT * FROM task_performance_records WHERE category = :category")
    suspend fun getRecordsByCategory(category: String): List<TaskPerformanceRecordEntity>

    @Query("DELETE FROM task_performance_records")
    suspend fun clearTaskPerformanceRecords()

    // Phase 7: Recommendation Feedback
    @Query("SELECT * FROM recommendation_feedback ORDER BY timestamp DESC")
    fun getAllRecommendationFeedback(): Flow<List<RecommendationFeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendationFeedback(feedback: RecommendationFeedbackEntity): Long

    @Query("DELETE FROM recommendation_feedback")
    suspend fun clearRecommendationFeedback()

    // Phase 7: Learned Patterns
    @Query("SELECT * FROM learned_patterns")
    fun getAllLearnedPatterns(): Flow<List<LearnedPatternEntity>>

    @Query("SELECT * FROM learned_patterns WHERE patternKey = :key LIMIT 1")
    suspend fun getLearnedPatternByKey(key: String): LearnedPatternEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearnedPattern(pattern: LearnedPatternEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearnedPatterns(patterns: List<LearnedPatternEntity>)

    // Phase 8: Predictive Recommendations
    @Query("SELECT * FROM predictive_recommendations WHERE state != 'DISMISSED' AND state != 'EXPIRED' ORDER BY createdTimestamp DESC")
    fun getActivePredictiveRecommendations(): Flow<List<PredictiveRecommendationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredictiveRecommendation(recommendation: PredictiveRecommendationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredictiveRecommendations(recommendations: List<PredictiveRecommendationEntity>)

    @Query("UPDATE predictive_recommendations SET state = :newState WHERE id = :id")
    suspend fun updateRecommendationState(id: String, newState: String)

    @Query("DELETE FROM predictive_recommendations")
    suspend fun clearPredictiveRecommendations()

    // Phase 9: Predictive Notification History & Reliability Hardening
    @Query("SELECT MAX(dispatchedTimestamp) FROM predictive_notification_history WHERE recommendationType = :type")
    suspend fun getLastDispatchedTimestampForType(type: String): Long?

    @Query("SELECT MAX(dispatchedTimestamp) FROM predictive_notification_history WHERE deduplicationHash = :hash")
    suspend fun getLastDispatchedTimestampForHash(hash: String): Long?

    @Query("SELECT COUNT(*) FROM predictive_notification_history WHERE dispatchedTimestamp >= :startOfDayTimestamp")
    suspend fun getDispatchedCountSince(startOfDayTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationHistory(history: PredictiveNotificationHistoryEntity): Long

    @Query("DELETE FROM predictive_notification_history")
    suspend fun clearNotificationHistory()

    @Query("DELETE FROM predictive_notification_history WHERE dispatchedTimestamp < :cutoffTimestamp")
    suspend fun pruneOldNotificationHistory(cutoffTimestamp: Long)

    // ==========================================
    // Local Backup & Atomic Restore Operations
    // ==========================================

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfileEntity?

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    // Tasks
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksList(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    // Habits
    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsList(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    // Goals
    @Query("SELECT * FROM goals")
    suspend fun getAllGoalsList(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    // Milestones
    @Query("SELECT * FROM milestones")
    suspend fun getAllMilestonesList(): List<MilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)

    // Sub Tasks
    @Query("SELECT * FROM sub_tasks")
    suspend fun getAllSubTasksList(): List<SubTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTasks(subTasks: List<SubTaskEntity>)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getAllChatMessagesList(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    // Recurring Alarms
    @Query("SELECT * FROM recurring_alarms")
    suspend fun getAllRecurringAlarmsList(): List<RecurringAlarmEntity>

    @Query("DELETE FROM recurring_alarms")
    suspend fun clearAllRecurringAlarms()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringAlarms(alarms: List<RecurringAlarmEntity>)

    // Daily Reviews
    @Query("SELECT * FROM daily_reviews ORDER BY timestamp DESC")
    suspend fun getAllDailyReviewsList(): List<DailyReviewEntity>

    @Query("DELETE FROM daily_reviews")
    suspend fun clearAllDailyReviews()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyReviews(reviews: List<DailyReviewEntity>)

    // Behavioral Events
    @Query("SELECT * FROM behavioral_events")
    suspend fun getAllBehavioralEventsList(): List<BehavioralEventEntity>

    // Task Performance Records
    @Query("SELECT * FROM task_performance_records")
    suspend fun getAllTaskPerformanceRecordsList(): List<TaskPerformanceRecordEntity>

    // Recommendation Feedback
    @Query("SELECT * FROM recommendation_feedback")
    suspend fun getAllRecommendationFeedbackList(): List<RecommendationFeedbackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendationFeedbacks(feedbacks: List<RecommendationFeedbackEntity>)

    // Learned Patterns
    @Query("SELECT * FROM learned_patterns")
    suspend fun getAllLearnedPatternsList(): List<LearnedPatternEntity>

    @Query("DELETE FROM learned_patterns")
    suspend fun clearAllLearnedPatterns()

    // Predictive Recommendations
    @Query("SELECT * FROM predictive_recommendations")
    suspend fun getAllPredictiveRecommendationsList(): List<PredictiveRecommendationEntity>

    // Predictive Notification History
    @Query("SELECT * FROM predictive_notification_history")
    suspend fun getAllNotificationHistoryList(): List<PredictiveNotificationHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationHistories(histories: List<PredictiveNotificationHistoryEntity>)
}
