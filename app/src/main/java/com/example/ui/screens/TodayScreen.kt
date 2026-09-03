package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.TaskEntity
import com.example.viewmodel.LifeViewModel
import com.example.ui.components.AdaptiveCapacityCard
import com.example.ui.components.AdaptiveRebalanceDialog
import com.example.ui.components.EveningReviewDialog
import com.example.ui.components.WhatShouldIDoNowCard
import com.example.ui.components.MorningBriefingCard
import com.example.ui.components.PlanDivergenceAlertCard
import com.example.ui.components.PredictiveRecommendationCard
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: LifeViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allGoals by viewModel.allGoals.collectAsState()

    // Phase 6 State Collections
    val capacityReport by viewModel.capacityReport.collectAsState()
    val isLoadingCapacity by viewModel.isLoadingCapacity.collectAsState()
    val rebalanceResult by viewModel.rebalanceResult.collectAsState()
    val isRebalancing by viewModel.isRebalancing.collectAsState()
    val showRebalanceDialog by viewModel.showRebalanceDialog.collectAsState()
    val eveningReviewSummary by viewModel.eveningReviewSummary.collectAsState()
    val isLoadingEveningReview by viewModel.isLoadingEveningReview.collectAsState()
    val showEveningReviewDialog by viewModel.showEveningReviewDialog.collectAsState()

    // Phase 8: Predictive AI & Proactive State Collectors
    val whatShouldIDoNow by viewModel.whatShouldIDoNow.collectAsState()
    val predictiveMorningBriefing by viewModel.predictiveMorningBriefing.collectAsState()
    val aiEnhancedBriefingText by viewModel.aiEnhancedBriefingText.collectAsState()
    val isGeneratingAiBriefing by viewModel.isGeneratingAiBriefing.collectAsState()
    val planDivergenceReport by viewModel.planDivergenceReport.collectAsState()
    val activeRecommendations by viewModel.activeRecommendations.collectAsState()
    val tomorrowPreview by viewModel.tomorrowPreview.collectAsState()
    val showTomorrowPreviewModal by viewModel.showTomorrowPreviewModal.collectAsState()

    val todayDateStr = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    val todayDisplayStr = remember {
        java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.getDefault()).format(java.util.Date())
    }

    LaunchedEffect(Unit) {
        viewModel.analyzeDailyCapacity(todayDateStr)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showAlarmsSheet by remember { mutableStateOf(false) }
    var showWorkScheduleDialog by remember { mutableStateOf(false) }
    var pickerTitle by remember { mutableStateOf("") }
    var pickerTarget by remember { mutableStateOf("BANNER") } // "AVATAR" or "BANNER"

    val todayTasks = allTasks.filter { it.date == todayDateStr }
    val completedCount = todayTasks.count { it.isCompleted }
    val totalCount = todayTasks.size
    val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = userProfile?.photoUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80"
                            ),
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Secondary.copy(alpha = 0.6f), CircleShape)
                                .clickable {
                                    pickerTitle = "Change Profile Picture"
                                    pickerTarget = "AVATAR"
                                    showImagePicker = true
                                },
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = todayDisplayStr,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    userProfile?.let { profile ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SolidSurface)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.navigateTo("PROFILE") }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = "Level",
                                tint = Tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "LVL ${profile.level}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                    }
                    IconButton(
                        onClick = { showAlarmsSheet = true },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("today_alarms_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Recurring Alarms",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo("COACH_CHAT") },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("today_coach_chat_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Chat with Coach",
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo("PROFILE") },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = SecondaryContainer,
                contentColor = OnSecondaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome back & Level Badge
            userProfile?.let { profile ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "WELCOME BACK",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "Good Morning, ${profile.name.substringBefore(" ")}",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                            color = OnSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LVL ${profile.level}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Tertiary
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(OnSurfaceVariant.copy(alpha = 0.4f))
                        )
                        Text(
                            text = "Disciplined",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            // Phase 8: Midday Plan Divergence Alert (if schedule has drifted)
            if (planDivergenceReport.isDiverged) {
                PlanDivergenceAlertCard(
                    report = planDivergenceReport,
                    onRebalance = { viewModel.requestAdaptiveRebalance("Plan Divergence Detected") }
                )
            }

            // Phase 8: What Should I Do Now? (Instant Actionable Guidance)
            WhatShouldIDoNowCard(
                result = whatShouldIDoNow,
                onStartTask = { taskId -> viewModel.startTaskFromRecommendation(taskId) },
                onChooseAnother = { viewModel.toggleAlternativeWhatShouldIDoNow() }
            )

            // Phase 8: Predictive Recommendations (Deduplicated Top 2-3 High Confidence items)
            if (activeRecommendations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    activeRecommendations.forEach { rec ->
                        PredictiveRecommendationCard(
                            recommendation = rec,
                            onAccept = {
                                viewModel.acceptPredictiveRecommendation(rec)
                                when (rec.actionType) {
                                    "REBALANCE" -> viewModel.requestAdaptiveRebalance(rec.title)
                                    "START_TASK" -> rec.relatedTaskId?.let { viewModel.startTaskFromRecommendation(it) }
                                    "VIEW_HABIT" -> viewModel.navigateTo("HABITS")
                                    else -> {}
                                }
                            },
                            onDismiss = { viewModel.dismissPredictiveRecommendation(rec) },
                            onFeedback = { fb -> viewModel.feedbackPredictiveRecommendation(rec, fb) }
                        )
                    }
                }
            }

            // Phase 8: Proactive Morning Briefing
            MorningBriefingCard(
                briefing = predictiveMorningBriefing,
                aiEnhancedText = aiEnhancedBriefingText,
                isLoadingAi = isGeneratingAiBriefing,
                onGenerateAiCoach = { viewModel.generateAiEnhancedMorningBriefing() }
            )

            // AI Morning Briefing Section
            val morningBriefing by viewModel.morningBriefing.collectAsState()
            val isLoadingMorningBriefing by viewModel.isLoadingMorningBriefing.collectAsState()
            val calendarPermissionGranted by viewModel.calendarPermissionGranted.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            colors = listOf(
                                Secondary.copy(alpha = 0.4f),
                                Tertiary.copy(alpha = 0.4f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Secondary.copy(alpha = 0.15f),
                                                Tertiary.copy(alpha = 0.15f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Briefing",
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "AI MORNING BRIEFING",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = OnSurface
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (calendarPermissionGranted) Color(0xFF4CAF50) else Color(0xFFFF9800))
                                    )
                                    Text(
                                        text = if (calendarPermissionGranted) "Calendar Synced" else "Calendar Disconnected",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.generateMorningBriefing(todayDateStr) },
                            modifier = Modifier.size(28.dp),
                            enabled = !isLoadingMorningBriefing
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate Briefing",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Schedule & Work Mode Badge
                    val dayScheduleStatus = viewModel.getScheduleStatusForDate(todayDateStr)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (dayScheduleStatus.isVacation) Tertiary.copy(alpha = 0.15f)
                                else if (dayScheduleStatus.isWfh) Secondary.copy(alpha = 0.15f)
                                else if (dayScheduleStatus.isWeekend) Tertiary.copy(alpha = 0.12f)
                                else SurfaceContainerHigh
                            )
                            .clickable { showWorkScheduleDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = when {
                                    dayScheduleStatus.isVacation -> Icons.Default.BeachAccess
                                    dayScheduleStatus.isWeekend -> Icons.Default.Weekend
                                    dayScheduleStatus.isWfh -> Icons.Default.HomeWork
                                    else -> Icons.Default.Business
                                },
                                contentDescription = null,
                                tint = when {
                                    dayScheduleStatus.isVacation -> Tertiary
                                    dayScheduleStatus.isWfh -> Secondary
                                    dayScheduleStatus.isWeekend -> Tertiary
                                    else -> Primary
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = dayScheduleStatus.label,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = OnSurface,
                                maxLines = 1
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Adjust",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Secondary
                            )
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Adjust Schedule",
                                tint = Secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (isLoadingMorningBriefing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Secondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Synthesizing schedule & priorities...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val briefingText = morningBriefing
                        if (briefingText.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Your personalized schedule, overlaps, and priority briefing is ready to synthesize.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.generateMorningBriefing(todayDateStr) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SecondaryContainer,
                                        contentColor = OnSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Synthesize",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("SYNTHESIZE BRIEFING")
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = briefingText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp
                                ),
                                color = OnSurface
                            )

                            if (!calendarPermissionGranted) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceContainerHigh)
                                        .clickable { viewModel.navigateTo("PLANNER") }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Calendar Connection Required",
                                        tint = Secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Sync calendar to detect meeting conflicts",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.Underline
                                        ),
                                        color = Secondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Go",
                                        tint = Secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceContainerHigh)
                                    .clickable { viewModel.navigateTo("COACH_CHAT") }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Discuss with Coach",
                                    tint = PrimaryFixedDim,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Discuss briefing with your Coach",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    color = PrimaryFixedDim,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Go",
                                    tint = PrimaryFixedDim,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Phase 6: Adaptive Capacity & Reality Engine Card
            AdaptiveCapacityCard(
                capacityReport = capacityReport,
                isLoading = isLoadingCapacity,
                onRefresh = { viewModel.analyzeDailyCapacity(todayDateStr) },
                onOpenRebalance = { viewModel.requestAdaptiveRebalance("Running Behind") },
                onOpenEveningReview = { viewModel.openEveningReview(todayDateStr) }
            )

            // AI Hero Suggestion (LifeOS Insight)
            val todayInsight by viewModel.todayInsight.collectAsState()
            val isLoadingInsight by viewModel.isLoadingInsight.collectAsState()
            val milestones by viewModel.selectedMilestones.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Insight",
                                tint = Secondary
                            )
                            Text(
                                text = "LIFEOS INSIGHT",
                                style = MaterialTheme.typography.labelMedium,
                                color = Secondary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.generateTodayInsight() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Insight",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isLoadingInsight) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Text(
                            text = todayInsight,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurface
                        )
                    }

                    Button(
                        onClick = {
                            val activeMilestone = milestones.firstOrNull { it.status == "ACTIVE" }
                            if (activeMilestone != null) {
                                viewModel.navigateTo("MILESTONE_CHECKIN", milestoneId = activeMilestone.id)
                            } else {
                                viewModel.navigateTo("PLANNER")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnSurface,
                            contentColor = BaseDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Open Focus Session",
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Arrow Forward",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // At a Glance section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "AT A GLANCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Today Progress Card
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DonutLarge,
                                    contentDescription = "Progress",
                                    tint = Secondary
                                )
                                Text(
                                    text = "$progressPercent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurface
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "TODAY",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                                LinearProgressIndicator(
                                    progress = { progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = Secondary,
                                    trackColor = SurfaceContainerHighest
                                )
                            }
                        }
                    }

                    // Day Streak Card
                    userProfile?.let { profile ->
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainer)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Error
                                    )
                                    Text(
                                        text = "${profile.streak}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurface
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "DAY STREAK",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OnSurfaceVariant
                                    )
                                    Text(
                                        text = "Keep it up!",
                                        style = TextStyle(fontSize = 12.sp, color = OnTertiaryContainer)
                                    )
                                }
                            }
                        }
                    }

                    // Focus Time Card
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = Tertiary
                                )
                                Text(
                                    text = "2.2h",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurface
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "FOCUS TIME",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = "Daily Avg: 3h",
                                    style = TextStyle(fontSize = 12.sp, color = OnSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }

            // Tasks list section ("What Matters Most")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHAT MATTERS MOST",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary,
                        modifier = Modifier.clickable { viewModel.navigateTo("PLANNER") }
                    )
                }

                if (todayTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer)
                            .clickable { showAddTaskDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Create your first focus task",
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        todayTasks.forEach { task ->
                            val accentColor = when (task.category) {
                                "WORK" -> Secondary
                                "HEALTH" -> Tertiary
                                "REPLY" -> Secondary
                                "ADMIN" -> OnSurfaceVariant
                                else -> Primary
                            }

                            SwipeToDeleteTask(
                                task = task,
                                onDelete = {
                                    viewModel.deleteTask(task)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted: ${task.title}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreTask(task)
                                        }
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SurfaceContainer)
                                        .border(
                                            1.dp,
                                            if (task.isCompleted) Color.Transparent else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.toggleTaskCompleted(task) }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(accentColor.copy(alpha = 0.1f))
                                                .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (task.category) {
                                                    "WORK" -> Icons.Default.Description
                                                    "HEALTH" -> Icons.Default.FitnessCenter
                                                    "REPLY" -> Icons.Default.ChatBubbleOutline
                                                    "ADMIN" -> Icons.Default.ReceiptLong
                                                    else -> Icons.Default.TaskAlt
                                                },
                                                contentDescription = task.category,
                                                tint = accentColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                                                ),
                                                color = if (task.isCompleted) OnSurfaceVariant.copy(alpha = 0.6f) else OnSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(accentColor.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = task.category,
                                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                                        color = accentColor
                                                    )
                                                }

                                                // Priority Badge
                                                val priorityColor = when (task.priority) {
                                                    "CRITICAL" -> Error
                                                    "IMPORTANT" -> Secondary
                                                    "FLEXIBLE" -> Tertiary
                                                    else -> OnSurfaceVariant
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(priorityColor.copy(alpha = 0.15f))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = task.priority,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        color = priorityColor
                                                    )
                                                }

                                                if (task.rescheduleCount > 0) {
                                                    Text(
                                                        text = "• Rolled (${task.rescheduleCount}x)",
                                                        style = TextStyle(fontSize = 11.sp, color = Warning, fontWeight = FontWeight.Medium)
                                                    )
                                                }

                                                Text(
                                                    text = task.timeSlot,
                                                    style = TextStyle(fontSize = 12.sp, color = OnSurfaceVariant),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (!task.isCompleted) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.postponeTaskToTomorrow(task)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Deferred to tomorrow: ${task.title}")
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ScheduleSend,
                                                    contentDescription = "Postpone to tomorrow",
                                                    tint = Tertiary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.toggleTaskCompleted(task) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = "Task State",
                                                tint = if (task.isCompleted) Tertiary else OnSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteTask(task) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Task",
                                                tint = Error.copy(alpha = 0.6f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Experience / Level Progress Card
            userProfile?.let { profile ->
                val progressXp = if (profile.maxXp > 0) profile.xp / profile.maxXp.toFloat() else 0f
                val progressPercent = (progressXp * 100).toInt()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = "Level Progress",
                                    tint = Tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "LEVEL PROGRESS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurface
                                )
                            }
                            Text(
                                text = "${profile.xp} / ${profile.maxXp} XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressXp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(SecondaryContainer, Tertiary)
                                        )
                                    )
                            )
                        }

                        val remainingXp = profile.maxXp - profile.xp
                        Text(
                            text = "$remainingXp XP to Level ${profile.level + 1} (High Achiever)",
                            style = TextStyle(fontSize = 12.sp, color = OnSurfaceVariant),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Grand Visions & Objectives Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Goals",
                            tint = Secondary
                        )
                        Text(
                            text = "GRAND VISIONS & GOALS",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                    }
                    Text(
                        text = "+ Add Goal",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Secondary,
                        modifier = Modifier.clickable { viewModel.navigateTo("DEFINE_GOAL") }
                    )
                }

                if (allGoals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .clickable { viewModel.navigateTo("DEFINE_GOAL") }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "No goals",
                                tint = OnSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No grand visions defined yet.",
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurface
                            )
                            Text(
                                text = "Define a goal to let LifeOS auto-architect your weekly check-ins and milestones.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+ Define First Goal",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Secondary
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        allGoals.forEach { goal ->
                            val goalColor = when (goal.domain) {
                                "Career" -> Secondary
                                "Health" -> Tertiary
                                "Wealth" -> Error
                                else -> Primary
                            }

                            val goalIcon = when (goal.domain) {
                                "Career" -> Icons.Default.Architecture
                                "Health" -> Icons.Default.FitnessCenter
                                "Wealth" -> Icons.Default.Payments
                                else -> Icons.Default.MenuBook
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceContainer)
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.navigateTo("MILESTONE_PLAN", goalId = goal.id) }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(goalColor.copy(alpha = 0.12f))
                                            .border(1.dp, goalColor.copy(alpha = 0.25f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = goalIcon,
                                            contentDescription = goal.domain,
                                            tint = goalColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(goalColor.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = goal.domain.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = goalColor
                                                    )
                                                }
                                                Text(
                                                    text = goal.horizon,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = OnSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "${goal.progressPercent}%",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = OnSurface
                                            )
                                        }

                                        Text(
                                            text = goal.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            LinearProgressIndicator(
                                                progress = { goal.progressPercent / 100f },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(6.dp)
                                                    .clip(CircleShape),
                                                color = goalColor,
                                                trackColor = SurfaceContainerHighest
                                            )
                                            Text(
                                                text = goal.targetTimeline,
                                                style = TextStyle(fontSize = 11.sp, color = OnSurfaceVariant),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "View Details",
                                        tint = OnSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Atmospheric Vibe Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainer)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = userProfile?.todayBannerUrl ?: "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80"
                    ),
                    contentDescription = "Atmospheric Studio Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )

                // Atmospheric Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Edit Banner Button
                IconButton(
                    onClick = {
                        pickerTitle = "Customize Today Banner Photo"
                        pickerTarget = "BANNER"
                        showImagePicker = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Change Banner Picture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CURRENT VIBE",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurface.copy(alpha = 0.8f),
                        letterSpacing = 0.15.sp
                    )
                    Text(
                        text = "Deep Work & Clarity",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showImagePicker) {
        com.example.ui.components.ImagePickerDialog(
            title = pickerTitle,
            currentImageUrl = if (pickerTarget == "AVATAR") userProfile?.photoUrl else userProfile?.todayBannerUrl,
            onDismiss = { showImagePicker = false },
            onImageSelected = { newUrl ->
                if (pickerTarget == "AVATAR") {
                    viewModel.updateProfilePhoto(newUrl)
                } else {
                    viewModel.updateTabBanner("TODAY", newUrl)
                }
            }
        )
    }

    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskCategory by remember { mutableStateOf("WORK") }
        var taskTimeSlot by remember { mutableStateOf("09:00 - 10:30 AM") }
        var taskDescription by remember { mutableStateOf("") }
        var taskPriority by remember { mutableStateOf("IMPORTANT") }
        var taskEnergyLevel by remember { mutableStateOf("MEDIUM") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = SolidSurface,
            title = {
                Text("New Focus Task", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title", color = OnSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Description", color = OnSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector
                    Text("Category", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("WORK", "HEALTH", "REPLY", "ADMIN").forEach { category ->
                            val isSelected = taskCategory == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SecondaryContainer else SurfaceContainerHigh)
                                    .clickable { taskCategory = category }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                    color = if (isSelected) OnSecondaryContainer else OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Priority Selector
                    Text("Priority Level", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("CRITICAL", "IMPORTANT", "FLEXIBLE", "OPTIONAL").forEach { priority ->
                            val isSelected = taskPriority == priority
                            val pColor = when (priority) {
                                "CRITICAL" -> Error
                                "IMPORTANT" -> Secondary
                                "FLEXIBLE" -> Tertiary
                                else -> OnSurfaceVariant
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) pColor.copy(alpha = 0.2f) else SurfaceContainerHigh)
                                    .border(1.dp, if (isSelected) pColor else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { taskPriority = priority }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = priority,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) pColor else OnSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = taskTimeSlot,
                        onValueChange = { taskTimeSlot = it },
                        label = { Text("Time Slot", color = OnSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            viewModel.addTask(
                                title = taskTitle,
                                category = taskCategory,
                                timeSlot = taskTimeSlot,
                                description = taskDescription,
                                date = todayDateStr,
                                priority = taskPriority,
                                energyLevel = taskEnergyLevel
                            )
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("ADD TASK", color = Secondary, style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("CANCEL", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }

    if (showRebalanceDialog) {
        AdaptiveRebalanceDialog(
            rebalanceResult = rebalanceResult,
            isRebalancing = isRebalancing,
            onCalculateRebalance = { trigger ->
                viewModel.requestAdaptiveRebalance(trigger)
            },
            onApplyRebalance = {
                viewModel.applyRebalancePlan()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Day successfully rebalanced with AI (+50 XP)!")
                }
            },
            onDismiss = {
                viewModel.dismissRebalanceDialog()
            }
        )
    }

    if (showEveningReviewDialog) {
        val pending = todayTasks.filter { !it.isCompleted }
        EveningReviewDialog(
            eveningSummary = eveningReviewSummary,
            pendingTasks = pending,
            isLoading = isLoadingEveningReview,
            onCompleteReview = { scoreRating, notes, rolledIds ->
                viewModel.completeEveningReview(
                    scoreRating = scoreRating,
                    notes = notes,
                    rolledTaskIds = rolledIds,
                    dateString = todayDateStr
                )
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Daily Review complete! +100 XP awarded.")
                }
            },
            onDismiss = {
                viewModel.dismissEveningReviewDialog()
            }
        )
    }

    if (showTomorrowPreviewModal) {
        com.example.ui.components.TomorrowPreviewDialog(
            report = tomorrowPreview,
            onDismiss = { viewModel.setTomorrowPreviewModalVisible(false) },
            onRebalanceTomorrow = { viewModel.requestAdaptiveRebalance("Tomorrow Overload Risk") }
        )
    }

    if (showAlarmsSheet) {
        com.example.ui.components.RecurringAlarmsBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAlarmsSheet = false }
        )
    }

    if (showWorkScheduleDialog) {
        val current = userProfile ?: com.example.data.UserProfileEntity()
        com.example.ui.components.WorkScheduleDialog(
            currentProfile = current,
            onDismiss = { showWorkScheduleDialog = false },
            onSaveSchedule = { start, end, wfh, workDays, weekends, vacationMode, vStart, vEnd, vNotes ->
                viewModel.updateWorkSchedule(
                    workStartTime = start,
                    workEndTime = end,
                    wfhDays = wfh,
                    workDays = workDays,
                    weekendDays = weekends
                )
                viewModel.updateVacationSettings(
                    isVacationMode = vacationMode,
                    startDate = vStart,
                    endDate = vEnd,
                    notes = vNotes
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteTask(
    task: TaskEntity,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                val color = Error.copy(alpha = 0.85f)
                val alignment = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
                val icon = Icons.Default.Delete

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            content()
        }
    )
}
