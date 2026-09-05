package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LifeViewModel(application: Application) : AndroidViewModel(application) {

    // Pomodoro Timer States
    private val _timeLeftSeconds = MutableStateFlow(25 * 60)
    val timeLeftSeconds: StateFlow<Int> = _timeLeftSeconds.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(25 * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _currentTimerMode = MutableStateFlow(PomodoroMode.WORK)
    val currentTimerMode: StateFlow<PomodoroMode> = _currentTimerMode.asStateFlow()

    private val _completedRounds = MutableStateFlow(0)
    val completedRounds: StateFlow<Int> = _completedRounds.asStateFlow()

    private val _focusTarget = MutableStateFlow("")
    val focusTarget: StateFlow<String> = _focusTarget.asStateFlow()

    private val _aiEncouragement = MutableStateFlow("")
    val aiEncouragement: StateFlow<String> = _aiEncouragement.asStateFlow()

    private val _isFetchingEncouragement = MutableStateFlow(false)
    val isFetchingEncouragement: StateFlow<Boolean> = _isFetchingEncouragement.asStateFlow()

    private var timerJob: Job? = null

    // White Noise Sound States
    private val noisePlayer = com.example.audio.NoisePlayer(application)

    private val _selectedNoise = MutableStateFlow(com.example.audio.NoiseType.OFF)
    val selectedNoise: StateFlow<com.example.audio.NoiseType> = _selectedNoise.asStateFlow()

    private val _noiseVolume = MutableStateFlow(0.5f)
    val noiseVolume: StateFlow<Float> = _noiseVolume.asStateFlow()

    // Smart Notification States
    private val _smartNotificationsEnabled = MutableStateFlow(true)
    val smartNotificationsEnabled: StateFlow<Boolean> = _smartNotificationsEnabled.asStateFlow()

    private val _pomodoroNotificationsEnabled = MutableStateFlow(true)
    val pomodoroNotificationsEnabled: StateFlow<Boolean> = _pomodoroNotificationsEnabled.asStateFlow()

    private val _habitNotificationsEnabled = MutableStateFlow(true)
    val habitNotificationsEnabled: StateFlow<Boolean> = _habitNotificationsEnabled.asStateFlow()

    private val _isGeneratingNotification = MutableStateFlow(false)
    val isGeneratingNotification: StateFlow<Boolean> = _isGeneratingNotification.asStateFlow()

    // Calendar Integration States
    private val _calendarEvents = MutableStateFlow<List<com.example.data.CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<com.example.data.CalendarEvent>> = _calendarEvents.asStateFlow()

    private val _calendarPermissionGranted = MutableStateFlow(false)
    val calendarPermissionGranted: StateFlow<Boolean> = _calendarPermissionGranted.asStateFlow()

    private val _isLoadingCalendarEvents = MutableStateFlow(false)
    val isLoadingCalendarEvents: StateFlow<Boolean> = _isLoadingCalendarEvents.asStateFlow()

    // Morning Briefing States
    private val _morningBriefing = MutableStateFlow<String?>(null)
    val morningBriefing: StateFlow<String?> = _morningBriefing.asStateFlow()

    private val _isLoadingMorningBriefing = MutableStateFlow(false)
    val isLoadingMorningBriefing: StateFlow<Boolean> = _isLoadingMorningBriefing.asStateFlow()

    // Chat States
    private val _chatMessages = MutableStateFlow<List<com.example.data.ChatMessageEntity>>(emptyList())
    val chatMessages: StateFlow<List<com.example.data.ChatMessageEntity>> = _chatMessages.asStateFlow()

    private val _isSendingChatMessage = MutableStateFlow(false)
    val isSendingChatMessage: StateFlow<Boolean> = _isSendingChatMessage.asStateFlow()

    private val repository: LifeRepository
    private val backupManager: BackupManager

    // Local Backup & Restore states
    private val _isBackupOperating = MutableStateFlow(false)
    val isBackupOperating: StateFlow<Boolean> = _isBackupOperating.asStateFlow()

    private val _backupOperationResult = MutableStateFlow<BackupResult?>(null)
    val backupOperationResult: StateFlow<BackupResult?> = _backupOperationResult.asStateFlow()

    // AI Insight states
    private val _todayInsight = MutableStateFlow("Analyzing your dashboard focus density and active streaks...")
    val todayInsight: StateFlow<String> = _todayInsight.asStateFlow()

    private val _isLoadingInsight = MutableStateFlow(false)
    val isLoadingInsight: StateFlow<Boolean> = _isLoadingInsight.asStateFlow()

    private val _insightsAIEvaluation = MutableStateFlow<String?>("Analyzing your productivity trends and task categories to generate localized efficiency optimizations...")
    val insightsAIEvaluation: StateFlow<String?> = _insightsAIEvaluation.asStateFlow()

    private val _isLoadingInsightsAI = MutableStateFlow(false)
    val isLoadingInsightsAI: StateFlow<Boolean> = _isLoadingInsightsAI.asStateFlow()

    private val _milestoneEvaluation = MutableStateFlow<String?>(null)
    val milestoneEvaluation: StateFlow<String?> = _milestoneEvaluation.asStateFlow()

    private val _isLoadingEvaluation = MutableStateFlow(false)
    val isLoadingEvaluation: StateFlow<Boolean> = _isLoadingEvaluation.asStateFlow()

    // AI Roadmap Interview state
    private val _interviewQuestions = MutableStateFlow<List<InterviewQuestion>>(emptyList())
    val interviewQuestions: StateFlow<List<InterviewQuestion>> = _interviewQuestions.asStateFlow()

    private val _isFetchingQuestions = MutableStateFlow(false)
    val isFetchingQuestions: StateFlow<Boolean> = _isFetchingQuestions.asStateFlow()

    private val _isGeneratingTailoredRoadmap = MutableStateFlow(false)
    val isGeneratingTailoredRoadmap: StateFlow<Boolean> = _isGeneratingTailoredRoadmap.asStateFlow()

    private val _isRewritingMilestone = MutableStateFlow(false)
    val isRewritingMilestone: StateFlow<Boolean> = _isRewritingMilestone.asStateFlow()

    private val _isGeneratingPhaseDailyTasks = MutableStateFlow(false)
    val isGeneratingPhaseDailyTasks: StateFlow<Boolean> = _isGeneratingPhaseDailyTasks.asStateFlow()

    private val _lastGeneratedTaskCount = MutableStateFlow(0)
    val lastGeneratedTaskCount: StateFlow<Int> = _lastGeneratedTaskCount.asStateFlow()

    // Onboarding State
    private val _onboardingStep = MutableStateFlow(OnboardingStep.WELCOME)
    val onboardingStep: StateFlow<OnboardingStep> = _onboardingStep.asStateFlow()

    private val _selectedOnboardingInterests = MutableStateFlow<Set<String>>(emptySet())
    val selectedOnboardingInterests: StateFlow<Set<String>> = _selectedOnboardingInterests.asStateFlow()

    private val _customInterestInput = MutableStateFlow("")
    val customInterestInput: StateFlow<String> = _customInterestInput.asStateFlow()

    private val _currentAdaptiveQuestion = MutableStateFlow<AdaptiveInterviewQuestion?>(null)
    val currentAdaptiveQuestion: StateFlow<AdaptiveInterviewQuestion?> = _currentAdaptiveQuestion.asStateFlow()

    private val _interviewHistoryList = MutableStateFlow<List<InterviewHistoryItem>>(emptyList())
    val interviewHistoryList: StateFlow<List<InterviewHistoryItem>> = _interviewHistoryList.asStateFlow()

    private val _isThinkingInterview = MutableStateFlow(false)
    val isThinkingInterview: StateFlow<Boolean> = _isThinkingInterview.asStateFlow()

    private val _interviewError = MutableStateFlow<String?>(null)
    val interviewError: StateFlow<String?> = _interviewError.asStateFlow()

    private val _generatedPlannerConfig = MutableStateFlow<PersonalizedPlannerConfig?>(null)
    val generatedPlannerConfig: StateFlow<PersonalizedPlannerConfig?> = _generatedPlannerConfig.asStateFlow()

    private val _isApplyingPlan = MutableStateFlow(false)
    val isApplyingPlan: StateFlow<Boolean> = _isApplyingPlan.asStateFlow()

    private val _isSigningInWithGoogle = MutableStateFlow(false)
    val isSigningInWithGoogle: StateFlow<Boolean> = _isSigningInWithGoogle.asStateFlow()

    private val _googleSignInError = MutableStateFlow<String?>(null)
    val googleSignInError: StateFlow<String?> = _googleSignInError.asStateFlow()

    // Phase 6: Adaptive Capacity & Intelligence States
    private val _capacityReport = MutableStateFlow<AdaptiveCapacityReport?>(null)
    val capacityReport: StateFlow<AdaptiveCapacityReport?> = _capacityReport.asStateFlow()

    private val _isLoadingCapacity = MutableStateFlow(false)
    val isLoadingCapacity: StateFlow<Boolean> = _isLoadingCapacity.asStateFlow()

    // Phase 6: Interactive Rebalance ("My Day Changed")
    private val _rebalanceResult = MutableStateFlow<AdaptiveRebalanceResult?>(null)
    val rebalanceResult: StateFlow<AdaptiveRebalanceResult?> = _rebalanceResult.asStateFlow()

    private val _isRebalancing = MutableStateFlow(false)
    val isRebalancing: StateFlow<Boolean> = _isRebalancing.asStateFlow()

    private val _showRebalanceDialog = MutableStateFlow(false)
    val showRebalanceDialog: StateFlow<Boolean> = _showRebalanceDialog.asStateFlow()

    // Phase 6: Evening Review & Reflection
    private val _eveningReviewSummary = MutableStateFlow<EveningReviewSummary?>(null)
    val eveningReviewSummary: StateFlow<EveningReviewSummary?> = _eveningReviewSummary.asStateFlow()

    private val _isLoadingEveningReview = MutableStateFlow(false)
    val isLoadingEveningReview: StateFlow<Boolean> = _isLoadingEveningReview.asStateFlow()

    private val _showEveningReviewDialog = MutableStateFlow(false)
    val showEveningReviewDialog: StateFlow<Boolean> = _showEveningReviewDialog.asStateFlow()

    // Phase 7.5: Multi-Provider AI Engine States
    val aiManager: com.example.ai.AiManager = com.example.ai.AiManager.initialize(application)

    private val _activeAiProviderId = MutableStateFlow(aiManager.keyStorage.getActiveProvider())
    val activeAiProviderId: StateFlow<String> = _activeAiProviderId.asStateFlow()

    private val _activeAiModelId = MutableStateFlow(aiManager.keyStorage.getActiveModel(aiManager.keyStorage.getActiveProvider()))
    val activeAiModelId: StateFlow<String> = _activeAiModelId.asStateFlow()

    private val _aiConnectionResult = MutableStateFlow<com.example.ai.ConnectionTestResult?>(null)
    val aiConnectionResult: StateFlow<com.example.ai.ConnectionTestResult?> = _aiConnectionResult.asStateFlow()

    private val _isTestingAiConnection = MutableStateFlow(false)
    val isTestingAiConnection: StateFlow<Boolean> = _isTestingAiConnection.asStateFlow()

    private val _customAiEndpoint = MutableStateFlow(aiManager.keyStorage.getEndpointUrl(com.example.ai.ProviderType.CUSTOM.id))
    val customAiEndpoint: StateFlow<String> = _customAiEndpoint.asStateFlow()

    init {
        val database = LifeDatabase.getDatabase(application)
        repository = LifeRepository(database.lifeDao())
        backupManager = BackupManager(database)

        // Check user profile and onboarded state
        viewModelScope.launch {
            val profile = repository.userProfile.firstOrNull()
            if (profile == null) {
                val freshProfile = UserProfileEntity(
                    id = 1,
                    userId = "local_user",
                    name = "",
                    level = 1,
                    xp = 0,
                    streak = 0,
                    focusPoints = 0f,
                    uptime = 100,
                    rankPercent = 100,
                    coachPersonality = "The Stoic Mentor",
                    currentVibe = "Focused & Intentional",
                    isGoogleLinked = false,
                    isOnboarded = false
                )
                repository.insertUserProfile(freshProfile)
                _currentScreen.value = "ONBOARDING"
                _onboardingStep.value = OnboardingStep.WELCOME
            } else if (!profile.isOnboarded) {
                _currentScreen.value = "ONBOARDING"
                _onboardingStep.value = OnboardingStep.WELCOME
            } else {
                _currentScreen.value = "TODAY"
            }
            
            // Check calendar permission and load calendar events
            val hasCal = androidx.core.content.ContextCompat.checkSelfPermission(
                application,
                android.Manifest.permission.READ_CALENDAR
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            setCalendarPermissionGranted(hasCal)
            if (hasCal) {
                loadCalendarEvents(getTodayDateString())
            }

            // Generate dynamic insights if onboarded
            if (profile?.isOnboarded == true) {
                generateTodayInsight()
                generateInsightsAI()
                generateMorningBriefing(getTodayDateString())
                generatePersonalizedInsightsAI()
            }
        }

        // Collect and sync Focus Timer state with Foreground Service
        viewModelScope.launch {
            com.example.notification.FocusTimerService.isTimerRunning.collect { running ->
                _isTimerRunning.value = running
            }
        }
        viewModelScope.launch {
            com.example.notification.FocusTimerService.timeLeftSeconds.collect { seconds ->
                _timeLeftSeconds.value = seconds
            }
        }

        // Collect and sync Chat History, inserting a custom welcome message if empty
        viewModelScope.launch {
            repository.allChatMessages.collect { messages ->
                if (messages.isEmpty()) {
                    val profile = repository.userProfile.firstOrNull() ?: com.example.data.UserProfileEntity()
                    val coach = profile.coachPersonality
                    val name = profile.name
                    val welcomeText = when {
                        name.isBlank() -> "Hello! I am your AI Productivity Coach. Let us align your priorities, schedule, and habits to make today a success. How can I assist you?"
                        coach.contains("Stoic", ignoreCase = true) -> "Greetings, $name. I am your Stoic Mentor. Let us look at your obstacles today as raw material for wisdom and discipline. What is on your mind?"
                        coach.contains("Motivator", ignoreCase = true) -> "WHAT'S UP, $name?! 🚀 I'm your High-Energy Motivator, and we are here to absolutely CRUSH your targets today! Tell me what we're tackling first!"
                        coach.contains("Strategist", ignoreCase = true) -> "Hello, $name. I am your Analytical Strategist. Let's optimize your priorities, resolve any scheduling conflicts, and break down your objectives systematically. Where should we begin?"
                        else -> "Hello, $name. I am your AI Productivity Coach. Let us align your priorities, schedule, and habits to make today a success. How can I assist you?"
                    }
                    repository.insertChatMessage(com.example.data.ChatMessageEntity(role = "model", text = welcomeText))
                } else {
                    _chatMessages.value = messages
                }
            }
        }
    }

    fun generateTodayInsight() {
        viewModelScope.launch {
            _isLoadingInsight.value = true
            val profile = userProfile.value
            val tasks = allTasks.value
            val habits = allHabits.value
            val coach = profile?.coachPersonality ?: "The Stoic Mentor"
            val username = profile?.name?.takeIf { it.isNotBlank() } ?: "Champion"
            val streak = profile?.streak ?: 0
            
            val completedTasks = tasks.filter { it.isCompleted }.size
            val pendingTasks = tasks.filter { !it.isCompleted }.size
            val activeHabits = habits.joinToString { "${it.name} (${it.currentValue}/${it.targetValue} ${it.unit})" }

            val prompt = """
                You are $coach, an AI coach. Analyze the user's progress and write a concise, powerful 1-2 sentence daily insight.
                User: $username
                Coach Personality: $coach
                Current Streak: $streak days
                Today's Tasks: $completedTasks completed, $pendingTasks pending
                Active Habit Streaks: $activeHabits
                
                Keep the tone extremely aligned with $coach. Focus on the next immediate action, avoid generic platitudes, and keep the insight short, punchy, and highly tactical.
            """.trimIndent()

            val result = GeminiService.generateContent(prompt)
            _todayInsight.value = if (result.startsWith("Error:") || result.isBlank()) {
                "Stay locked in. Keep pushing today's tasks and build consistency. Remember: consistency is the compound interest of self-improvement."
            } else {
                result.replace("\"", "").trim()
            }
            _isLoadingInsight.value = false
        }
    }

    fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }

    fun generateMorningBriefing(dateString: String) {
        viewModelScope.launch {
            _isLoadingMorningBriefing.value = true
            try {
                val profile = userProfile.value ?: repository.userProfile.firstOrNull() ?: com.example.data.UserProfileEntity()
                val tasks = allTasks.value.filter { it.date == dateString }
                
                // Fetch calendar events for today using CalendarManager
                val calendarEventsList = if (_calendarPermissionGranted.value) {
                    com.example.data.CalendarManager.fetchEventsForDate(getApplication(), dateString)
                } else {
                    emptyList()
                }

                val coach = profile.coachPersonality
                val username = profile.name
                val currentVibe = profile.currentVibe
                val streak = profile.streak

                val tasksListStr = if (tasks.isNotEmpty()) {
                    tasks.joinToString("\n") { task ->
                        "- [${if (task.isCompleted) "Completed" else "Pending"}] ${task.title} | Category: ${task.category} | Time: ${task.timeSlot} | Duration: ${task.durationHours}h | Desc: ${task.description}"
                    }
                } else {
                    "No planned tasks for today."
                }

                val eventsListStr = if (calendarEventsList.isNotEmpty()) {
                    calendarEventsList.joinToString("\n") { event ->
                        "- Meeting/Event: ${event.title} | Time: ${event.formattedTime} | Location: ${event.location ?: "N/A"} | All Day: ${event.allDay}"
                    }
                } else {
                    "No local calendar events scheduled for today."
                }

                val todayStatus = getScheduleStatusForDate(dateString)
                val scheduleContextStr = buildString {
                    append("Today's Work Environment: ${todayStatus.label}\n")
                    append("Configured Work Hours: ${profile.workStartTime} to ${profile.workEndTime}\n")
                    append("WFH Days of Week: ${profile.wfhDays}\n")
                    append("Weekend Rest Days: ${profile.weekendDays}\n")
                    if (todayStatus.isVacation) {
                        append("VACATION STATUS: Active Vacation Mode (${profile.vacationNotes.ifBlank { "Personal Time Off" }}). Advise relaxing, low cognitive load, and streak protection.\n")
                    } else if (todayStatus.isWeekend) {
                        append("WEEKEND STATUS: Weekend Rest Day. Focus on rejuvenation, personal recovery, and casual habits.\n")
                    } else if (todayStatus.isWfh) {
                        append("WFH STATUS: Working From Home. Focus on deep work boundaries, minimizing home distractions, and taking intentional screen breaks.\n")
                    } else {
                        append("OFFICE STATUS: In-Office Workday. Focus on high-collaboration bandwidth, punctual commute buffers, and meeting execution.\n")
                    }
                }

                // Compile lightweight context summary (goals, habits + streaks, recent completions)
                val goals = allGoals.value
                val habits = allHabits.value
                val allTaskList = allTasks.value
                val contextSummary = com.example.ai.AiContextSummaryBuilder.buildSummary(
                    goals = goals,
                    habits = habits,
                    allTasks = allTaskList,
                    todayDate = dateString
                )

                val systemPrompt = """
                    You are $coach, an elite AI productivity advisor. Your task is to analyze the user's schedule (work hours, WFH/office mode, vacation status, local calendar events, and planned tasks) for today, connected to their active goals, habits + streaks, and recent progress, to synthesize a high-impact, elite "Morning Briefing" snippet. 
                    The user's display name is $username.
                    Their current mindset/vibe is "$currentVibe".
                    Keep the tone professional, motivating, and closely aligned with $coach.

                    $contextSummary

                    Focus on:
                    1. Acknowledging their work environment context (WFH vs Office vs Weekend vs Vacation) and work hours.
                    2. Synthesizing scheduled calendar events and planned tasks in relation to their overarching goals and habit momentum.
                    3. Highlighting task priorities and pointing out any potential schedule conflicts, timing overlaps, or tight gaps.
                    4. Giving a concise, highly strategic coaching suggestion (tactical, specific, no generic platitudes) for dominating the day.
                    Do not use markdown bold/asterisks or titles within the main briefing. Format with clean, readable spacing or bullet points if necessary.
                    Limit the output to around 80-120 words maximum.
                """.trimIndent()

                val userPrompt = """
                    Today's Date: $dateString
                    User Vibe: $currentVibe
                    Active Streak: $streak days
                    Level: ${profile.level} (XP: ${profile.xp}/${profile.maxXp})

                    Active Goals, Habits & Recent Progress:
                    $contextSummary

                    Schedule & Work Mode:
                    $scheduleContextStr

                    Planned Tasks for Today:
                    $tasksListStr

                    Local Calendar Events for Today:
                    $eventsListStr

                    Synthesize these inputs. Connect their daily tasks and habits to their overarching goals. Check for overlapping times or scheduling conflicts (e.g. a calendar meeting coinciding with a planned task duration). Emphasize high-priority work aligned with their WFH/Office/Vacation status and give me a sharp, motivating morning briefing snippet. Keep it short and actionable.
                """.trimIndent()

                val result = GeminiService.generateContent(userPrompt, systemPrompt)
                _morningBriefing.value = if (result.startsWith("Error:") || result.isBlank()) {
                    "Stay focused today! Connect your calendar and plan your priorities to generate a customized AI morning briefing."
                } else {
                    result.replace("\"", "").trim()
                }
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to generate morning briefing", e)
                _morningBriefing.value = "Welcome to your day! Get ready to crush your targets. Complete your daily priorities and build your focus streak."
            } finally {
                _isLoadingMorningBriefing.value = false
            }
        }
    }

    fun generateInsightsAI() {
        viewModelScope.launch {
            _isLoadingInsightsAI.value = true
            val profile = userProfile.value
            val tasks = allTasks.value
            val coach = profile?.coachPersonality ?: "The Stoic Mentor"
            val username = profile?.name ?: "Julian"

            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.isCompleted }
            val rolloverTasks = tasks.count { it.isRollover }
            
            val categoryCounts = tasks.groupBy { it.category }.map { "${it.key}: ${it.value.size} tasks" }.joinToString()

            val prompt = """
                You are $coach, an AI productivity coach. Review the user's task history and rollover counts.
                User: $username
                Total Registered Tasks: $totalTasks
                Completed Tasks: $completedTasks
                Rollover Tasks: $rolloverTasks (tasks that had to be rolled over to another day)
                Task Categories: $categoryCounts

                Provide a highly actionable, critical, and hyper-focused productivity analysis of 1-2 sentences. Tell them which category they are succeeding in or neglecting, how to optimize their rollover rate, and give a stoic, laser-focused instruction.
            """.trimIndent()

            val result = GeminiService.generateContent(prompt)
            _insightsAIEvaluation.value = if (result.startsWith("Error:") || result.isBlank()) {
                "Audit your daily rollover rates. You're completing tasks successfully, but over-scheduling is leading to friction. Focus on 2 core tasks daily and win."
            } else {
                result.replace("\"", "").trim()
            }
            _isLoadingInsightsAI.value = false
        }
    }

    fun generateMilestoneEvaluation(milestoneTitle: String, milestoneDesc: String, reflection: String) {
        viewModelScope.launch {
            _isLoadingEvaluation.value = true
            val profile = userProfile.value
            val coach = profile?.coachPersonality ?: "The Stoic Mentor"
            val username = profile?.name ?: "Julian"

            val prompt = """
                You are $coach, an AI coach. Evaluate the user's progress on their milestone and their written reflection.
                User: $username
                Milestone Title: $milestoneTitle
                Milestone Description: $milestoneDesc
                User's Reflection Log: "$reflection"

                Write a concise, high-impact evaluation of 2-3 sentences. Challenge any passive mindsets, praise real effort, and give them a highly motivational or stoic push forward in the exact voice of $coach.
            """.trimIndent()

            val result = GeminiService.generateContent(prompt)
            _milestoneEvaluation.value = if (result.startsWith("Error:") || result.isBlank()) {
                "The obstacle is the way. Your reflection shows self-awareness. Continue chipping away at the sub-tasks, execute with discipline, and let the results speak."
            } else {
                result.replace("\"", "").trim()
            }
            _isLoadingEvaluation.value = false
        }
    }

    fun clearMilestoneEvaluation() {
        _milestoneEvaluation.value = null
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

    val allRecurringAlarms: StateFlow<List<RecurringAlarmEntity>> = repository.allRecurringAlarms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDailyReviews: StateFlow<List<DailyReviewEntity>> = repository.allDailyReviews
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

    val selectedMilestone: StateFlow<MilestoneEntity?> = selectedMilestoneId
        .flatMapLatest { id ->
            if (id != null) {
                kotlinx.coroutines.flow.flow {
                    emit(repository.getMilestoneById(id))
                }
            } else flowOf<MilestoneEntity?>(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Phase 7: Behavioral Learning & Personalization Streams
    val allBehavioralEvents: StateFlow<List<BehavioralEventEntity>> = repository.allBehavioralEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTaskPerformanceRecords: StateFlow<List<TaskPerformanceRecordEntity>> = repository.allTaskPerformanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecommendationFeedback: StateFlow<List<RecommendationFeedbackEntity>> = repository.allRecommendationFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLearnedPatterns: StateFlow<List<LearnedPatternEntity>> = repository.allLearnedPatterns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalCapacityModel: StateFlow<PersonalCapacityModel> = combine(
        allTasks,
        allTaskPerformanceRecords,
        allDailyReviews,
        allBehavioralEvents
    ) { tasks, records, reviews, events ->
        LearningEngine.calculatePersonalCapacity(tasks, records, reviews, events)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LearningEngine.calculatePersonalCapacity(emptyList(), emptyList(), emptyList(), emptyList())
    )

    val planningAccuracyReport: StateFlow<PlanningAccuracyReport> = combine(
        allTasks,
        allBehavioralEvents,
        allTaskPerformanceRecords,
        allDailyReviews
    ) { tasks, events, records, reviews ->
        LearningEngine.calculatePlanningAccuracy(tasks, events, records, reviews)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LearningEngine.calculatePlanningAccuracy(emptyList(), emptyList(), emptyList(), emptyList())
    )

    val productivityPatternsReport: StateFlow<ProductivityPatternsReport> = combine(
        allBehavioralEvents,
        allTaskPerformanceRecords,
        allTasks
    ) { events, records, tasks ->
        LearningEngine.detectProductivityPatterns(events, records, tasks)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LearningEngine.detectProductivityPatterns(emptyList(), emptyList(), emptyList())
    )

    private val _personalizedInsights = MutableStateFlow<List<PersonalizedInsightItem>>(emptyList())
    val personalizedInsights: StateFlow<List<PersonalizedInsightItem>> = _personalizedInsights.asStateFlow()

    private val _isLoadingPersonalizedAI = MutableStateFlow(false)
    val isLoadingPersonalizedAI: StateFlow<Boolean> = _isLoadingPersonalizedAI.asStateFlow()

    private val _currentPatternsTab = MutableStateFlow("OVERVIEW") // "OVERVIEW" or "MY_PATTERNS"
    val currentPatternsTab: StateFlow<String> = _currentPatternsTab.asStateFlow()

    private val _showInsightWhyDialog = MutableStateFlow<PersonalizedInsightItem?>(null)
    val showInsightWhyDialog: StateFlow<PersonalizedInsightItem?> = _showInsightWhyDialog.asStateFlow()

    // Phase 8: Predictive AI & Proactive Life Assistant States
    private val _alternativeTaskOffset = MutableStateFlow(0)
    val alternativeTaskOffset: StateFlow<Int> = _alternativeTaskOffset.asStateFlow()

    private val _dismissedRecommendationIds = MutableStateFlow<Set<String>>(emptySet())
    val dismissedRecommendationIds: StateFlow<Set<String>> = _dismissedRecommendationIds.asStateFlow()

    private val _predictiveNotificationsEnabled = MutableStateFlow(true)
    val predictiveNotificationsEnabled: StateFlow<Boolean> = _predictiveNotificationsEnabled.asStateFlow()

    private val _quietHoursEnabled = MutableStateFlow(true)
    val quietHoursEnabled: StateFlow<Boolean> = _quietHoursEnabled.asStateFlow()

    private val _aiEnhancedBriefingText = MutableStateFlow<String?>(null)
    val aiEnhancedBriefingText: StateFlow<String?> = _aiEnhancedBriefingText.asStateFlow()

    private val _isGeneratingAiBriefing = MutableStateFlow(false)
    val isGeneratingAiBriefing: StateFlow<Boolean> = _isGeneratingAiBriefing.asStateFlow()

    private val _showTomorrowPreviewModal = MutableStateFlow(false)
    val showTomorrowPreviewModal: StateFlow<Boolean> = _showTomorrowPreviewModal.asStateFlow()

    val todayOverloadPrediction: StateFlow<ScheduleOverloadPrediction> = combine(
        allTasks,
        calendarEvents,
        personalCapacityModel,
        planningAccuracyReport,
        allTaskPerformanceRecords,
        userProfile
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val tasks = args[0] as List<TaskEntity>
        @Suppress("UNCHECKED_CAST")
        val calEvents = args[1] as List<CalendarEvent>
        val cap = args[2] as PersonalCapacityModel
        val acc = args[3] as PlanningAccuracyReport
        @Suppress("UNCHECKED_CAST")
        val records = args[4] as List<TaskPerformanceRecordEntity>
        val profile = args[5] as? UserProfileEntity

        PredictiveEngine.predictScheduleOverload(
            date = getTodayDateString(),
            tasks = tasks,
            calendarEvents = calEvents,
            capacityModel = cap,
            accuracyReport = acc,
            performanceRecords = records,
            userProfile = profile
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PredictiveEngine.predictScheduleOverload(
            date = getTodayDateString(),
            tasks = emptyList(),
            calendarEvents = emptyList(),
            capacityModel = LearningEngine.calculatePersonalCapacity(emptyList(), emptyList(), emptyList(), emptyList()),
            accuracyReport = LearningEngine.calculatePlanningAccuracy(emptyList(), emptyList(), emptyList(), emptyList()),
            performanceRecords = emptyList()
        )
    )

    val whatShouldIDoNow: StateFlow<WhatShouldIDoNowResult> = combine(
        allTasks,
        calendarEvents,
        productivityPatternsReport,
        allTaskPerformanceRecords,
        _alternativeTaskOffset
    ) { tasks, calEvents, patterns, records, offset ->
        val todayStr = getTodayDateString()
        val todayTasks = tasks.filter { it.date == todayStr }
        val baseResult = PredictiveEngine.calculateWhatShouldIDoNow(
            currentTime = Calendar.getInstance(),
            todayTasks = todayTasks,
            calendarEvents = calEvents,
            patterns = patterns,
            performanceRecords = records
        )
        if (offset > 0 && baseResult.secondaryOptionTaskId != null) {
            val secTask = todayTasks.find { it.id == baseResult.secondaryOptionTaskId }
            if (secTask != null) {
                baseResult.copy(
                    recommendedTaskId = secTask.id,
                    recommendedTaskTitle = secTask.title,
                    priority = secTask.priority,
                    durationMinutes = (secTask.durationHours * 60).toInt(),
                    energyLevel = secTask.energyLevel,
                    reason = "Alternative recommendation: ${secTask.priority} priority item matching current available window.",
                    secondaryOptionTaskId = baseResult.recommendedTaskId,
                    secondaryOptionTitle = baseResult.recommendedTaskTitle
                )
            } else baseResult
        } else {
            baseResult
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WhatShouldIDoNowResult(
            recommendedTaskId = null,
            recommendedTaskTitle = "Analyzing today's schedule...",
            priority = "IMPORTANT",
            durationMinutes = 30,
            energyLevel = "MEDIUM",
            focusWindowAvailableMinutes = 60,
            reason = "Evaluating upcoming commitments and priority queues.",
            isActionable = false
        )
    )

    val predictiveFocusWindows: StateFlow<List<PredictiveFocusWindow>> = combine(
        allTasks,
        calendarEvents,
        productivityPatternsReport,
        personalCapacityModel
    ) { tasks, calEvents, patterns, cap ->
        PredictiveEngine.detectPredictiveFocusWindows(
            tasks = tasks.filter { it.date == getTodayDateString() },
            calendarEvents = calEvents,
            patterns = patterns,
            capacityModel = cap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deadlineRisks: StateFlow<List<DeadlineRiskPrediction>> = combine(
        allTasks,
        allGoals,
        selectedMilestones,
        personalCapacityModel,
        allTaskPerformanceRecords
    ) { tasks, goals, milestones, cap, records ->
        PredictiveEngine.predictDeadlineRisk(
            todayDate = getTodayDateString(),
            tasks = tasks,
            goals = goals,
            milestones = milestones,
            capacityModel = cap,
            performanceRecords = records
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitRisks: StateFlow<List<HabitRiskPrediction>> = combine(
        allHabits,
        calendarEvents,
        allBehavioralEvents,
        productivityPatternsReport
    ) { habits, calEvents, events, patterns ->
        PredictiveEngine.predictHabitRisk(
            habits = habits,
            calendarEvents = calEvents,
            events = events,
            patterns = patterns
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val planDivergenceReport: StateFlow<PlanDivergenceReport> = combine(
        allTasks,
        calendarEvents,
        allBehavioralEvents
    ) { tasks, calEvents, events ->
        PredictiveEngine.detectPlanDivergence(
            currentTime = Calendar.getInstance(),
            todayTasks = tasks.filter { it.date == getTodayDateString() },
            calendarEvents = calEvents,
            events = events
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PlanDivergenceReport(false, 0, 0, 0, 0, 0, emptyList(), "")
    )

    val tomorrowPreview: StateFlow<TomorrowPreviewReport> = combine(
        allTasks,
        personalCapacityModel,
        planningAccuracyReport,
        allTaskPerformanceRecords,
        userProfile
    ) { tasks, cap, acc, records, profile ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        PredictiveEngine.generateTomorrowPreview(
            tomorrowDate = tomorrowDate,
            tasks = tasks,
            calendarEvents = emptyList(),
            capacityModel = cap,
            accuracyReport = acc,
            performanceRecords = records,
            userProfile = profile
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TomorrowPreviewReport(
            date = "",
            expectedCapacityHours = 4.5f,
            plannedWorkloadHours = 0f,
            calendarLoadHours = 0f,
            overloadRisk = OverloadRiskLevel.LOW,
            importantDeadlines = emptyList(),
            recommendedFocusPeriod = "Morning",
            potentialConflicts = emptyList(),
            tasksToPostpone = emptyList()
        )
    )

    val activeRecommendations: StateFlow<List<PredictiveRecommendation>> = combine(
        allTasks,
        calendarEvents,
        allHabits,
        allGoals,
        selectedMilestones,
        personalCapacityModel,
        planningAccuracyReport,
        allTaskPerformanceRecords,
        allBehavioralEvents,
        allRecommendationFeedback,
        _dismissedRecommendationIds
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val tasks = args[0] as List<TaskEntity>
        @Suppress("UNCHECKED_CAST")
        val calEvents = args[1] as List<CalendarEvent>
        @Suppress("UNCHECKED_CAST")
        val habits = args[2] as List<HabitEntity>
        @Suppress("UNCHECKED_CAST")
        val goals = args[3] as List<GoalEntity>
        @Suppress("UNCHECKED_CAST")
        val milestones = args[4] as List<MilestoneEntity>
        val cap = args[5] as PersonalCapacityModel
        val acc = args[6] as PlanningAccuracyReport
        @Suppress("UNCHECKED_CAST")
        val records = args[7] as List<TaskPerformanceRecordEntity>
        @Suppress("UNCHECKED_CAST")
        val events = args[8] as List<BehavioralEventEntity>
        @Suppress("UNCHECKED_CAST")
        val feedback = args[9] as List<RecommendationFeedbackEntity>
        @Suppress("UNCHECKED_CAST")
        val dismissed = args[10] as Set<String>

        val cal = Calendar.getInstance()
        val todayDate = getTodayDateString()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        val raw = PredictiveEngine.generateRankedRecommendations(
            todayDate = todayDate,
            tomorrowDate = tomorrowDate,
            tasks = tasks,
            calendarEvents = calEvents,
            habits = habits,
            goals = goals,
            milestones = milestones,
            capacityModel = cap,
            accuracyReport = acc,
            performanceRecords = records,
            events = events,
            feedbackList = feedback
        )
        raw.filter { !dismissed.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val predictiveMorningBriefing: StateFlow<MorningBriefing> = combine(
        allTasks,
        calendarEvents,
        personalCapacityModel,
        planningAccuracyReport,
        allTaskPerformanceRecords
    ) { tasks, calEvents, cap, acc, records ->
        val todayStr = getTodayDateString()
        PredictiveEngine.generateMorningBriefing(
            todayDate = todayStr,
            todayTasks = tasks.filter { it.date == todayStr },
            calendarEvents = calEvents,
            capacityModel = cap,
            accuracyReport = acc,
            performanceRecords = records
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MorningBriefing(
            date = getTodayDateString(),
            capacityStatus = "OPTIMAL",
            mainPriorityTask = "Daily Planning",
            bestFocusWindow = "09:00 - 10:30 AM",
            potentialIssue = null,
            aiRecommendation = "Focus on your primary goal during morning deep work."
        )
    )

    fun toggleAlternativeWhatShouldIDoNow() {
        _alternativeTaskOffset.value = if (_alternativeTaskOffset.value == 0) 1 else 0
    }

    fun dismissPredictiveRecommendation(rec: PredictiveRecommendation) {
        _dismissedRecommendationIds.value = _dismissedRecommendationIds.value + rec.id
        viewModelScope.launch {
            repository.updateRecommendationState(rec.id, "DISMISSED")
            logBehavioralEvent(
                eventType = BehavioralEventType.AI_RECOMMENDATION_REJECTED,
                metadataJson = "{\"id\":\"${rec.id}\",\"type\":\"${rec.type.name}\"}"
            )
        }
    }

    fun acceptPredictiveRecommendation(rec: PredictiveRecommendation) {
        _dismissedRecommendationIds.value = _dismissedRecommendationIds.value + rec.id
        viewModelScope.launch {
            repository.updateRecommendationState(rec.id, "ACCEPTED")
            logBehavioralEvent(
                eventType = BehavioralEventType.AI_RECOMMENDATION_ACCEPTED,
                metadataJson = "{\"id\":\"${rec.id}\",\"type\":\"${rec.type.name}\"}"
            )
        }
    }

    fun feedbackPredictiveRecommendation(rec: PredictiveRecommendation, feedback: String) {
        viewModelScope.launch {
            recordRecommendationFeedback(
                recommendationType = rec.type.name,
                recommendationText = rec.title,
                feedback = feedback
            )
            _dismissedRecommendationIds.value = _dismissedRecommendationIds.value + rec.id
        }
    }

    fun generateAiEnhancedMorningBriefing() {
        viewModelScope.launch {
            _isGeneratingAiBriefing.value = true
            try {
                val profile = userProfile.value ?: repository.userProfile.firstOrNull()
                val cap = personalCapacityModel.value
                val acc = planningAccuracyReport.value
                val pat = productivityPatternsReport.value
                val briefing = predictiveMorningBriefing.value

                val context = LearningEngine.buildPrivacySafeAiContext(
                    userName = profile?.name ?: "Julian",
                    capacity = cap,
                    accuracy = acc,
                    patterns = pat
                )

                val enhanced = GeminiService.generateAiEnhancedBriefing(briefing, context)
                _aiEnhancedBriefingText.value = enhanced
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to generate AI enhanced briefing", e)
            } finally {
                _isGeneratingAiBriefing.value = false
            }
        }
    }

    fun setTomorrowPreviewModalVisible(visible: Boolean) {
        _showTomorrowPreviewModal.value = visible
    }

    fun setPredictiveNotificationsEnabled(enabled: Boolean) {
        _predictiveNotificationsEnabled.value = enabled
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        _quietHoursEnabled.value = enabled
    }

    fun startTaskFromRecommendation(taskId: Int) {
        val task = allTasks.value.find { it.id == taskId }
        if (task != null) {
            updateFocusTarget(task.title)
        }
        navigateTo("FOCUS")
    }

    fun setPatternsTab(tab: String) {
        _currentPatternsTab.value = tab
    }

    fun showWhyExplanation(item: PersonalizedInsightItem?) {
        _showInsightWhyDialog.value = item
    }

    fun recordRecommendationFeedback(recommendationType: String, recommendationText: String, feedback: String) {
        viewModelScope.launch {
            repository.insertRecommendationFeedback(
                RecommendationFeedbackEntity(
                    recommendationType = recommendationType,
                    recommendationText = recommendationText,
                    feedback = feedback
                )
            )
            _personalizedInsights.value = _personalizedInsights.value.map { item ->
                if (item.recommendationType == recommendationType || item.title == recommendationText || item.insightText == recommendationText) {
                    item.copy(feedbackState = feedback)
                } else item
            }
            if (feedback == "HELPFUL") {
                logBehavioralEvent(
                    eventType = BehavioralEventType.AI_RECOMMENDATION_ACCEPTED,
                    metadataJson = "{\"type\":\"$recommendationType\"}"
                )
            } else {
                logBehavioralEvent(
                    eventType = BehavioralEventType.AI_RECOMMENDATION_REJECTED,
                    metadataJson = "{\"type\":\"$recommendationType\",\"feedback\":\"$feedback\"}"
                )
            }
        }
    }

    fun generatePersonalizedInsightsAI() {
        viewModelScope.launch {
            _isLoadingPersonalizedAI.value = true
            try {
                val profile = userProfile.value ?: repository.userProfile.firstOrNull()
                val cap = personalCapacityModel.value
                val acc = planningAccuracyReport.value
                val pat = productivityPatternsReport.value
                val feedback = allRecommendationFeedback.value

                val context = LearningEngine.buildPrivacySafeAiContext(
                    userName = profile?.name ?: "Julian",
                    capacity = cap,
                    accuracy = acc,
                    patterns = pat
                )

                val generated = GeminiService.generatePersonalizedInsights(context)
                val filtered = LearningEngine.filterRecommendationsWithFeedback(generated, feedback)
                _personalizedInsights.value = filtered
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to generate personalized insights AI", e)
            } finally {
                _isLoadingPersonalizedAI.value = false
            }
        }
    }

    fun predictDuration(
        category: String,
        priority: String = "IMPORTANT",
        energyLevel: String = "MEDIUM",
        title: String = ""
    ): TaskDurationPrediction {
        return LearningEngine.predictTaskDuration(
            category = category,
            priority = priority,
            energyLevel = energyLevel,
            title = title,
            records = allTaskPerformanceRecords.value
        )
    }

    fun logBehavioralEvent(
        eventType: String,
        entityId: Int? = null,
        category: String? = null,
        priority: String? = null,
        energyLevel: String? = null,
        metadataJson: String? = null,
        date: String = getTodayDateString()
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
            val event = BehavioralEventEntity(
                eventType = eventType,
                entityId = entityId,
                category = category,
                priority = priority,
                energyLevel = energyLevel,
                timeOfDayHour = hour,
                dayOfWeek = dayOfWeek,
                metadataJson = metadataJson,
                date = date
            )
            repository.insertBehavioralEvent(event)
        }
    }

    private suspend fun recordTaskCompletionLearning(task: TaskEntity) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val estimatedMin = (task.durationHours * 60).coerceAtLeast(15)
        val actualMin = estimatedMin
        val errorMin = actualMin - estimatedMin

        val event = BehavioralEventEntity(
            eventType = BehavioralEventType.TASK_COMPLETED,
            entityId = task.id,
            category = task.category,
            priority = task.priority,
            energyLevel = task.energyLevel,
            timeOfDayHour = hour,
            dayOfWeek = dayOfWeek,
            metadataJson = "{\"durationHours\":${task.durationHours}}",
            date = task.date.ifBlank { getTodayDateString() }
        )
        repository.insertBehavioralEvent(event)

        val record = TaskPerformanceRecordEntity(
            taskId = task.id,
            category = task.category,
            estimatedMinutes = estimatedMin,
            actualMinutes = actualMin,
            estimationErrorMinutes = errorMin,
            priority = task.priority,
            energyLevel = task.energyLevel,
            timeSlotHour = hour,
            dayOfWeek = dayOfWeek,
            isAiScheduled = task.isAiSuggested,
            rolloverCount = task.rescheduleCount,
            date = task.date.ifBlank { getTodayDateString() }
        )
        repository.insertTaskPerformanceRecord(record)
    }

    // Task & Planner Actions
    fun addTask(
        title: String,
        category: String,
        timeSlot: String,
        description: String,
        durationHours: Int = 1,
        location: String? = null,
        date: String = "2024-10-24",
        priority: String = "IMPORTANT",
        energyLevel: String = "MEDIUM",
        isAiSuggested: Boolean = false
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                category = category,
                timeSlot = timeSlot,
                description = description,
                durationHours = durationHours,
                location = location,
                date = date,
                priority = priority,
                energyLevel = energyLevel,
                isAiSuggested = isAiSuggested
            )
            repository.insertTask(task)
            logBehavioralEvent(
                eventType = BehavioralEventType.TASK_CREATED,
                category = category,
                priority = priority,
                energyLevel = energyLevel,
                date = date
            )
            analyzeDailyCapacity(date)
        }
    }

    fun updateTaskPriority(task: TaskEntity, newPriority: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(priority = newPriority, isAiSuggested = false))
            analyzeDailyCapacity(task.date)
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        viewModelScope.launch {
            val isCompleted = (newStatus == "COMPLETED")
            val updated = task.copy(status = newStatus, isCompleted = isCompleted)
            repository.updateTask(updated)
            if (isCompleted) {
                rewardXpAndFocus(50, 10f)
                triggerCelebration(updated.title)
                recordTaskCompletionLearning(updated)
            } else if (newStatus == "SKIPPED") {
                logBehavioralEvent(
                    eventType = BehavioralEventType.TASK_SKIPPED,
                    entityId = task.id,
                    category = task.category,
                    priority = task.priority,
                    date = task.date
                )
            }
            analyzeDailyCapacity(task.date)
        }
    }

    fun postponeTaskToTomorrow(task: TaskEntity) {
        viewModelScope.launch {
            val tomorrow = getTomorrowDateString()
            val updated = task.copy(
                date = tomorrow,
                isRollover = true,
                rescheduleCount = task.rescheduleCount + 1,
                status = "DEFERRED"
            )
            repository.updateTask(updated)
            logBehavioralEvent(
                eventType = BehavioralEventType.TASK_POSTPONED,
                entityId = task.id,
                category = task.category,
                priority = task.priority,
                date = task.date
            )
            analyzeDailyCapacity(task.date)
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            val isNowCompleted = !task.isCompleted
            val updated = task.copy(isCompleted = isNowCompleted, status = if (isNowCompleted) "COMPLETED" else "PENDING")
            repository.updateTask(updated)

            if (isNowCompleted) {
                // Reward XP (+50) and Focus Points (+10)
                rewardXpAndFocus(50, 10f)
                triggerCelebration(updated.title)
                recordTaskCompletionLearning(updated)
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
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
                logBehavioralEvent(
                    eventType = BehavioralEventType.HABIT_COMPLETED,
                    entityId = habit.id,
                    metadataJson = "{\"habitName\":\"${habit.name}\",\"streak\":${updated.streak}}"
                )
            }
        }
    }

    fun resetHabitValue(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(currentValue = 0f, isCompleted = false))
        }
    }

    fun addHabit(name: String, targetValue: Float, unit: String, iconName: String) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    name = name,
                    currentValue = 0f,
                    targetValue = targetValue,
                    unit = unit,
                    isCompleted = false,
                    iconName = iconName,
                    streak = 0
                )
            )
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun restoreHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllSystemData()
            startPersonalizationAgain()
        }
    }

    fun dismissBackupResult() {
        _backupOperationResult.value = null
    }

    fun exportBackupToUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _isBackupOperating.value = true
            val result = backupManager.exportBackup(getApplication(), uri)
            _isBackupOperating.value = false
            _backupOperationResult.value = result
        }
    }

    fun restoreBackupFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _isBackupOperating.value = true
            val result = backupManager.restoreBackup(getApplication(), uri)
            _isBackupOperating.value = false
            _backupOperationResult.value = result
        }
    }

    fun linkGoogleAccount(name: String, email: String, photoUrl: String?) {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                repository.updateUserProfile(
                    profile.copy(
                        name = name,
                        email = email,
                        photoUrl = photoUrl,
                        isGoogleLinked = true
                    )
                )
            }
        }
    }

    fun unlinkGoogleAccount() {
        viewModelScope.launch {
            userProfile.value?.let { profile ->
                repository.updateUserProfile(
                    profile.copy(
                        name = "Local User",
                        email = null,
                        photoUrl = null,
                        isGoogleLinked = false
                    )
                )
            }
        }
    }

    // --- Onboarding Management ---

    fun setOnboardingStep(step: OnboardingStep) {
        _onboardingStep.value = step
    }

    fun toggleOnboardingInterest(interest: String) {
        val current = _selectedOnboardingInterests.value.toMutableSet()
        if (current.contains(interest)) {
            current.remove(interest)
        } else {
            current.add(interest)
        }
        _selectedOnboardingInterests.value = current
    }

    fun setCustomInterestInput(text: String) {
        _customInterestInput.value = text
    }

    fun addCustomInterest() {
        val input = _customInterestInput.value.trim()
        if (input.isNotEmpty()) {
            val current = _selectedOnboardingInterests.value.toMutableSet()
            current.add(input)
            _selectedOnboardingInterests.value = current
            _customInterestInput.value = ""
        }
    }

    fun removeCustomInterest(interest: String) {
        val current = _selectedOnboardingInterests.value.toMutableSet()
        current.remove(interest)
        _selectedOnboardingInterests.value = current
    }

    fun performGoogleSignIn(context: android.content.Context) {
        viewModelScope.launch {
            _isSigningInWithGoogle.value = true
            _googleSignInError.value = null
            val result = com.example.auth.GoogleAuthHelper.signInWithGoogle(context)
            result.onSuccess { userData ->
                val current = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
                val updated = current.copy(
                    userId = userData.userId,
                    name = userData.displayName,
                    email = userData.email,
                    photoUrl = userData.photoUrl,
                    isGoogleLinked = true
                )
                repository.updateUserProfile(updated)
                _isSigningInWithGoogle.value = false
            }.onFailure { err ->
                _googleSignInError.value = err.localizedMessage ?: "Google Sign-In failed"
                _isSigningInWithGoogle.value = false
            }
        }
    }

    fun setManualGoogleUser(name: String, email: String, photoUrl: String?) {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
            val updated = current.copy(
                name = name,
                email = email,
                photoUrl = photoUrl,
                isGoogleLinked = true
            )
            repository.updateUserProfile(updated)
        }
    }

    fun startAiInterview() {
        viewModelScope.launch {
            _onboardingStep.value = OnboardingStep.AI_INTERVIEW
            _interviewHistoryList.value = emptyList()
            _isThinkingInterview.value = true
            _interviewError.value = null

            val profile = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
            val userName = profile.name.takeIf { it.isNotBlank() } ?: "there"
            val interests = _selectedOnboardingInterests.value.toList()

            try {
                val firstQ = GeminiService.generateNextAdaptiveInterviewQuestion(
                    userName = userName,
                    selectedInterests = interests,
                    history = emptyList(),
                    questionIndex = 1
                )
                _currentAdaptiveQuestion.value = firstQ
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Error generating interview question", e)
                _interviewError.value = e.localizedMessage
            } finally {
                _isThinkingInterview.value = false
            }
        }
    }

    fun submitInterviewAnswer(answer: String) {
        val currentQ = _currentAdaptiveQuestion.value ?: return
        viewModelScope.launch {
            _isThinkingInterview.value = true
            _interviewError.value = null

            val updatedHistory = _interviewHistoryList.value + InterviewHistoryItem(currentQ, answer)
            _interviewHistoryList.value = updatedHistory

            val profile = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
            val userName = profile.name.takeIf { it.isNotBlank() } ?: "there"
            val interests = _selectedOnboardingInterests.value.toList()

            if (currentQ.isFinalQuestion || updatedHistory.size >= 3) {
                // Done with interview, synthesize personalized planner config!
                try {
                    val config = GeminiService.generatePersonalizedPlannerConfig(
                        userName = userName,
                        selectedInterests = interests,
                        history = updatedHistory
                    )
                    _generatedPlannerConfig.value = config
                    _onboardingStep.value = OnboardingStep.REVIEW_CONFIRM
                } catch (e: Exception) {
                    Log.e("LifeViewModel", "Error generating planner config", e)
                    _interviewError.value = "Failed to finalize plan: ${e.localizedMessage}"
                } finally {
                    _isThinkingInterview.value = false
                }
            } else {
                // Fetch next adaptive question based on previous answers
                try {
                    val nextIndex = updatedHistory.size + 1
                    val nextQ = GeminiService.generateNextAdaptiveInterviewQuestion(
                        userName = userName,
                        selectedInterests = interests,
                        history = updatedHistory,
                        questionIndex = nextIndex
                    )
                    _currentAdaptiveQuestion.value = nextQ
                } catch (e: Exception) {
                    Log.e("LifeViewModel", "Error generating next interview question", e)
                    _interviewError.value = e.localizedMessage
                } finally {
                    _isThinkingInterview.value = false
                }
            }
        }
    }

    fun goToPreviousInterviewQuestion() {
        val history = _interviewHistoryList.value
        if (history.isEmpty()) {
            _onboardingStep.value = OnboardingStep.INTERESTS
        } else {
            val lastItem = history.last()
            _interviewHistoryList.value = history.dropLast(1)
            _currentAdaptiveQuestion.value = lastItem.question
        }
    }

    fun toggleSuggestedHabit(index: Int) {
        val config = _generatedPlannerConfig.value ?: return
        val currentHabits = config.suggestedStarterHabits.toMutableList()
        if (index in currentHabits.indices) {
            val habit = currentHabits[index]
            currentHabits[index] = habit.copy(isSelected = !habit.isSelected)
            _generatedPlannerConfig.value = config.copy(suggestedStarterHabits = currentHabits)
        }
    }

    fun toggleSuggestedGoal() {
        val config = _generatedPlannerConfig.value ?: return
        val currentGoal = config.suggestedStarterGoal ?: return
        _generatedPlannerConfig.value = config.copy(
            suggestedStarterGoal = currentGoal.copy(isSelected = !currentGoal.isSelected)
        )
    }

    fun applyPersonalizedPlan() {
        viewModelScope.launch {
            _isApplyingPlan.value = true
            val config = _generatedPlannerConfig.value
            val currentProfile = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()

            val interestsStr = _selectedOnboardingInterests.value.joinToString(", ")
            val planningStyle = config?.planningStyle ?: "Time Blocking + Tasks"
            val focusSummary = config?.focusSummary ?: "Personalized focus for $interestsStr"
            val priorityStatement = config?.topPriority ?: "Build daily consistency"
            val availability = config?.scheduleConstraints ?: "Standard routine"
            val reminderIntensity = config?.reminderIntensity ?: "Balanced"

            val updatedProfile = currentProfile.copy(
                isOnboarded = true,
                selectedInterests = interestsStr,
                planningStyle = planningStyle,
                focusSummary = focusSummary,
                priorityStatement = priorityStatement,
                availabilityWindow = availability,
                reminderIntensity = reminderIntensity,
                currentVibe = "Focused & Personalized"
            )
            repository.updateUserProfile(updatedProfile)

            // Insert approved suggested habits
            config?.suggestedStarterHabits?.filter { it.isSelected }?.forEach { habitItem ->
                repository.insertHabit(
                    HabitEntity(
                        name = habitItem.name,
                        currentValue = 0f,
                        targetValue = habitItem.targetValue,
                        unit = habitItem.unit,
                        isCompleted = false,
                        iconName = habitItem.iconName,
                        streak = 0
                    )
                )
            }

            // Insert approved suggested starter goal & milestone
            config?.suggestedStarterGoal?.let { goalItem ->
                if (goalItem.isSelected) {
                    val goalId = repository.insertGoal(
                        GoalEntity(
                            title = goalItem.title,
                            targetTimeline = "Est. 3 Months",
                            domain = goalItem.domain,
                            horizon = goalItem.horizon,
                            progressPercent = 0,
                            visionImage = "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=600&auto=format&fit=crop&q=80"
                        )
                    ).toInt()

                    repository.insertMilestone(
                        MilestoneEntity(
                            goalId = goalId,
                            title = goalItem.firstMilestoneTitle,
                            description = goalItem.firstMilestoneDesc,
                            status = "ACTIVE",
                            dueDate = "Phase 1",
                            iconName = "flag"
                        )
                    )
                }
            }

            // Generate initial insights and morning briefing
            generateTodayInsight()
            generateInsightsAI()
            generateMorningBriefing(getTodayDateString())

            _isApplyingPlan.value = false
            _currentScreen.value = "TODAY"
        }
    }

    fun startPersonalizationAgain() {
        _selectedOnboardingInterests.value = emptySet()
        _interviewHistoryList.value = emptyList()
        _currentAdaptiveQuestion.value = null
        _generatedPlannerConfig.value = null
        _onboardingStep.value = OnboardingStep.INTERESTS
        _currentScreen.value = "ONBOARDING"
    }

    fun updateWorkSchedule(
        workStartTime: String,
        workEndTime: String,
        wfhDays: String,
        workDays: String,
        weekendDays: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
            val updated = current.copy(
                workStartTime = workStartTime,
                workEndTime = workEndTime,
                wfhDays = wfhDays,
                workDays = workDays,
                weekendDays = weekendDays
            )
            repository.updateUserProfile(updated)
            // Refresh morning briefing to reflect new schedule parameters
            generateMorningBriefing(getTodayDateString())
        }
    }

    fun updateVacationSettings(
        isVacationMode: Boolean,
        startDate: String?,
        endDate: String?,
        notes: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
            val updated = current.copy(
                isVacationMode = isVacationMode,
                vacationStartDate = startDate,
                vacationEndDate = endDate,
                vacationNotes = notes
            )
            repository.updateUserProfile(updated)
            // Refresh morning briefing to reflect vacation status
            generateMorningBriefing(getTodayDateString())
        }
    }

    fun toggleVacationMode() {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
            val updated = current.copy(isVacationMode = !current.isVacationMode)
            repository.updateUserProfile(updated)
            generateMorningBriefing(getTodayDateString())
        }
    }

    fun getDayOfWeekShort(dateStr: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdf.parse(dateStr) ?: java.util.Date()
            val cal = java.util.Calendar.getInstance().apply { time = date }
            when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "Mon"
                java.util.Calendar.TUESDAY -> "Tue"
                java.util.Calendar.WEDNESDAY -> "Wed"
                java.util.Calendar.THURSDAY -> "Thu"
                java.util.Calendar.FRIDAY -> "Fri"
                java.util.Calendar.SATURDAY -> "Sat"
                java.util.Calendar.SUNDAY -> "Sun"
                else -> "Mon"
            }
        } catch (e: Exception) {
            "Mon"
        }
    }

    fun isDateInVacation(dateStr: String, profile: UserProfileEntity): Boolean {
        if (profile.isVacationMode) return true
        val start = profile.vacationStartDate
        val end = profile.vacationEndDate
        if (!start.isNullOrBlank() && !end.isNullOrBlank()) {
            return try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val d = sdf.parse(dateStr)
                val s = sdf.parse(start)
                val e = sdf.parse(end)
                if (d != null && s != null && e != null) {
                    !d.before(s) && !d.after(e)
                } else false
            } catch (ex: Exception) {
                false
            }
        } else if (!start.isNullOrBlank()) {
            return dateStr >= start
        }
        return false
    }

    fun getScheduleStatusForDate(dateStr: String): ScheduleDayStatus {
        val profile = userProfile.value ?: UserProfileEntity()
        val dayOfWeek = getDayOfWeekShort(dateStr)
        val weekendList = profile.weekendDays.split(",").map { it.trim() }
        val wfhList = profile.wfhDays.split(",").map { it.trim() }
        val isVacation = isDateInVacation(dateStr, profile)
        val isWeekend = weekendList.contains(dayOfWeek)
        val isWfh = wfhList.contains(dayOfWeek) && !isWeekend && !isVacation
        val isWorkDay = !isWeekend && !isVacation

        val label = when {
            isVacation -> if (profile.vacationNotes.isNotBlank()) "Vacation: ${profile.vacationNotes}" else "Vacation Mode"
            isWeekend -> "Weekend Rest Day"
            isWfh -> "Work From Home (${profile.workStartTime} - ${profile.workEndTime})"
            else -> "In-Office (${profile.workStartTime} - ${profile.workEndTime})"
        }

        val tag = when {
            isVacation -> "VACATION"
            isWeekend -> "WEEKEND"
            isWfh -> "WFH"
            else -> "OFFICE"
        }

        return ScheduleDayStatus(
            isVacation = isVacation,
            isWeekend = isWeekend,
            isWfh = isWfh,
            isWorkDay = isWorkDay,
            label = label,
            tag = tag,
            workHours = "${profile.workStartTime} - ${profile.workEndTime}"
        )
    }

    fun updateTabBanner(tabName: String, imageUri: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.userProfile.firstOrNull() ?: com.example.data.UserProfileEntity()
            val updated = when (tabName.uppercase()) {
                "TODAY" -> current.copy(todayBannerUrl = imageUri)
                "PLANNER" -> current.copy(plannerBannerUrl = imageUri)
                "HABITS" -> current.copy(habitsBannerUrl = imageUri)
                "INSIGHTS" -> current.copy(insightsBannerUrl = imageUri)
                "PROFILE" -> current.copy(todayBannerUrl = imageUri)
                else -> current
            }
            repository.updateUserProfile(updated)
        }
    }

    fun updateProfilePhoto(photoUri: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: repository.userProfile.firstOrNull() ?: com.example.data.UserProfileEntity()
            repository.updateUserProfile(current.copy(photoUrl = photoUri))
        }
    }

    fun updateGoalVisionImage(goalId: Int, visionImageUri: String) {
        viewModelScope.launch {
            val goal = allGoals.value.find { it.id == goalId }
            if (goal != null) {
                repository.insertGoal(goal.copy(visionImage = visionImageUri))
            }
        }
    }

    // Goal Setup & AI Milestone Generation
    fun generateQuestionsForGoal(title: String, domain: String, targetTimeline: String) {
        viewModelScope.launch {
            _isFetchingQuestions.value = true
            _interviewQuestions.value = emptyList()
            try {
                val prompt = """
                    The user wants to achieve this grand vision goal: "$title" (Domain: $domain, Target timeframe: $targetTimeline).
                    To generate a highly tailored sequential roadmap of 3 milestones, generate exactly 3 or 4 targeted multiple-choice interview questions.
                    
                    Ask questions evaluating:
                    1. Their current experience level or baseline with this topic
                    2. Weekly time commitment they can dedicate
                    3. Their available tools, budget, or preferred learning/working style
                    4. Potential primary constraint or hurdle they foresee

                    For each question, provide 3 to 4 distinct multiple-choice options tailored specifically to "$title".
                    
                    Respond ONLY with a valid JSON array of objects without any markdown code block formatting (no ```json or ```).
                    Format:
                    [
                      {
                        "id": 1,
                        "question": "What is your current experience level with $title?",
                        "subtitle": "Baseline Experience",
                        "options": [
                          "Complete Beginner (Starting from scratch)",
                          "Foundational (Know the basics, need structure)",
                          "Intermediate (Have practical experience)",
                          "Advanced (Looking for scale and optimization)"
                        ]
                      },
                      {
                        "id": 2,
                        "question": "How much focused time can you allocate per week?",
                        "subtitle": "Weekly Capacity",
                        "options": [
                          "1 to 3 hours / week (Casual pacing)",
                          "4 to 8 hours / week (Steady dedication)",
                          "10 to 15+ hours / week (Intensive sprint)"
                        ]
                      },
                      {
                        "id": 3,
                        "question": "What is your preferred execution approach?",
                        "subtitle": "Execution Style",
                        "options": [
                          "Structured curriculum & guided roadmaps",
                          "Hands-on building & trial-by-doing",
                          "Mentorship, peer accountability & coaching",
                          "Self-directed research & independent study"
                        ]
                      }
                    ]
                """.trimIndent()

                var rawResult = GeminiService.generateContent(prompt).trim()
                if (rawResult.startsWith("```")) {
                    val lines = rawResult.lines()
                    rawResult = lines.filter { !it.startsWith("```") }.joinToString("\n").trim()
                }

                val jsonArray = org.json.JSONArray(rawResult)
                val questions = mutableListOf<InterviewQuestion>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val questionText = obj.getString("question")
                    val subtitle = obj.optString("subtitle", "Assessment")
                    val optionsArr = obj.optJSONArray("options")
                    val options = mutableListOf<String>()
                    if (optionsArr != null) {
                        for (j in 0 until optionsArr.length()) {
                            options.add(optionsArr.getString(j))
                        }
                    }
                    if (options.isEmpty()) {
                        options.addAll(listOf("Beginning stage", "Intermediate stage", "Advanced stage"))
                    }
                    questions.add(
                        InterviewQuestion(
                            id = obj.optInt("id", i + 1),
                            question = questionText,
                            subtitle = subtitle,
                            options = options
                        )
                    )
                }
                _interviewQuestions.value = if (questions.isNotEmpty()) questions else getDefaultInterviewQuestions(title, domain)
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to generate multiple choice interview questions", e)
                _interviewQuestions.value = getDefaultInterviewQuestions(title, domain)
            } finally {
                _isFetchingQuestions.value = false
            }
        }
    }

    private fun getDefaultInterviewQuestions(title: String, domain: String): List<InterviewQuestion> {
        return listOf(
            InterviewQuestion(
                id = 1,
                question = "What is your current starting baseline for \"$title\"?",
                subtitle = "Experience & Baseline",
                options = listOf(
                    "Complete Beginner (Starting from square one)",
                    "Beginner-Intermediate (Familiar with foundations)",
                    "Experienced (Have done related projects or habits)",
                    "Advanced (Ready for mastery and specialized execution)"
                )
            ),
            InterviewQuestion(
                id = 2,
                question = "What is your current daily work / employment commitment?",
                subtitle = "Work Schedule",
                options = listOf(
                    "Full-Time Job (e.g. 9:00 AM - 5:00 PM weekdays)",
                    "Part-Time Job (Morning or Afternoon shifts)",
                    "Freelance / Variable flexible hours",
                    "Full Focus (No Job / 100% bandwidth for vision)"
                )
            ),
            InterviewQuestion(
                id = 3,
                question = "How much dedicated time can you realistically invest each week?",
                subtitle = "Weekly Commitment",
                options = listOf(
                    "2 to 4 hours / week (Light, sustainable consistency around job)",
                    "5 to 8 hours / week (Solid daily morning/evening focus blocks)",
                    "10 to 15+ hours / week (Deep immersion & weekend sprints)"
                )
            ),
            InterviewQuestion(
                id = 4,
                question = "What is your primary available resource or preferred method?",
                subtitle = "Execution Strategy",
                options = listOf(
                    "Self-paced learning & curated digital resources",
                    "Practical hands-on building & daily reps",
                    "Guided courses or accountability partner",
                    "Dedicated budget for tools, equipment & assets"
                )
            ),
            InterviewQuestion(
                id = 5,
                question = "What is the biggest potential obstacle you want this roadmap to mitigate?",
                subtitle = "Risk & Obstacles",
                options = listOf(
                    "Balancing energy around busy work schedule",
                    "Lack of structure & overwhelming information",
                    "Inconsistent motivation & procrastination",
                    "Technical complexity or skill gaps"
                )
            )
        )
    }

    fun clearInterviewState() {
        _interviewQuestions.value = emptyList()
        _isFetchingQuestions.value = false
        _isGeneratingTailoredRoadmap.value = false
    }

    fun createGoalWithTailoredRoadmap(
        title: String,
        domain: String,
        horizon: String,
        targetTimeline: String,
        imageUrl: String?,
        questionsAndAnswers: List<Pair<String, String>>
    ) {
        viewModelScope.launch {
            _isGeneratingTailoredRoadmap.value = true
            try {
                // Insert the Goal first
                val defaultImg = imageUrl ?: when (domain) {
                    "Career" -> "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=600&auto=format&fit=crop&q=80"
                    "Health" -> "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80"
                    "Wealth" -> "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=600&auto=format&fit=crop&q=80"
                    else -> "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600&auto=format&fit=crop&q=80"
                }

                val goalId = repository.insertGoal(
                    GoalEntity(
                        title = title,
                        domain = domain,
                        horizon = horizon,
                        targetTimeline = targetTimeline,
                        visionImage = defaultImg,
                        progressPercent = 0
                    )
                ).toInt()

                // Generate tailored milestones with answers
                val generatedMilestones = generateTailoredMilestonesWithAI(title, domain, horizon, targetTimeline, questionsAndAnswers, goalId)
                val milestones = if (generatedMilestones.isNotEmpty()) {
                    generatedMilestones
                } else {
                    // Fallback if empty or failed
                    listOf(
                        MilestoneEntity(goalId = goalId, title = "Initial Groundwork", description = "Set up your workspace and gather baseline resources.", status = "ACTIVE", iconName = "terminal"),
                        MilestoneEntity(goalId = goalId, title = "Core Execution Phase", description = "Dive deep into the main milestones of your personalized vision.", status = "LOCKED", iconName = "architecture"),
                        MilestoneEntity(goalId = goalId, title = "Final Delivery & Check-in", description = "Finalize targets and complete a comprehensive review.", status = "LOCKED", iconName = "workspace_premium")
                    )
                }

                for (m in milestones) {
                    repository.insertMilestone(m)
                }

                // Clear states & navigate to milestone screen
                clearInterviewState()
                navigateTo("MILESTONE_PLAN", goalId = goalId)
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to create tailored goal", e)
            } finally {
                _isGeneratingTailoredRoadmap.value = false
            }
        }
    }

    private suspend fun generateTailoredMilestonesWithAI(
        title: String,
        domain: String,
        horizon: String,
        targetTimeline: String,
        questionsAndAnswers: List<Pair<String, String>>,
        goalId: Int
    ): List<MilestoneEntity> {
        val qAsFormatted = questionsAndAnswers.joinToString("\n") { (q, a) -> "Q: $q\nA: $a" }
        
        val prompt = """
            The user wants to achieve this goal: "$title" (Domain: $domain, Horizon: $horizon, Timeframe target: $targetTimeline).
            
            We interviewed the user with specific questions to better tailor the roadmap. Here is their profile details and what is available:
            $qAsFormatted
            
            Based on these specific resources, skill level, and constraints, generate exactly 3 highly customized sequential milestones / roadmap steps that the user can work on to achieve this goal in this timeframe. Make the steps reflect their answers!
            Each milestone must have:
            1. Title (short, 3-5 words)
            2. Description (1 sentence describing what to do, incorporating timeframe expectations and referencing their answers if appropriate)
            3. Icon name (use exactly one of: "payments", "sports_motorsports", "shield", "two_wheeler", "architecture", "groups", "terminal", "workspace_premium", "self_improvement", "fitness_center")

            Respond ONLY with a valid JSON array of objects, containing "title", "description", and "iconName" fields. Do not include markdown block formatting (no ```json or ```). Just the raw JSON.
            Example format:
            [
              {"title": "Establish Saving Fund", "description": "Open a high-yield savings account and set monthly goals matching your budget.", "iconName": "payments"},
              {"title": "Track Automated Net Worth", "description": "Synthesize and map account balances regularly using your preferred software.", "iconName": "payments"},
              {"title": "Secure Investment Allocation", "description": "Lock in index fund distribution within the target timeframe.", "iconName": "shield"}
            ]
        """.trimIndent()

        return try {
            var rawResult = GeminiService.generateContent(prompt).trim()
            if (rawResult.startsWith("```")) {
                val lines = rawResult.lines()
                rawResult = lines.filter { !it.startsWith("```") }.joinToString("\n").trim()
            }

            val jsonArray = org.json.JSONArray(rawResult)
            val list = mutableListOf<MilestoneEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    MilestoneEntity(
                        goalId = goalId,
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        status = if (i == 0) "ACTIVE" else "LOCKED",
                        iconName = obj.optString("iconName", "terminal")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("LifeViewModel", "Failed to generate tailored AI milestones", e)
            emptyList()
        }
    }

    fun editMilestoneWithAIAssist(
        milestoneId: Int,
        promptInstruction: String,
        onSuccess: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            _isRewritingMilestone.value = true
            try {
                val milestone = repository.getMilestoneById(milestoneId) ?: return@launch
                val currentTitle = milestone.title
                val currentDesc = milestone.description

                val prompt = """
                    The user wants to rewrite a specific milestone / roadmap step with AI assistance.
                    Current Milestone Title: "$currentTitle"
                    Current Milestone Description: "$currentDesc"
                    
                    User Instruction for rewrite: "$promptInstruction"
                    
                    Please rewrite the Title (short, 3-5 words) and Description (1 sentence describing what to do).
                    Keep the original goal intent but modify it exactly based on the user's instructions.
                    
                    Respond ONLY with a valid JSON object containing "title" and "description" fields. Do not include markdown block formatting (no ```json or ```). Just the raw JSON.
                    Example format:
                    {"title": "New Title Here", "description": "New description sentence here."}
                """.trimIndent()

                var rawResult = GeminiService.generateContent(prompt).trim()
                if (rawResult.startsWith("```")) {
                    val lines = rawResult.lines()
                    rawResult = lines.filter { !it.startsWith("```") }.joinToString("\n").trim()
                }

                val jsonObject = org.json.JSONObject(rawResult)
                val newTitle = jsonObject.getString("title")
                val newDesc = jsonObject.getString("description")

                onSuccess(newTitle, newDesc)
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to edit milestone with AI", e)
            } finally {
                _isRewritingMilestone.value = false
            }
        }
    }

    fun updateMilestoneDetails(milestoneId: Int, newTitle: String, newDesc: String) {
        viewModelScope.launch {
            repository.getMilestoneById(milestoneId)?.let { m ->
                repository.updateMilestone(m.copy(title = newTitle, description = newDesc))
            }
        }
    }

    fun generatePhaseDailyTasksForPlanner(
        goalId: Int,
        milestoneId: Int?,
        jobScheduleType: JobScheduleType,
        customJobTimeSlot: String?,
        includeJobBlocks: Boolean,
        startDate: String = "2024-10-21",
        numDays: Int = 7,
        onComplete: ((Int) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isGeneratingPhaseDailyTasks.value = true
            try {
                val goal = allGoals.value.find { it.id == goalId }
                val goalTitle = goal?.title ?: "Grand Vision"
                val goalDomain = goal?.domain ?: "Career"
                val goalHorizon = goal?.horizon ?: "Quarterly"

                val allMilestonesForGoal = repository.getMilestonesForGoal(goalId).firstOrNull() ?: emptyList()
                val targetMilestone = if (milestoneId != null) {
                    allMilestonesForGoal.find { it.id == milestoneId }
                } else {
                    allMilestonesForGoal.find { it.status == "ACTIVE" } ?: allMilestonesForGoal.firstOrNull()
                }

                val milestoneTitle = targetMilestone?.title ?: "Core Execution Phase"
                val milestoneDesc = targetMilestone?.description ?: "Execute the core milestones of this grand vision."
                val targetMilestoneId = targetMilestone?.id ?: milestoneId ?: 1

                val effectiveJobSlot = if (!customJobTimeSlot.isNullOrBlank()) customJobTimeSlot else jobScheduleType.defaultTimeSlot
                val hasJob = jobScheduleType != JobScheduleType.NO_JOB_FULL_FOCUS && includeJobBlocks

                val datesList = listOf(
                    "2024-10-21", "2024-10-22", "2024-10-23",
                    "2024-10-24", "2024-10-25", "2024-10-26", "2024-10-27"
                ).take(numDays)

                val prompt = """
                    You are an expert AI Life OS Planner and Grand Vision Tactical Architect.
                    Break down the Grand Vision phase into step-by-step actionable daily tasks across the 7-day week ($startDate to ${datesList.lastOrNull() ?: "2024-10-27"}) and place them into the user's planner.

                    VISION CONTEXT:
                    - Grand Vision Goal: "$goalTitle" (Domain: $goalDomain, Horizon: $goalHorizon)
                    - Active Phase / Milestone: "$milestoneTitle"
                    - Phase Objective: "$milestoneDesc"

                    WORK / EMPLOYMENT CONSTRAINTS:
                    - Work Status: ${jobScheduleType.title}
                    - Work Hours: ${if (hasJob) effectiveJobSlot else "No employment block"}
                    - Job Blocks Scheduled: ${if (hasJob) "YES, create a daily job task block on weekdays (Mon-Fri) with title '🏢 ${jobScheduleType.title.substringBefore(" (")}: Core Work Hours' and timeslot '$effectiveJobSlot'" else "NO"}

                    IMPORTANT SCHEDULING RULES:
                    1. For weekdays (Monday 2024-10-21 through Friday 2024-10-25):
                       ${if (hasJob) "- Include the daily job task block (category: 'WORK', timeSlot: '$effectiveJobSlot', title: '🏢 ${jobScheduleType.title.substringBefore(" (")}: Core Work Hours')." else ""}
                       - Schedule 1 to 2 concrete, step-by-step daily tasks for the Grand Vision phase.
                       - CRITICAL CONFLICT PREVENTION: Grand Vision tasks MUST NOT conflict with the job hours. If work is 9 AM - 5 PM, schedule vision tasks in the morning (e.g. 07:00 - 08:30 AM), lunch break (12:30 - 01:15 PM), or evening (06:30 - 08:00 PM). If morning work, schedule vision tasks in the afternoon/evening.
                    2. For weekend days (Saturday 2024-10-26 & Sunday 2024-10-27):
                       - No standard job block. Schedule dedicated 1.5 - 2 hour Deep Work milestone build sessions for the Grand Vision.
                    3. Each task must have a specific, actionable title directly progressing Phase: "$milestoneTitle" (e.g. "Phase Step 1: Draft Requirements Spec", "Phase Step 2: Implement Core Engine Architecture", "Phase Step 3: Run Validation Suite").
                    4. Category should be "WORK", "GROWTH", "HEALTH", "FINANCE", or "ADMIN".

                    Respond ONLY with a valid JSON array of task objects (no markdown, no ```json formatting):
                    [
                      {
                        "title": "🏢 Work Shift",
                        "category": "WORK",
                        "timeSlot": "$effectiveJobSlot",
                        "description": "Daily job responsibilities and workplace deliverables.",
                        "date": "2024-10-21",
                        "durationHours": 8
                      },
                      {
                        "title": "Phase 1: Build Core Architecture",
                        "category": "GROWTH",
                        "timeSlot": "07:00 - 08:30 AM",
                        "description": "Setup distributed scaffolding before workday.",
                        "date": "2024-10-21",
                        "durationHours": 1
                      }
                    ]
                """.trimIndent()

                var taskCount = 0
                val generatedTasks = mutableListOf<TaskEntity>()
                val generatedSubTasks = mutableListOf<String>()

                try {
                    var rawResult = GeminiService.generateContent(prompt).trim()
                    if (rawResult.startsWith("```")) {
                        val lines = rawResult.lines()
                        rawResult = lines.filter { !it.startsWith("```") }.joinToString("\n").trim()
                    }

                    val jsonArray = org.json.JSONArray(rawResult)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val title = obj.getString("title")
                        val category = obj.optString("category", "GROWTH")
                        val timeSlot = obj.optString("timeSlot", "07:30 - 08:30 AM")
                        val desc = obj.optString("description", "")
                        val date = obj.optString("date", "2024-10-24")
                        val duration = obj.optInt("durationHours", 1)

                        generatedTasks.add(
                            TaskEntity(
                                title = title,
                                category = category,
                                timeSlot = timeSlot,
                                description = desc,
                                date = date,
                                durationHours = duration
                            )
                        )
                        if (!title.startsWith("🏢") && !title.contains("Job Work") && !title.contains("Core Work")) {
                            generatedSubTasks.add(title)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LifeViewModel", "Failed to parse AI daily tasks, using fallback generator", e)
                }

                // Fallback if empty or failed
                if (generatedTasks.isEmpty()) {
                    generatedTasks.addAll(
                        createFallbackDailyTasks(
                            goalTitle = goalTitle,
                            milestoneTitle = milestoneTitle,
                            jobScheduleType = jobScheduleType,
                            jobTimeSlot = effectiveJobSlot,
                            hasJob = hasJob,
                            datesList = datesList
                        )
                    )
                    generatedTasks.filter { !it.title.startsWith("🏢") && !it.title.contains("Job Work") }.forEach {
                        generatedSubTasks.add(it.title)
                    }
                }

                // Save all tasks to Database
                for (task in generatedTasks) {
                    repository.insertTask(task)
                    taskCount++
                }

                // Also populate milestone subtasks for checklist tracking
                for (subTaskTitle in generatedSubTasks.distinct().take(8)) {
                    repository.insertSubTask(
                        SubTaskEntity(
                            milestoneId = targetMilestoneId,
                            title = subTaskTitle,
                            isCompleted = false
                        )
                    )
                }

                _lastGeneratedTaskCount.value = taskCount
                rewardXpAndFocus(100, 25f)
                triggerCelebration("Planner Synchronized ($taskCount tasks)")
                onComplete?.invoke(taskCount)
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed generating phase daily tasks", e)
            } finally {
                _isGeneratingPhaseDailyTasks.value = false
            }
        }
    }

    private fun createFallbackDailyTasks(
        goalTitle: String,
        milestoneTitle: String,
        jobScheduleType: JobScheduleType,
        jobTimeSlot: String,
        hasJob: Boolean,
        datesList: List<String>
    ): List<TaskEntity> {
        val list = mutableListOf<TaskEntity>()

        val phaseSteps = listOf(
            "Phase Step 1: Define Requirements & Architecture Spec",
            "Phase Step 2: Scaffold Foundation & Core Modules",
            "Phase Step 3: Implement Primary Feature Logic",
            "Phase Step 4: Integration & Validation Reps",
            "Phase Step 5: Quality Review & Performance Benchmark",
            "Phase Step 6: Weekend Deep Sprint - Execution Milestone",
            "Phase Step 7: Comprehensive Retrospective & Progress Log"
        )

        datesList.forEachIndexed { index, dateStr ->
            val isWeekend = index >= 5 // Sat & Sun

            // 1. Add Job Shift on weekdays if user has a job
            if (hasJob && !isWeekend) {
                list.add(
                    TaskEntity(
                        title = "🏢 ${jobScheduleType.title.substringBefore(" (")}: Core Work Hours",
                        category = "WORK",
                        timeSlot = jobTimeSlot,
                        description = "Daily employment responsibilities and workplace deliverables.",
                        date = dateStr,
                        durationHours = if (jobScheduleType == JobScheduleType.FULL_TIME) 8 else 4
                    )
                )
            }

            // 2. Add Grand Vision Phase step scheduled around the job
            val stepTitle = phaseSteps.getOrElse(index) { "Phase: $milestoneTitle Execution" }
            val visionTimeSlot = when {
                !hasJob -> if (index % 2 == 0) "09:00 - 11:30 AM" else "02:00 - 04:30 PM"
                jobScheduleType == JobScheduleType.PART_TIME_MORNING -> "02:00 - 04:00 PM"
                jobScheduleType == JobScheduleType.PART_TIME_AFTERNOON -> "08:00 - 10:00 AM"
                jobScheduleType == JobScheduleType.PART_TIME_EVENING -> "10:00 AM - 12:00 PM"
                isWeekend -> "10:00 AM - 01:00 PM"
                else -> if (index % 2 == 0) "07:00 - 08:30 AM" else "06:30 - 08:00 PM" // Morning or Evening around 9-5
            }

            list.add(
                TaskEntity(
                    title = stepTitle,
                    category = "GROWTH",
                    timeSlot = visionTimeSlot,
                    description = "Dedicated step towards '$milestoneTitle' for '$goalTitle'.",
                    date = dateStr,
                    durationHours = 1
                )
            )

            // Optional secondary evening check-in / review on weekdays
            if (!isWeekend && hasJob && index in listOf(1, 3)) {
                list.add(
                    TaskEntity(
                        title = "Phase: 15-Min Daily Review & Progress Log",
                        category = "ADMIN",
                        timeSlot = "08:30 - 08:45 PM",
                        description = "Log daily metrics, blockers, and update Grand Vision roadmap status.",
                        date = dateStr,
                        durationHours = 1
                    )
                )
            }
        }

        return list
    }

    // Goal Setup & AI Milestone Generation
    fun createGoalFromVision(
        title: String,
        domain: String,
        horizon: String,
        targetTimeline: String,
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
                    targetTimeline = targetTimeline,
                    visionImage = defaultImg,
                    progressPercent = 0
                )
            ).toInt()

            // Try to generate customized Milestones with AI, otherwise fallback
            val generatedMilestones = generateMilestonesWithAI(title, domain, horizon, targetTimeline, goalId)
            val milestones = if (generatedMilestones.isNotEmpty()) {
                generatedMilestones
            } else {
                // Fallback to static lists based on domain
                when (domain) {
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
            }

            for (m in milestones) {
                repository.insertMilestone(m)
            }

            // Navigate to milestone overview of the newly created goal
            navigateTo("MILESTONE_PLAN", goalId = goalId)
        }
    }

    private suspend fun generateMilestonesWithAI(
        title: String,
        domain: String,
        horizon: String,
        targetTimeline: String,
        goalId: Int
    ): List<MilestoneEntity> {
        val prompt = """
            The user wants to achieve this goal: "$title" (Domain: $domain, Horizon: $horizon, Timeframe target: $targetTimeline).
            Generate exactly 3 sequential milestones / roadmap steps that the user can work on to achieve this goal in this timeframe.
            Each milestone must have:
            1. Title (short, 3-5 words)
            2. Description (1 sentence describing what to do, incorporating timeframe expectations if appropriate)
            3. Icon name (use exactly one of: "payments", "sports_motorsports", "shield", "two_wheeler", "architecture", "groups", "terminal", "workspace_premium", "self_improvement", "fitness_center")

            Respond ONLY with a valid JSON array of objects, containing "title", "description", and "iconName" fields. Do not include markdown block formatting (no ```json or ```). Just the raw JSON.
            Example format:
            [
              {"title": "Establish Saving Fund", "description": "Open a high-yield savings account and set monthly goals.", "iconName": "payments"},
              {"title": "Track Automated Net Worth", "description": "Synthesize and map account balances regularly.", "iconName": "payments"},
              {"title": "Secure Investment Allocation", "description": "Lock in index fund distribution within the target timeframe.", "iconName": "shield"}
            ]
        """.trimIndent()

        return try {
            var rawResult = GeminiService.generateContent(prompt).trim()
            if (rawResult.startsWith("```")) {
                val lines = rawResult.lines()
                rawResult = lines.filter { !it.startsWith("```") }.joinToString("\n").trim()
            }

            val jsonArray = org.json.JSONArray(rawResult)
            val list = mutableListOf<MilestoneEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    MilestoneEntity(
                        goalId = goalId,
                        title = obj.getString("title"),
                        description = obj.getString("description"),
                        status = if (i == 0) "ACTIVE" else "LOCKED",
                        iconName = obj.optString("iconName", "terminal")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("LifeViewModel", "Failed to generate AI milestones, using fallback", e)
            emptyList()
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
            repository.clearAllSystemData()
            startPersonalizationAgain()
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

    // --- Pomodoro Focus Timer Logic ---
    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        if (_selectedNoise.value != com.example.audio.NoiseType.OFF) {
            noisePlayer.play(_selectedNoise.value)
        }
        com.example.notification.FocusTimerService.startTimerService(
            getApplication(),
            _timeLeftSeconds.value,
            _focusTarget.value.ifBlank { "Deep Focus" }
        )
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        noisePlayer.stop()
        com.example.notification.FocusTimerService.pauseTimerService(getApplication())
    }

    fun resetTimer() {
        pauseTimer()
        val mode = _currentTimerMode.value
        _timeLeftSeconds.value = mode.defaultMinutes * 60
        _totalDurationSeconds.value = mode.defaultMinutes * 60
        com.example.notification.FocusTimerService.stopTimerService(getApplication())
        com.example.notification.FocusTimerService.updateTimeLeftManual(_timeLeftSeconds.value)
    }

    fun setNoiseType(type: com.example.audio.NoiseType) {
        _selectedNoise.value = type
        if (_isTimerRunning.value) {
            if (type == com.example.audio.NoiseType.OFF) {
                noisePlayer.stop()
            } else {
                noisePlayer.play(type)
            }
        }
    }

    fun setNoiseVolume(volume: Float) {
        _noiseVolume.value = volume
        noisePlayer.setVolume(volume)
    }

    override fun onCleared() {
        super.onCleared()
        noisePlayer.stop()
    }

    fun setTimerMode(mode: PomodoroMode) {
        pauseTimer()
        _currentTimerMode.value = mode
        _timeLeftSeconds.value = mode.defaultMinutes * 60
        _totalDurationSeconds.value = mode.defaultMinutes * 60
    }

    fun updateFocusTarget(target: String) {
        _focusTarget.value = target
    }

    fun generateFocusEncouragement() {
        val target = _focusTarget.value
        if (target.isBlank()) return
        viewModelScope.launch {
            _isFetchingEncouragement.value = true
            try {
                val modeLabel = _currentTimerMode.value.label
                val prompt = """
                    The user is using a Pomodoro focus timer to do deep work.
                    They are in a "$modeLabel" session.
                    They specified their focus task: "$target".
                    
                    Generate a single, short, high-energy, encouraging sentence (max 15 words) to motivate them. Keep it professional, inspirational, and directly related to their focus task if possible. Do not include markdown or quotes.
                """.trimIndent()
                val encouragement = GeminiService.generateContent(prompt).trim().removeSurrounding("\"")
                _aiEncouragement.value = encouragement
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to generate AI encouragement", e)
                _aiEncouragement.value = "Stay focused! One step at a time, you're building your future."
            } finally {
                _isFetchingEncouragement.value = false
            }
        }
    }

    fun setSmartNotificationsEnabled(enabled: Boolean) {
        _smartNotificationsEnabled.value = enabled
    }

    fun setPomodoroNotificationsEnabled(enabled: Boolean) {
        _pomodoroNotificationsEnabled.value = enabled
    }

    fun setHabitNotificationsEnabled(enabled: Boolean) {
        _habitNotificationsEnabled.value = enabled
    }

    fun triggerSmartNotificationManual() {
        viewModelScope.launch {
            _isGeneratingNotification.value = true
            try {
                val profile = repository.userProfile.firstOrNull() ?: UserProfileEntity()
                val tasks = repository.allTasks.firstOrNull() ?: emptyList()
                val habits = repository.allHabits.firstOrNull() ?: emptyList()

                val pendingTasks = tasks.filter { !it.isCompleted }
                val pendingHabits = habits.filter { !it.isCompleted }

                val taskContext = if (pendingTasks.isNotEmpty()) {
                    "Pending Tasks: ${pendingTasks.take(2).joinToString { it.title }}"
                } else {
                    "No pending tasks! Encourage them to plan new ones."
                }

                val habitContext = if (pendingHabits.isNotEmpty()) {
                    "Pending Habits: ${pendingHabits.take(2).joinToString { it.name }}"
                } else {
                    "No pending habits left for today."
                }

                val systemPrompt = """
                    You are ${profile.coachPersonality}, an expert and deeply personalized productivity coach.
                    The user's display name is ${profile.name}.
                    Their current mindset/vibe is "${profile.currentVibe}".
                    
                    Your task is to generate a short, high-impact, punchy notification title and message.
                    The message MUST reflect your personality as ${profile.coachPersonality}.
                    Keep the title to 3-5 words, and the message to 12-18 words maximum.
                    Do not use markdown, markdown bold, asterisks, or quotes in the output.
                    Format the output strictly as a JSON object:
                    {"title": "...", "message": "..."}
                """.trimIndent()

                val userPrompt = """
                    Current context for ${profile.name}:
                    $taskContext
                    $habitContext
                    Level: ${profile.level} (XP: ${profile.xp}/${profile.maxXp})
                    
                    Generate a smart, motivational push notification warning/nudge customized to this state. Remember, output only the raw JSON.
                """.trimIndent()

                val jsonResponse = GeminiService.generateContent(userPrompt, systemPrompt).trim()
                Log.d("SmartNotification", "Generated raw text: $jsonResponse")

                // Extract title and message from JSON
                var title = "${profile.coachPersonality} Nudge"
                var message = "Keep pushing forward on your goals today!"

                try {
                    // Clean json codeblock markings if any
                    val cleanJson = jsonResponse
                        .removePrefix("```json")
                        .removePrefix("```")
                        .removeSuffix("```")
                        .trim()
                    
                    val obj = org.json.JSONObject(cleanJson)
                    title = obj.optString("title", title)
                    message = obj.optString("message", message)
                } catch (e: Exception) {
                    Log.e("SmartNotification", "JSON parsing failed, falling back", e)
                    if (jsonResponse.contains("title") && jsonResponse.contains("message")) {
                        val titleRegex = "\"title\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        val messageRegex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                        titleRegex.find(jsonResponse)?.groupValues?.get(1)?.let { title = it }
                        messageRegex.find(jsonResponse)?.groupValues?.get(1)?.let { message = it }
                    } else if (jsonResponse.isNotBlank() && !jsonResponse.startsWith("Error")) {
                        message = jsonResponse
                    }
                }

                com.example.notification.SmartNotificationManager.sendNotification(
                    getApplication(),
                    title,
                    message
                )
            } catch (e: Exception) {
                Log.e("SmartNotification", "Failed to trigger notification", e)
                com.example.notification.SmartNotificationManager.sendNotification(
                    getApplication(),
                    "AI Coach Daily Sync",
                    "Keep up your productivity streak today! You're doing amazing."
                )
            } finally {
                _isGeneratingNotification.value = false
            }
        }
    }

    fun setCalendarPermissionGranted(granted: Boolean) {
        _calendarPermissionGranted.value = granted
        if (granted) {
            loadCalendarEvents(getTodayDateString())
            generateMorningBriefing(getTodayDateString())
        }
    }

    fun loadCalendarEvents(dateString: String) {
        viewModelScope.launch {
            _isLoadingCalendarEvents.value = true
            try {
                val events = com.example.data.CalendarManager.fetchEventsForDate(getApplication(), dateString)
                _calendarEvents.value = events
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Error loading calendar events", e)
            } finally {
                _isLoadingCalendarEvents.value = false
            }
        }
    }

    fun importCalendarEventAsTask(event: com.example.data.CalendarEvent, targetCategory: String = "WORK") {
        viewModelScope.launch {
            try {
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(event.startMillis))
                val task = com.example.data.TaskEntity(
                    title = event.title,
                    category = targetCategory,
                    timeSlot = event.formattedTime,
                    description = event.description ?: "Imported from Calendar App",
                    isCompleted = false,
                    date = dateStr,
                    durationHours = (((event.endMillis - event.startMillis) / (1000 * 60 * 60)).toInt()).coerceAtLeast(1),
                    location = event.location
                )
                repository.insertTask(task)
                // Reload calendar events to trigger recompositions and sync UI
                loadCalendarEvents(dateStr)
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to import event as task", e)
            }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isSendingChatMessage.value = true
            try {
                // 1. Save user message to database
                val userMsg = com.example.data.ChatMessageEntity(role = "user", text = text)
                repository.insertChatMessage(userMsg)

                // 2. Fetch full current history
                val history = chatMessages.value

                // 3. Retrieve user profile and data to establish persistent context continuity
                val profile = userProfile.value ?: repository.userProfile.firstOrNull() ?: com.example.data.UserProfileEntity()
                val coach = profile.coachPersonality
                val vibe = profile.currentVibe
                val name = profile.name
                val todayDate = getTodayDateString()

                val goals = allGoals.value
                val habits = allHabits.value
                val tasks = allTasks.value

                val contextSummary = com.example.ai.AiContextSummaryBuilder.buildSummary(
                    goals = goals,
                    habits = habits,
                    allTasks = tasks,
                    todayDate = todayDate
                )

                // 4. Set role-specific, high-craft coaching system instruction with live context summary
                val systemPrompt = """
                    You are $coach, an elite personal development advisor and productivity master inside the LifeOS companion app. 
                    The user's name is $name.
                    Their current mental posture / active vibe is "$vibe".
                    Their current leveling streak is ${profile.streak} days.
                    Their current level is ${profile.level} (XP: ${profile.xp}/${profile.maxXp}).
                    
                    $contextSummary
                    
                    Role Instructions:
                    - You MUST speak, advise, and guide in the exact voice of $coach.
                    - Stoic Mentor: calm, highly disciplined, reflective, values adversity, uses ancient Stoic principles to navigate daily friction.
                    - High-Energy Motivator: bold, enthusiastic, uses capital letters/emojis moderately, focuses on instant action, momentum, and crushing goals.
                    - Analytical Strategist: objective, uses logic, data points, breaks down goals into milestones, optimizes schedules, flags conflicts.
                    - Context Continuity: You have live awareness of their current goals, habits + streaks, today's tasks, and recent completions provided above. Reference or weave them in naturally when relevant to provide personalized coaching continuity without explicitly saying "according to my database".
                    - Be extremely tactical. Offer specific actionable ideas (e.g. recommend a habit, write a tiny priority layout, suggest Pomodoro techniques).
                    - Keep your replies concise and digestible (80-150 words max). Do not output huge lists of text or essays. Focus on the human element.
                """.trimIndent()

                // 5. Query Gemini
                val reply = com.example.data.GeminiService.generateChatResponse(history, systemPrompt)

                // 6. Save model reply to database
                if (reply.isNotBlank() && !reply.startsWith("Error:")) {
                    val modelMsg = com.example.data.ChatMessageEntity(role = "model", text = reply)
                    repository.insertChatMessage(modelMsg)
                } else {
                    val errorMsg = com.example.data.ChatMessageEntity(role = "model", text = "I had trouble reaching my coaching servers. Please make sure your Gemini API Key is configured in AI Studio's Secrets panel and try again!")
                    repository.insertChatMessage(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Error sending chat message", e)
                val errorMsg = com.example.data.ChatMessageEntity(role = "model", text = "I experienced an internal malfunction: ${e.localizedMessage}")
                repository.insertChatMessage(errorMsg)
            } finally {
                _isSendingChatMessage.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatMessages()
        }
    }

    fun addRecurringAlarm(
        context: android.content.Context,
        title: String,
        message: String,
        hour: Int,
        minute: Int,
        repeatType: String = "DAILY",
        soundEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            val alarm = RecurringAlarmEntity(
                title = title,
                message = message,
                hour = hour,
                minute = minute,
                repeatType = repeatType,
                isEnabled = true,
                soundEnabled = soundEnabled
            )
            val newId = repository.insertRecurringAlarm(alarm).toInt()
            val created = alarm.copy(id = newId)
            com.example.notification.AlarmSchedulerManager.scheduleAlarm(context, created)
        }
    }

    fun toggleRecurringAlarm(context: android.content.Context, alarm: RecurringAlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateRecurringAlarm(updated)
            if (updated.isEnabled) {
                com.example.notification.AlarmSchedulerManager.scheduleAlarm(context, updated)
            } else {
                com.example.notification.AlarmSchedulerManager.cancelAlarm(context, updated.id)
            }
        }
    }

    fun deleteRecurringAlarm(context: android.content.Context, alarm: RecurringAlarmEntity) {
        viewModelScope.launch {
            repository.deleteRecurringAlarm(alarm)
            com.example.notification.AlarmSchedulerManager.cancelAlarm(context, alarm.id)
        }
    }

    fun triggerTestAlarmNow(context: android.content.Context, title: String, message: String, delaySeconds: Int = 5) {
        com.example.notification.AlarmSchedulerManager.scheduleTestAlarm(
            context = context,
            title = title,
            message = message,
            delaySeconds = delaySeconds
        )
    }

    // --- Unified Calendar & Overlap Prevention Logic ---
    fun getUnifiedScheduleForDate(
        dateString: String,
        dayTasks: List<TaskEntity>,
        goals: List<GoalEntity>,
        alarms: List<RecurringAlarmEntity>,
        events: List<com.example.data.CalendarEvent>
    ): List<CalendarScheduleItem> {
        val rawItems = mutableListOf<CalendarScheduleItem>()

        // 1. Add Planner Tasks
        dayTasks.forEach { task ->
            val (startMin, duration) = parseTimeSlotToMinutes(task.timeSlot, task.durationHours)
            rawItems.add(
                CalendarScheduleItem(
                    id = "task_${task.id}",
                    title = task.title,
                    description = task.description,
                    type = ScheduleType.TASK,
                    startMinutes = startMin,
                    durationMinutes = duration,
                    endMinutes = startMin + duration,
                    timeSlotFormatted = task.timeSlot,
                    isCompleted = task.isCompleted,
                    categoryOrDomain = task.category,
                    taskId = task.id
                )
            )
        }

        // 2. Add Grand Vision Goals / Milestones
        goals.forEach { goal ->
            // Add goal primary focus block if applicable or active horizon
            val domainCategory = goal.domain.uppercase()
            val (gStartMin, gDuration) = parseTimeSlotToMinutes("02:00 PM - 03:30 PM", 1)
            rawItems.add(
                CalendarScheduleItem(
                    id = "goal_${goal.id}",
                    title = "Grand Vision: ${goal.title}",
                    description = "${goal.horizon} Goal (${goal.domain}) — ${goal.targetTimeline}",
                    type = ScheduleType.GRAND_VISION_MILESTONE,
                    startMinutes = gStartMin,
                    durationMinutes = 90,
                    endMinutes = gStartMin + 90,
                    timeSlotFormatted = formatMinutesToTimeSlot(gStartMin, 90),
                    isCompleted = goal.progressPercent >= 100,
                    categoryOrDomain = domainCategory
                )
            )
        }

        // 3. Add Active Recurring Alarms for today
        val dayOfWeek = try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = sdf.parse(dateString)
            val cal = java.util.Calendar.getInstance()
            if (date != null) cal.time = date
            cal.get(java.util.Calendar.DAY_OF_WEEK)
        } catch (e: Exception) {
            java.util.Calendar.MONDAY
        }

        val isWeekend = dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY

        alarms.filter { it.isEnabled }.forEach { alarm ->
            val matchesSchedule = when (alarm.repeatType) {
                "DAILY" -> true
                "WEEKDAYS" -> !isWeekend
                "WEEKENDS" -> isWeekend
                else -> true
            }

            if (matchesSchedule) {
                val startMin = alarm.hour * 60 + alarm.minute
                val duration = 30 // Default 30 min alarm block
                rawItems.add(
                    CalendarScheduleItem(
                        id = "alarm_${alarm.id}",
                        title = "🔔 ${alarm.title}",
                        description = "${alarm.message} (Recurring ${alarm.repeatType})",
                        type = ScheduleType.RECURRING_ALARM,
                        startMinutes = startMin,
                        durationMinutes = duration,
                        endMinutes = startMin + duration,
                        timeSlotFormatted = formatMinutesToTimeSlot(startMin, duration),
                        isCompleted = false,
                        categoryOrDomain = "ALARM",
                        alarmId = alarm.id
                    )
                )
            }
        }

        // 4. Add Local Phone Calendar Events
        events.forEach { event ->
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = event.startMillis }
            val startMin = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
            val durationMin = (((event.endMillis - event.startMillis) / (1000 * 60)).toInt()).coerceAtLeast(30)
            rawItems.add(
                CalendarScheduleItem(
                    id = "event_${event.id}",
                    title = "📅 ${event.title}",
                    description = event.description ?: "Local Device Calendar Event",
                    type = ScheduleType.SYSTEM_CALENDAR_EVENT,
                    startMinutes = startMin,
                    durationMinutes = durationMin,
                    endMinutes = startMin + durationMin,
                    timeSlotFormatted = event.formattedTime,
                    isCompleted = false,
                    categoryOrDomain = "CALENDAR"
                )
            )
        }

        // Apply Overlap Deconfliction Engine
        return deconflictScheduleList(rawItems)
    }

    private fun deconflictScheduleList(items: List<CalendarScheduleItem>): List<CalendarScheduleItem> {
        if (items.size <= 1) return items

        val sorted = items.sortedBy { it.startMinutes }.toMutableList()
        val result = mutableListOf<CalendarScheduleItem>()

        var currentLastEnd = -1

        for (item in sorted) {
            var newStart = item.startMinutes
            var isShifted = false

            if (newStart < currentLastEnd) {
                // Overlap detected! Shift forward past previous item + 5 min buffer
                newStart = currentLastEnd + 5
                isShifted = true
            }

            val newEnd = newStart + item.durationMinutes
            val formattedSlot = formatMinutesToTimeSlot(newStart, item.durationMinutes)

            result.add(
                item.copy(
                    startMinutes = newStart,
                    endMinutes = newEnd,
                    timeSlotFormatted = formattedSlot,
                    isShiftedForOverlap = isShifted
                )
            )

            currentLastEnd = newEnd
        }

        return result
    }

    private fun parseTimeSlotToMinutes(timeSlot: String, fallbackDurationHours: Int = 1): Pair<Int, Int> {
        return try {
            val clean = timeSlot.trim()
            val parts = clean.split("-")
            val startTimeStr = parts[0].trim()
            
            val isPm = startTimeStr.uppercase().contains("PM")
            val isAm = startTimeStr.uppercase().contains("AM")
            val digitsOnly = startTimeStr.replace("AM", "", true).replace("PM", "", true).trim()

            val timeParts = digitsOnly.split(":")
            var hour = timeParts[0].toIntOrNull() ?: 9
            val minute = if (timeParts.size > 1) timeParts[1].toIntOrNull() ?: 0 else 0

            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0

            val startMin = (hour * 60 + minute).coerceIn(0, 1400)
            val durationMin = fallbackDurationHours * 60

            Pair(startMin, durationMin)
        } catch (e: Exception) {
            Pair(9 * 60, fallbackDurationHours * 60)
        }
    }

    private fun formatMinutesToTimeSlot(startMinutes: Int, durationMinutes: Int): String {
        val startHour24 = (startMinutes / 60) % 24
        val startMin = startMinutes % 60
        val endMinutes = startMinutes + durationMinutes
        val endHour24 = (endMinutes / 60) % 24
        val endMin = endMinutes % 60

        fun formatTime(h24: Int, m: Int): String {
            val ampm = if (h24 >= 12) "PM" else "AM"
            val h12 = if (h24 % 12 == 0) 12 else h24 % 12
            return String.format(java.util.Locale.getDefault(), "%02d:%02d %s", h12, m, ampm)
        }

        return "${formatTime(startHour24, startMin)} - ${formatTime(endHour24, endMin)}"
    }

    fun deconflictAndSaveScheduleToDatabase(dateString: String) {
        viewModelScope.launch {
            val dayTasks = allTasks.value.filter { it.date == dateString }
            val goals = allGoals.value
            val alarms = allRecurringAlarms.value
            val events = calendarEvents.value

            val deconflicted = getUnifiedScheduleForDate(dateString, dayTasks, goals, alarms, events)
            
            // Save updated task slots back to Room DB
            deconflicted.forEach { item ->
                if (item.type == ScheduleType.TASK && item.taskId != null && item.isShiftedForOverlap) {
                    val task = dayTasks.find { it.id == item.taskId }
                    if (task != null) {
                        repository.updateTask(task.copy(timeSlot = item.timeSlotFormatted))
                    }
                }
            }
        }
    }

    fun getTomorrowDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    fun calculateHistoricalAverageDailyHours(): Float {
        val tasks = allTasks.value
        if (tasks.isEmpty()) return 3.5f
        val completed = tasks.filter { it.isCompleted }
        if (completed.isEmpty()) return 3.5f
        val uniqueDays = completed.map { it.date }.distinct().size.coerceAtLeast(1)
        val totalHours = completed.sumOf { it.durationHours }.toFloat()
        return (totalHours / uniqueDays).coerceIn(1.5f, 8.0f)
    }

    fun analyzeDailyCapacity(dateString: String = getTodayDateString()) {
        viewModelScope.launch {
            _isLoadingCapacity.value = true
            try {
                val profile = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
                val dayTasks = allTasks.value.filter { it.date == dateString }
                val events = calendarEvents.value
                val historicalAvg = calculateHistoricalAverageDailyHours()

                // Calculate calendar meeting hours
                val meetingHours = events.sumOf { event ->
                    val diff = (event.endMillis - event.startMillis).toFloat() / (1000f * 60f * 60f)
                    diff.coerceIn(0.5f, 4f).toDouble()
                }.toFloat()

                // Total work window from profile
                val totalWorkWindow = 8.0f

                val report = GeminiService.generateCapacityAnalysis(
                    userName = profile.name.takeIf { it.isNotBlank() } ?: "Julian",
                    plannedTasks = dayTasks,
                    historicalAvgHours = historicalAvg,
                    calendarMeetingsHours = meetingHours,
                    totalWorkWindowHours = totalWorkWindow
                )
                _capacityReport.value = report
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to analyze capacity", e)
            } finally {
                _isLoadingCapacity.value = false
            }
        }
    }

    fun requestAdaptiveRebalance(triggerReason: String, dateString: String = getTodayDateString()) {
        viewModelScope.launch {
            _isRebalancing.value = true
            _showRebalanceDialog.value = true
            try {
                val profile = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
                val dayTasks = allTasks.value.filter { it.date == dateString }
                val events = calendarEvents.value
                val currentTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                val result = GeminiService.generateAdaptiveRebalancePlan(
                    userName = profile.name.takeIf { it.isNotBlank() } ?: "Julian",
                    coachPersonality = profile.coachPersonality,
                    currentTasks = dayTasks,
                    calendarEvents = events,
                    rebalanceTrigger = triggerReason,
                    availableHours = 5.5f,
                    currentLocalTime = currentTimeStr
                )
                _rebalanceResult.value = result
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to calculate rebalance plan", e)
            } finally {
                _isRebalancing.value = false
            }
        }
    }

    fun applyRebalancePlan(dateString: String = getTodayDateString()) {
        viewModelScope.launch {
            val result = _rebalanceResult.value ?: return@launch
            val dayTasks = allTasks.value.filter { it.date == dateString }
            val tomorrow = getTomorrowDateString()

            // Update Kept Tasks (new timeslots)
            result.keptTasks.forEach { kept ->
                val task = dayTasks.find { it.id == kept.taskId }
                if (task != null && kept.newTimeSlot.isNotBlank()) {
                    repository.updateTask(task.copy(timeSlot = kept.newTimeSlot))
                }
            }

            // Update Deferred Tasks (move to tomorrow, increment reschedule count)
            result.deferredTasks.forEach { deferred ->
                val task = dayTasks.find { it.id == deferred.taskId }
                if (task != null) {
                    repository.updateTask(
                        task.copy(
                            date = tomorrow,
                            isRollover = true,
                            rescheduleCount = task.rescheduleCount + 1,
                            status = "DEFERRED"
                        )
                    )
                    logBehavioralEvent(
                        eventType = BehavioralEventType.TASK_RESCHEDULED_AI,
                        entityId = deferred.taskId,
                        category = task.category,
                        priority = task.priority,
                        date = dateString
                    )
                }
            }

            logBehavioralEvent(
                eventType = BehavioralEventType.AI_RECOMMENDATION_ACCEPTED,
                metadataJson = "{\"action\":\"APPLY_REBALANCE\",\"kept\":${result.keptTasks.size},\"deferred\":${result.deferredTasks.size}}",
                date = dateString
            )

            rewardXpAndFocus(40, 10f)
            _showRebalanceDialog.value = false
            _rebalanceResult.value = null
            analyzeDailyCapacity(dateString)
            generateMorningBriefing(dateString)
        }
    }

    fun dismissRebalanceDialog() {
        _showRebalanceDialog.value = false
        _rebalanceResult.value = null
    }

    fun openEveningReview(dateString: String = getTodayDateString()) {
        viewModelScope.launch {
            _isLoadingEveningReview.value = true
            _showEveningReviewDialog.value = true
            try {
                val profile = userProfile.value ?: repository.userProfile.firstOrNull() ?: UserProfileEntity()
                val dayTasks = allTasks.value.filter { it.date == dateString }
                val completed = dayTasks.filter { it.isCompleted }
                val pending = dayTasks.filter { !it.isCompleted }
                val habits = allHabits.value
                val habitsCompleted = habits.count { it.isCompleted }

                val summary = GeminiService.generateEveningReview(
                    userName = profile.name.takeIf { it.isNotBlank() } ?: "Julian",
                    coachPersonality = profile.coachPersonality,
                    completedTasks = completed,
                    pendingTasks = pending,
                    habitsCompletedCount = habitsCompleted,
                    totalHabitsCount = habits.size,
                    focusPointsEarned = profile.focusPoints
                )
                _eveningReviewSummary.value = summary
            } catch (e: Exception) {
                Log.e("LifeViewModel", "Failed to generate evening review", e)
            } finally {
                _isLoadingEveningReview.value = false
            }
        }
    }

    fun completeEveningReview(
        scoreRating: String,
        notes: String,
        rolledTaskIds: Set<Int>,
        dateString: String = getTodayDateString()
    ) {
        viewModelScope.launch {
            val dayTasks = allTasks.value.filter { it.date == dateString }
            val completedCount = dayTasks.count { it.isCompleted }
            val tomorrow = getTomorrowDateString()

            // Postpone selected incomplete tasks
            rolledTaskIds.forEach { taskId ->
                val task = dayTasks.find { it.id == taskId }
                if (task != null) {
                    repository.updateTask(
                        task.copy(
                            date = tomorrow,
                            isRollover = true,
                            rescheduleCount = task.rescheduleCount + 1,
                            status = "DEFERRED"
                        )
                    )
                }
            }

            // Save Daily Review record
            val review = DailyReviewEntity(
                date = dateString,
                scoreRating = scoreRating,
                summaryNotes = notes,
                completedCount = completedCount,
                deferredCount = rolledTaskIds.size,
                focusPointsEarned = userProfile.value?.focusPoints ?: 0f
            )
            repository.insertDailyReview(review)

            logBehavioralEvent(
                eventType = BehavioralEventType.EVENING_REVIEW_COMPLETED,
                metadataJson = "{\"scoreRating\":\"$scoreRating\",\"completed\":$completedCount,\"deferred\":${rolledTaskIds.size}}",
                date = dateString
            )
            logBehavioralEvent(
                eventType = BehavioralEventType.DAILY_RATING_SUBMITTED,
                metadataJson = "{\"scoreRating\":\"$scoreRating\"}",
                date = dateString
            )

            // Reward daily wrap-up bonus XP & Focus
            rewardXpAndFocus(100, 20f)
            _showEveningReviewDialog.value = false
            _eveningReviewSummary.value = null
        }
    }

    fun dismissEveningReviewDialog() {
        _showEveningReviewDialog.value = false
        _eveningReviewSummary.value = null
    }

    private fun onTimerFinished() {
        pauseTimer()
        val currentMode = _currentTimerMode.value

        // Check if Pomodoro notifications are enabled
        if (_pomodoroNotificationsEnabled.value) {
            val title = when (currentMode) {
                PomodoroMode.WORK -> "Work Session Completed! 🎯"
                PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> "Break Completed! ⚡"
            }
            val message = when (currentMode) {
                PomodoroMode.WORK -> {
                    val nextModeLabel = if (_completedRounds.value + 1 % 4 == 0) "Long Break" else "Short Break"
                    "Phenomenal job! You earned 100 XP & 10 Focus Points. Ready for a $nextModeLabel?"
                }
                PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                    "Time to focus! Let's get back to work and make progress on your goals."
                }
            }
            com.example.notification.SmartNotificationManager.sendNotification(getApplication(), title, message)
        }

        if (currentMode == PomodoroMode.WORK) {
            _completedRounds.value += 1
            viewModelScope.launch {
                rewardXpAndFocus(xpReward = 100, focusReward = 10f)
            }
            val nextMode = if (_completedRounds.value % 4 == 0) PomodoroMode.LONG_BREAK else PomodoroMode.SHORT_BREAK
            setTimerMode(nextMode)
        } else {
            setTimerMode(PomodoroMode.WORK)
        }
    }

    // --- Phase 7.5 Multi-Provider AI Management Methods ---
    fun selectAiProvider(providerId: String) {
        aiManager.keyStorage.setActiveProvider(providerId)
        _activeAiProviderId.value = providerId
        _activeAiModelId.value = aiManager.keyStorage.getActiveModel(providerId)
        _aiConnectionResult.value = null
    }

    fun selectAiModel(providerId: String, modelId: String) {
        aiManager.keyStorage.setActiveModel(providerId, modelId)
        if (providerId.equals(_activeAiProviderId.value, ignoreCase = true)) {
            _activeAiModelId.value = modelId
        }
        _aiConnectionResult.value = null
    }

    fun saveAiApiKey(providerId: String, apiKey: String) {
        aiManager.keyStorage.setApiKey(providerId, apiKey)
        _aiConnectionResult.value = null
    }

    fun saveCustomAiEndpoint(endpointUrl: String) {
        aiManager.keyStorage.setEndpointUrl(com.example.ai.ProviderType.CUSTOM.id, endpointUrl)
        _customAiEndpoint.value = endpointUrl
        _aiConnectionResult.value = null
    }

    fun testAiConnection(providerId: String? = null, modelId: String? = null) {
        val targetProvider = providerId ?: _activeAiProviderId.value
        val targetModel = modelId ?: _activeAiModelId.value
        viewModelScope.launch {
            _isTestingAiConnection.value = true
            try {
                val result = aiManager.testConnection(targetProvider, targetModel)
                _aiConnectionResult.value = result
            } catch (e: Exception) {
                _aiConnectionResult.value = com.example.ai.ConnectionTestResult(
                    isSuccess = false,
                    message = "Test exception: ${e.localizedMessage}",
                    modelTested = targetModel,
                    providerTested = targetProvider
                )
            } finally {
                _isTestingAiConnection.value = false
            }
        }
    }

    fun resetAiSettingsToDefaults() {
        aiManager.keyStorage.resetToDefaults()
        _activeAiProviderId.value = com.example.ai.ProviderType.GEMINI.id
        _activeAiModelId.value = "gemini-3.5-flash"
        _customAiEndpoint.value = com.example.ai.ProviderType.CUSTOM.defaultEndpoint
        _aiConnectionResult.value = null
    }
}

enum class PomodoroMode(val label: String, val defaultMinutes: Int) {
    WORK("Work Session", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

data class CalendarScheduleItem(
    val id: String,
    val title: String,
    val description: String,
    val type: ScheduleType,
    val startMinutes: Int,
    val durationMinutes: Int,
    val endMinutes: Int = startMinutes + durationMinutes,
    val timeSlotFormatted: String,
    val isCompleted: Boolean,
    val categoryOrDomain: String,
    val isShiftedForOverlap: Boolean = false,
    val taskId: Int? = null,
    val alarmId: Int? = null,
    val milestoneId: Int? = null
)

enum class ScheduleType {
    TASK,
    GRAND_VISION_MILESTONE,
    RECURRING_ALARM,
    SYSTEM_CALENDAR_EVENT
}

data class InterviewQuestion(
    val id: Int,
    val question: String,
    val subtitle: String = "Assessment",
    val options: List<String> = emptyList()
)

enum class JobScheduleType(
    val title: String,
    val defaultTimeSlot: String,
    val description: String,
    val badgeLabel: String
) {
    FULL_TIME(
        title = "Full-Time Job (9 AM - 5 PM)",
        defaultTimeSlot = "09:00 AM - 05:00 PM",
        description = "Standard 40h/wk daytime work shift on weekdays",
        badgeLabel = "FULL-TIME 💼"
    ),
    PART_TIME_MORNING(
        title = "Part-Time Morning (8 AM - 12 PM)",
        defaultTimeSlot = "08:00 AM - 12:00 PM",
        description = "Morning employment shift (afternoons free)",
        badgeLabel = "PART-TIME 🌅"
    ),
    PART_TIME_AFTERNOON(
        title = "Part-Time Afternoon (1 PM - 5 PM)",
        defaultTimeSlot = "01:00 PM - 05:00 PM",
        description = "Afternoon employment shift (mornings free)",
        badgeLabel = "PART-TIME 🌇"
    ),
    PART_TIME_EVENING(
        title = "Part-Time Evening (5 PM - 9 PM)",
        defaultTimeSlot = "05:00 PM - 09:00 PM",
        description = "Evening work shift (daytime free)",
        badgeLabel = "PART-TIME 🌙"
    ),
    FREELANCE_FLEXIBLE(
        title = "Freelance / Flexible (10 AM - 2 PM)",
        defaultTimeSlot = "10:00 AM - 02:00 PM",
        description = "Flexible client hours / self-employed blocks",
        badgeLabel = "FREELANCE 🌐"
    ),
    NO_JOB_FULL_FOCUS(
        title = "No Job / Full Focus on Vision",
        defaultTimeSlot = "",
        description = "Full daily bandwidth allocated to Grand Vision",
        badgeLabel = "FULL FOCUS 🎯"
    )
}

enum class OnboardingStep {
    WELCOME,
    GOOGLE_SIGN_IN,
    INTERESTS,
    AI_INTERVIEW,
    REVIEW_CONFIRM
}

data class ScheduleDayStatus(
    val isVacation: Boolean = false,
    val isWeekend: Boolean = false,
    val isWfh: Boolean = false,
    val isWorkDay: Boolean = true,
    val label: String = "",
    val tag: String = "WORK",
    val workHours: String = "09:00 - 17:00"
)



