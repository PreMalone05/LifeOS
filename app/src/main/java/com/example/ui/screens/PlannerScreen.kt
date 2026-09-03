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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.viewmodel.LifeViewModel
import com.example.ui.components.AdaptiveRebalanceDialog
import com.example.ui.components.EveningReviewDialog
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(viewModel: LifeViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    var selectedDate by remember { mutableStateOf(viewModel.getTodayDateString()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showSchedulePhaseTasksDialog by remember { mutableStateOf(false) }
    var showWorkScheduleDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var pickerTitle by remember { mutableStateOf("") }
    var pickerTarget by remember { mutableStateOf("BANNER") } // "AVATAR", "BANNER", "GOAL"
    var selectedGoalId by remember { mutableStateOf<Int?>(null) }

    // Phase 6 State Collections
    val rebalanceResult by viewModel.rebalanceResult.collectAsState()
    val isRebalancing by viewModel.isRebalancing.collectAsState()
    val showRebalanceDialog by viewModel.showRebalanceDialog.collectAsState()
    val eveningReviewSummary by viewModel.eveningReviewSummary.collectAsState()
    val isLoadingEveningReview by viewModel.isLoadingEveningReview.collectAsState()
    val showEveningReviewDialog by viewModel.showEveningReviewDialog.collectAsState()

    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val calendarPermissionGranted by viewModel.calendarPermissionGranted.collectAsState()
    val isLoadingCalendarEvents by viewModel.isLoadingCalendarEvents.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val hasCal = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        viewModel.setCalendarPermissionGranted(hasCal)
    }

    LaunchedEffect(selectedDate, calendarPermissionGranted) {
        if (calendarPermissionGranted) {
            viewModel.loadCalendarEvents(selectedDate)
        }
    }

    val dayTasks = allTasks.filter { it.date == selectedDate }

    val allGoals by viewModel.allGoals.collectAsState()
    val allRecurringAlarms by viewModel.allRecurringAlarms.collectAsState()

    val unifiedSchedule = remember(selectedDate, dayTasks, allGoals, allRecurringAlarms, calendarEvents) {
        viewModel.getUnifiedScheduleForDate(selectedDate, dayTasks, allGoals, allRecurringAlarms, calendarEvents)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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
                            contentDescription = "Profile",
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
                            text = "Planner",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = OnSurfaceVariant
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
                    contentDescription = "Add Item",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Planner Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainer)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = userProfile?.plannerBannerUrl ?: "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=600&auto=format&fit=crop&q=80"
                    ),
                    contentDescription = "Planner Cover Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                )
                IconButton(
                    onClick = {
                        pickerTitle = "Customize Planner Banner Photo"
                        pickerTarget = "PLANNER_BANNER"
                        showImagePicker = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Change Banner",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "EXECUTIVE PLANNER",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary
                    )
                    Text(
                        text = "Master Your Schedule",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Week Picker Row
            val monthTitle = remember(selectedDate) {
                try {
                    val sdfKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    val parsed = sdfKey.parse(selectedDate)
                    if (parsed != null) {
                        java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(parsed).uppercase(java.util.Locale.getDefault())
                    } else {
                        java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()).uppercase(java.util.Locale.getDefault())
                    }
                } catch (e: Exception) {
                    "PLANNER"
                }
            }

            val currentWeekDays = remember {
                val calendar = java.util.Calendar.getInstance()
                calendar.firstDayOfWeek = java.util.Calendar.MONDAY
                calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
                val sdfKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val sdfDay = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                val sdfNum = java.text.SimpleDateFormat("d", java.util.Locale.getDefault())

                (0..6).map {
                    val date = calendar.time
                    val key = sdfKey.format(date)
                    val dayLabel = sdfDay.format(date).uppercase(java.util.Locale.getDefault())
                    val dayNum = sdfNum.format(date)
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                    Triple(key, dayLabel, dayNum)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Month Calendar",
                        tint = SecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentWeekDays.forEach { (dateKey, dayLabel, dayNum) ->
                        val isSelected = dateKey == selectedDate
                        val hasTasksOnDay = allTasks.any { it.date == dateKey && !it.isCompleted }

                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) SecondaryContainer else SurfaceContainerLow)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedDate = dateKey }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                    color = if (isSelected) OnSecondaryContainer else OnSurfaceVariant
                                )
                                Text(
                                    text = dayNum,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                                    color = if (isSelected) OnSecondaryContainer else OnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Notification dot on days with pending tasks
                            if (hasTasksOnDay && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 4.dp, end = 4.dp)
                                        .clip(CircleShape)
                                        .background(Error)
                                )
                            }
                        }
                    }
                }

                // Schedule & Work Status for selected date
                val dateScheduleStatus = viewModel.getScheduleStatusForDate(selectedDate)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (dateScheduleStatus.isVacation) Tertiary.copy(alpha = 0.15f)
                            else if (dateScheduleStatus.isWfh) Secondary.copy(alpha = 0.15f)
                            else if (dateScheduleStatus.isWeekend) Tertiary.copy(alpha = 0.12f)
                            else SurfaceContainerLow
                        )
                        .border(
                            1.dp,
                            if (dateScheduleStatus.isVacation) Tertiary.copy(alpha = 0.3f)
                            else if (dateScheduleStatus.isWfh) Secondary.copy(alpha = 0.3f)
                            else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showWorkScheduleDialog = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = when {
                                dateScheduleStatus.isVacation -> Icons.Default.BeachAccess
                                dateScheduleStatus.isWeekend -> Icons.Default.Weekend
                                dateScheduleStatus.isWfh -> Icons.Default.HomeWork
                                else -> Icons.Default.Business
                            },
                            contentDescription = null,
                            tint = when {
                                dateScheduleStatus.isVacation -> Tertiary
                                dateScheduleStatus.isWfh -> Secondary
                                dateScheduleStatus.isWeekend -> Tertiary
                                else -> Primary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = dateScheduleStatus.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = OnSurface
                            )
                            Text(
                                text = if (dateScheduleStatus.isVacation) "Vacation Mode • Streak protected"
                                else if (dateScheduleStatus.isWfh) "Remote Day • Deep work focus"
                                else if (dateScheduleStatus.isWeekend) "Weekly Rest Day • Casual cadence"
                                else "Standard Office Routine • In-person",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Tune",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Secondary
                        )
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Adjust",
                            tint = Secondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Roll-over Notification Banner
            val rolloverCount = allTasks.count { it.isRollover && !it.isCompleted }
            val todayDate = viewModel.getTodayDateString()
            if (rolloverCount > 0 && selectedDate == todayDate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.navigateTo("MILESTONE_CHECKIN", milestoneId = 9) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Rollover",
                                tint = Error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "UNFINISHED YESTERDAY",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = Error
                            )
                            Text(
                                text = "$rolloverCount tasks rolled over to today",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = OnSurface
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Rollovers list",
                        tint = OnSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // AI GRAND VISION & JOB SCHEDULER BANNER
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Secondary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Planner",
                                tint = Secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "AI Vision Phase Planner",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "Step-by-step tasks scheduled around your job",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showSchedulePhaseTasksDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryContainer,
                            contentColor = OnSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("PLAN PHASE", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Overlap & Deconfliction Header Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UNIFIED CALENDAR SCHEDULE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = "Grand Vision tasks & alarms deconflicted",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.requestAdaptiveRebalance("Schedule Shift") },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Rebalance",
                            tint = Secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("REBALANCE", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Secondary)
                    }

                    Button(
                        onClick = {
                            viewModel.deconflictAndSaveScheduleToDatabase(selectedDate)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Schedule deconflicted! Shifted overlapping items forward by 5 min.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryContainer,
                            contentColor = OnSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Deconflict",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("DECONFLICT", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Timeline chronological vertical blocks from unifiedSchedule
            if (unifiedSchedule.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "Empty",
                            tint = OnSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No focus blocks, alarms or vision tasks for today.",
                            color = OnSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    unifiedSchedule.forEach { item ->
                        val (leftColor, typeBadgeLabel, badgeBg) = when {
                            item.title.contains("🏢") || item.title.contains("Job Work") || item.title.contains("Core Work") ->
                                Triple(Primary, "JOB SHIFT 🏢", Primary.copy(alpha = 0.15f))
                            item.title.startsWith("Phase") || item.title.contains("Phase Step") ->
                                Triple(Secondary, "GRAND VISION 🚀", Secondary.copy(alpha = 0.15f))
                            item.type == com.example.viewmodel.ScheduleType.GRAND_VISION_MILESTONE ->
                                Triple(Secondary, "GRAND VISION", Secondary.copy(alpha = 0.15f))
                            item.type == com.example.viewmodel.ScheduleType.RECURRING_ALARM ->
                                Triple(Error, "RECURRING ALARM ⏰", Error.copy(alpha = 0.15f))
                            item.type == com.example.viewmodel.ScheduleType.TASK ->
                                Triple(SecondaryContainer, "TASK", SecondaryContainer.copy(alpha = 0.2f))
                            item.type == com.example.viewmodel.ScheduleType.SYSTEM_CALENDAR_EVENT ->
                                Triple(Tertiary, "CALENDAR", Tertiary.copy(alpha = 0.15f))
                            else ->
                                Triple(SecondaryContainer, "TASK", SecondaryContainer.copy(alpha = 0.2f))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Timeslot Column
                            Column(
                                modifier = Modifier
                                    .width(68.dp)
                                    .padding(top = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                val timeOnly = item.timeSlotFormatted.substringBefore(" -")
                                Text(
                                    text = timeOnly,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (item.isShiftedForOverlap) Error else OnSurfaceVariant,
                                    textAlign = TextAlign.End
                                )
                                if (item.isShiftedForOverlap) {
                                    Text(
                                        text = "+5m shift",
                                        style = TextStyle(fontSize = 9.sp, color = Error, fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceContainer)
                                    .border(
                                        1.dp,
                                        if (item.isShiftedForOverlap) Error.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                // Side Border Color Block
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .align(Alignment.CenterStart)
                                        .background(leftColor)
                                )

                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else null
                                                ),
                                                color = if (item.isCompleted) OnSurfaceVariant.copy(alpha = 0.6f) else OnSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(badgeBg)
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = typeBadgeLabel,
                                                style = TextStyle(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = leftColor
                                                )
                                            )
                                        }
                                    }

                                    if (item.description.isNotBlank()) {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.timeSlotFormatted,
                                            style = TextStyle(fontSize = 11.sp, color = OnSurfaceVariant.copy(alpha = 0.8f))
                                        )

                                        if (item.isShiftedForOverlap) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Deconflicted",
                                                    tint = Error,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Non-Overlapping Shift",
                                                    style = TextStyle(fontSize = 10.sp, color = Error, fontWeight = FontWeight.SemiBold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Integration Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALENDAR EVENTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    if (calendarPermissionGranted) {
                        IconButton(
                            onClick = { viewModel.loadCalendarEvents(selectedDate) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Calendar",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (!calendarPermissionGranted) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        viewModel.setCalendarPermissionGranted(isGranted)
                        if (isGranted) {
                            viewModel.loadCalendarEvents(selectedDate)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar Connect",
                                tint = Secondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Connect Local Calendar App",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "See your meetings, events, and schedules directly inside your Planner screen.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecondaryContainer,
                                    contentColor = OnSecondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("GRANT CALENDAR ACCESS")
                            }
                        }
                    }
                } else {
                    if (isLoadingCalendarEvents) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Secondary, modifier = Modifier.size(24.dp))
                        }
                    } else if (calendarEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceContainer)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = "No Events",
                                    tint = OnSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "No local calendar events found for this date.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            calendarEvents.forEach { event ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(SurfaceContainerLow)
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(Secondary)
                                                )
                                                Text(
                                                    text = event.title,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = OnSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = event.formattedTime,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = OnSurfaceVariant
                                            )
                                            if (!event.location.isNullOrBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Place,
                                                        contentDescription = "Location",
                                                        tint = OnSurfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = event.location,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = OnSurfaceVariant.copy(alpha = 0.8f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Button to Import this event as a local LifeOS task block
                                        IconButton(
                                            onClick = {
                                                viewModel.importCalendarEventAsTask(event)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Imported '${event.title}' to Planner!")
                                                }
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(SurfaceContainerHigh)
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Import Event",
                                                tint = Secondary,
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

            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showImagePicker) {
        com.example.ui.components.ImagePickerDialog(
            title = pickerTitle,
            currentImageUrl = when (pickerTarget) {
                "AVATAR" -> userProfile?.photoUrl
                "PLANNER_BANNER" -> userProfile?.plannerBannerUrl
                else -> userProfile?.plannerBannerUrl
            },
            onDismiss = { showImagePicker = false },
            onImageSelected = { newUrl ->
                when (pickerTarget) {
                    "AVATAR" -> viewModel.updateProfilePhoto(newUrl)
                    "PLANNER_BANNER" -> viewModel.updateTabBanner("PLANNER", newUrl)
                    "GOAL" -> {
                        selectedGoalId?.let { id ->
                            viewModel.updateGoalVisionImage(id, newUrl)
                        }
                    }
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
                Text("New Planner Block", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
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
                                date = selectedDate,
                                priority = taskPriority,
                                energyLevel = taskEnergyLevel
                            )
                            showAddTaskDialog = false
                        }
                    }
                ) {
                    Text("ADD BLOCK", color = Secondary, style = MaterialTheme.typography.labelMedium)
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
        val pending = dayTasks.filter { !it.isCompleted }
        EveningReviewDialog(
            eveningSummary = eveningReviewSummary,
            pendingTasks = pending,
            isLoading = isLoadingEveningReview,
            onCompleteReview = { scoreRating, notes, rolledIds ->
                viewModel.completeEveningReview(
                    scoreRating = scoreRating,
                    notes = notes,
                    rolledTaskIds = rolledIds,
                    dateString = selectedDate
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

    if (showSchedulePhaseTasksDialog) {
        val allGoals by viewModel.allGoals.collectAsState()
        val allMilestones by viewModel.selectedMilestones.collectAsState()
        val selectedGoalId by viewModel.selectedGoalId.collectAsState()
        val goal = allGoals.find { it.id == selectedGoalId } ?: allGoals.firstOrNull()

        com.example.ui.components.SchedulePhaseTasksDialog(
            viewModel = viewModel,
            goal = goal,
            milestones = allMilestones,
            onDismiss = { showSchedulePhaseTasksDialog = false },
            onTasksScheduled = { count ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Added $count daily phase tasks & job blocks to your Planner!")
                }
            }
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
