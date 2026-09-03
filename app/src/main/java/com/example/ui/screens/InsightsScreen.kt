package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: LifeViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val insightsAIEvaluation by viewModel.insightsAIEvaluation.collectAsState()
    val isLoadingInsightsAI by viewModel.isLoadingInsightsAI.collectAsState()

    // Phase 7 States
    val personalCapacity by viewModel.personalCapacityModel.collectAsState()
    val planningAccuracy by viewModel.planningAccuracyReport.collectAsState()
    val productivityPatterns by viewModel.productivityPatternsReport.collectAsState()
    val personalizedInsights by viewModel.personalizedInsights.collectAsState()
    val isLoadingPersonalizedAI by viewModel.isLoadingPersonalizedAI.collectAsState()
    val currentTab by viewModel.currentPatternsTab.collectAsState()
    val whyDialogItem by viewModel.showInsightWhyDialog.collectAsState()

    var showImagePicker by remember { mutableStateOf(false) }
    var pickerTitle by remember { mutableStateOf("") }
    var pickerTarget by remember { mutableStateOf("INSIGHTS_BANNER") }

    val completedTasks = allTasks.filter { it.isCompleted }
    val completedThisWeek = completedTasks.size
    val totalTasks = allTasks.size
    val productivityScore = if (totalTasks > 0) (completedTasks.size * 100) / totalTasks else 100

    Scaffold(
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
                            contentDescription = "User Portrait",
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
                        Column {
                            Text(
                                text = "Task Analytics & AI Engine",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = if (currentTab == "MY_PATTERNS") "Phase 7 Learning Engine" else "Historical Execution Data",
                                style = MaterialTheme.typography.labelSmall,
                                color = Secondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo("PLANNER") },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
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
            // Insights Hero Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainer)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = userProfile?.insightsBannerUrl ?: "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=600&auto=format&fit=crop&q=80"
                    ),
                    contentDescription = "Analytics Banner Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.65f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )
                IconButton(
                    onClick = {
                        pickerTitle = "Customize Insights Banner Photo"
                        pickerTarget = "INSIGHTS_BANNER"
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
                        contentDescription = "Change Insights Banner",
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
                        text = "BEHAVIORAL LEARNING & ANALYTICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary
                    )
                    Text(
                        text = if (currentTab == "MY_PATTERNS") "Personal Capacity & Patterns" else "Data-Driven Momentum",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Tab Selector: Overview vs My Patterns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    title = "OVERVIEW",
                    icon = Icons.Default.BarChart,
                    isSelected = (currentTab == "OVERVIEW"),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setPatternsTab("OVERVIEW") }
                )
                TabButton(
                    title = "MY PATTERNS 🧠",
                    icon = Icons.Default.Psychology,
                    isSelected = (currentTab == "MY_PATTERNS"),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setPatternsTab("MY_PATTERNS") }
                )
            }

            if (currentTab == "OVERVIEW") {
                // Circular Score gauge
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(192.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.05f),
                                radius = size.minDimension / 2 - 8.dp.toPx(),
                                style = Stroke(width = 10.dp.toPx())
                            )

                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(Secondary, Tertiary)
                                ),
                                startAngle = -90f,
                                sweepAngle = (productivityScore / 100f) * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$productivityScore%",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp),
                                color = OnSurface
                            )
                            Text(
                                text = "PRODUCTIVITY SCORE",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = OnSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$completedThisWeek Tasks Completed",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Trend up",
                                tint = Tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Based on $totalTasks total tasks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                // Peak Performance custom chart
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "PEAK PERFORMANCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Tertiary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Flow Window: ${productivityPatterns.mostProductiveHours}",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = Tertiary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val heights = listOf(0.12f, 0.18f, 0.15f, 0.45f, 0.85f, 0.95f, 0.80f, 0.50f, 0.35f, 0.25f, 0.20f, 0.15f)
                            heights.forEachIndexed { idx, height ->
                                val isFlowState = idx in 4..6 // 9:00 to 11:00 range
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(height)
                                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                        .background(if (isFlowState) Secondary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.05f))
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("06:00", "09:00", "11:00", "14:00", "18:00", "22:00").forEach { label ->
                            val isFlow = label == "09:00" || label == "11:00"
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = if (isFlow) Secondary else OnSurfaceVariant
                            )
                        }
                    }
                }

                // Category Distribution
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "CATEGORY DISTRIBUTION",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )

                    val categoriesList = listOf("WORK", "HEALTH", "REPLY", "ADMIN")
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        categoriesList.forEach { cat ->
                            val catTasks = allTasks.filter { it.category.uppercase() == cat }
                            val catCount = catTasks.size
                            val catColor = when (cat) {
                                "WORK" -> Color(0xFF0566D9)
                                "HEALTH" -> Color(0xFF4EDEA3)
                                "REPLY" -> Color(0xFF9C27B0)
                                else -> Color(0xFFFFD700)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "$catCount tasks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.05f))
                                ) {
                                    val weight = if (totalTasks > 0) catCount.toFloat() / totalTasks else 0f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(if (weight > 0f) weight else 0.01f)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                }
                            }
                        }
                    }
                }

                // Rollover insights
                val rolloverCount = allTasks.count { it.isRollover }
                val rolloverRate = if (totalTasks > 0) (rolloverCount * 100) / totalTasks else 0

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "EFFICIENCY & ROLLOVER",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .align(Alignment.CenterStart)
                                .background(Secondary)
                        )

                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "$rolloverRate% Rollover Rate",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Secondary
                                    )
                                    Text(
                                        text = "$rolloverCount tasks rolled over from previous sessions",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.generateInsightsAI() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Insights",
                                        tint = OnSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Secondary.copy(alpha = 0.05f))
                                    .border(1.dp, Secondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                if (isLoadingInsightsAI) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = insightsAIEvaluation ?: "No insights generated yet. Execute some tasks to analyze patterns.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurface,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Completion Velocity Grid
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "COMPLETION VELOCITY",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            VelocityCard(
                                label = "WORK AVG.",
                                value = "${(personalCapacity.workdayCapacityHours * 60 / 6).toInt().coerceIn(25, 60)}m",
                                icon = Icons.Default.Speed,
                                iconColor = Color(0xFF0566D9)
                            )
                            VelocityCard(
                                label = "GROWTH AVG.",
                                value = "45m",
                                icon = Icons.Default.RocketLaunch,
                                iconColor = Color(0xFF9C27B0)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            VelocityCard(
                                label = "HEALTH AVG.",
                                value = "20m",
                                icon = Icons.Default.Timer,
                                iconColor = Color(0xFF4EDEA3)
                            )
                            VelocityCard(
                                label = "ADMIN AVG.",
                                value = "15m",
                                icon = Icons.Default.Payments,
                                iconColor = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // PHASE 7: "MY PATTERNS" ENGINE TAB
                // ==========================================
                MyPatternsEngineView(
                    viewModel = viewModel,
                    personalCapacity = personalCapacity,
                    planningAccuracy = planningAccuracy,
                    productivityPatterns = productivityPatterns,
                    personalizedInsights = personalizedInsights,
                    isLoadingPersonalizedAI = isLoadingPersonalizedAI
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    // "Why this recommendation?" Explanation Modal
    whyDialogItem?.let { item ->
        InsightWhyDialog(
            item = item,
            onDismiss = { viewModel.showWhyExplanation(null) }
        )
    }

    if (showImagePicker) {
        com.example.ui.components.ImagePickerDialog(
            title = pickerTitle,
            currentImageUrl = if (pickerTarget == "AVATAR") userProfile?.photoUrl else userProfile?.insightsBannerUrl,
            onDismiss = { showImagePicker = false },
            onImageSelected = { newUrl ->
                if (pickerTarget == "AVATAR") {
                    viewModel.updateProfilePhoto(newUrl)
                } else {
                    viewModel.updateTabBanner("INSIGHTS", newUrl)
                }
            }
        )
    }
}

@Composable
fun TabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else OnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color.White else OnSurfaceVariant
            )
        }
    }
}

@Composable
fun MyPatternsEngineView(
    viewModel: LifeViewModel,
    personalCapacity: PersonalCapacityModel,
    planningAccuracy: PlanningAccuracyReport,
    productivityPatterns: ProductivityPatternsReport,
    personalizedInsights: List<PersonalizedInsightItem>,
    isLoadingPersonalizedAI: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // AI Learning Status & Calibration Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SurfaceContainer,
                            SurfaceContainerHighest.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(1.dp, Secondary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4EDEA3))
                        )
                        Text(
                            text = "AI LEARNING ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4EDEA3),
                            letterSpacing = 1.sp
                        )
                    }

                    ConfidenceBadge(confidence = personalCapacity.confidence)
                }

                Text(
                    text = "LifeOS analyzes your planned vs actual execution to progressively calibrate capacity and predict realistic durations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confidence Level: ${personalCapacity.confidence.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary
                    )

                    Button(
                        onClick = { viewModel.generatePersonalizedInsightsAI() },
                        enabled = !isLoadingPersonalizedAI,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("run_learning_cycle_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        if (isLoadingPersonalizedAI) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Run Learning",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Re-Calibrate",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 1. Personal Capacity Model Card
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "1. PERSONAL CAPACITY MODEL",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CapacityMetricBox(
                            title = "REALISTIC CAPACITY",
                            value = "${String.format(java.util.Locale.US, "%.1f", personalCapacity.averageRealisticDailyHours)}h",
                            subtitle = "Daily planned sweet spot",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        CapacityMetricBox(
                            title = "FOCUS CAPACITY",
                            value = "${String.format(java.util.Locale.US, "%.1f", personalCapacity.averageFocusCapacityHours)}h",
                            subtitle = "Deep focus bandwidth",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CapacityMetricBox(
                            title = "WORKDAY CAPACITY",
                            value = "${String.format(java.util.Locale.US, "%.1f", personalCapacity.workdayCapacityHours)}h",
                            subtitle = "Mon-Fri workload cap",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        CapacityMetricBox(
                            title = "WEEKEND CAPACITY",
                            value = "${String.format(java.util.Locale.US, "%.1f", personalCapacity.weekendCapacityHours)}h",
                            subtitle = "Rest & light tasks",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.06f))

                    // Energy & Chronotype Pattern
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Energy Rhythm & Dips",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RhythmChip(
                                label = "Peak Window",
                                time = productivityPatterns.mostProductiveHours,
                                color = Color(0xFF4EDEA3),
                                modifier = Modifier.weight(1f)
                            )
                            RhythmChip(
                                label = "Afternoon Dip",
                                time = productivityPatterns.leastProductiveHours,
                                color = Color(0xFFFFB74D),
                                modifier = Modifier.weight(1f)
                            )
                            RhythmChip(
                                label = "Best Day",
                                time = productivityPatterns.strongestDayOfWeek,
                                color = Secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Planning Accuracy & Explainable Factors
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "2. PLANNING ACCURACY & CALIBRATION",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Planning Accuracy Score",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "${planningAccuracy.overallScore}%",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (planningAccuracy.overallScore >= 75) Color(0xFF4EDEA3) else Color(0xFFFFB74D)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Secondary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = planningAccuracy.headline,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Secondary
                            )
                        }
                    }

                    Text(
                        text = planningAccuracy.detailedSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface
                    )

                    // Explainable Factors Breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Deterministic Calibration Factors",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )

                        planningAccuracy.factors.forEach { factor ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${factor.title} (${factor.weightPercent}%)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = "${factor.score}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (factor.score >= 80) Color(0xFF4EDEA3) else Secondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.05f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth((factor.score / 100f).coerceIn(0.01f, 1.0f))
                                            .clip(CircleShape)
                                            .background(if (factor.score >= 80) Color(0xFF4EDEA3) else Secondary)
                                    )
                                }
                                Text(
                                    text = factor.explanation,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Personalized Learned Insights Feed
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3. LEARNED RECOMMENDATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "${personalizedInsights.size} active",
                    style = MaterialTheme.typography.labelSmall,
                    color = Secondary
                )
            }

            if (personalizedInsights.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Learning",
                            tint = Secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Calibrating Behavioral Patterns...",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface
                        )
                        Text(
                            text = "Tap 'Re-Calibrate' or complete more daily tasks to generate custom recommendations tailored to your work rhythm.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                personalizedInsights.forEach { item ->
                    PersonalizedInsightCard(
                        item = item,
                        onWhyClick = { viewModel.showWhyExplanation(item) },
                        onFeedback = { feedback ->
                            viewModel.recordRecommendationFeedback(
                                recommendationType = item.recommendationType,
                                recommendationText = item.title,
                                feedback = feedback
                            )
                        }
                    )
                }
            }
        }

        // 4. Task Duration Predictor Playground
        DurationPredictorPlayground(viewModel = viewModel)
    }
}

@Composable
fun PersonalizedInsightCard(
    item: PersonalizedInsightItem,
    onWhyClick: () -> Unit,
    onFeedback: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainer)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Secondary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Secondary
                        )
                    }

                    ConfidenceBadge(confidence = item.confidence)
                }

                // Why button
                TextButton(
                    onClick = onWhyClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Why?",
                        tint = Tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Why?",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Tertiary
                    )
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )

            Text(
                text = item.insightText,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )

            if (item.evidencePoints.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        item.evidencePoints.forEach { point ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Evidence Point",
                                    tint = Secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }
            }

            // Feedback row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (item.feedbackState) {
                        "HELPFUL" -> "Marked as helpful 👍"
                        "NOT_HELPFUL" -> "Marked not helpful 👎"
                        "DONT_SUGGEST_AGAIN" -> "Blocked suggestion"
                        else -> "Is this insight accurate?"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.feedbackState != "NONE") Secondary else OnSurfaceVariant
                )

                if (item.feedbackState == "NONE") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onFeedback("HELPFUL") },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Helpful",
                                tint = Color(0xFF4EDEA3),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { onFeedback("NOT_HELPFUL") },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Dismiss",
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DurationPredictorPlayground(viewModel: LifeViewModel) {
    var selectedCategory by remember { mutableStateOf("WORK") }
    var selectedPriority by remember { mutableStateOf("IMPORTANT") }
    var selectedEnergy by remember { mutableStateOf("MEDIUM") }

    val prediction = remember(selectedCategory, selectedPriority, selectedEnergy) {
        viewModel.predictDuration(
            category = selectedCategory,
            priority = selectedPriority,
            energyLevel = selectedEnergy
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "4. DYNAMIC DURATION PREDICTOR",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceContainer)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Test how the learning engine estimates duration based on category history and energy level:",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )

                // Category selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("WORK", "HEALTH", "ADMIN", "REPLY").forEach { cat ->
                        FilterChip(
                            selected = (selectedCategory == cat),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Priority selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("CRITICAL", "IMPORTANT", "FLEXIBLE").forEach { prio ->
                        FilterChip(
                            selected = (selectedPriority == prio),
                            onClick = { selectedPriority = prio },
                            label = { Text(prio, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Output card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Primary.copy(alpha = 0.1f))
                        .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Predicted Duration",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                            Text(
                                text = "${prediction.predictedMinutes} minutes",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = prediction.explanation,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }

                        ConfidenceBadge(confidence = prediction.confidence)
                    }
                }
            }
        }
    }
}

@Composable
fun CapacityMetricBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = OnSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun RhythmChip(
    label: String,
    time: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = color
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
        }
    }
}

@Composable
fun ConfidenceBadge(confidence: ConfidenceLevel) {
    val color = when (confidence) {
        ConfidenceLevel.HIGH_CONFIDENCE -> Color(0xFF4EDEA3)
        ConfidenceLevel.MODERATE_CONFIDENCE -> Secondary
        ConfidenceLevel.LOW_CONFIDENCE -> Color(0xFFFFB74D)
        ConfidenceLevel.INSUFFICIENT_DATA -> Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = confidence.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun InsightWhyDialog(
    item: PersonalizedInsightItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHighest),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Insight Why",
                            tint = Secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Behind this Insight",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = OnSurfaceVariant
                        )
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "BEHAVIORAL EVIDENCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary
                        )
                        Text(
                            text = item.whyExplanation.ifBlank {
                                "Derived from correlation between your completion rates, estimation deviations, and rollover patterns over recorded days."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface
                        )
                        if (item.evidencePoints.isNotEmpty()) {
                            item.evidencePoints.forEach { pt ->
                                Text(
                                    text = "• $pt",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Got it", color = Primary)
                    }
                }
            }
        }
    }
}

@Composable
fun VelocityCard(label: String, value: String, icon: ImageVector, iconColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainer)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSurface
                )
            }
        }
    }
}
