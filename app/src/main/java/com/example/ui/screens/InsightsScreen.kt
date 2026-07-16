package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: LifeViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()

    val completedTasks = allTasks.filter { it.isCompleted }
    val completedThisWeek = completedTasks.size + 42 // Dynamic + default offset for realistic values
    val productivityScore = 94

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCx_4l8_xNQwhE62m7BVi3gGAJlUCYXwKJYwAFsxsbgM6ldt1NQjd6g_xgMSe3dKGnyz-6Z4jwMIMcpraf68WFyz_VcFbvPskxHfMqGNfSUvLtpz1egl8cf577Za64HmKchyLSaVvhBjawn22-qU4Q4haSXAfp6aGxfHsfOiZexfyDRjlSCWLIV3jPB_DRpoP2meqR1iCIDPHnzwArpOBE8dphNNRxyFyYFlvuVaquOWnPTKb377RAxDjN15cJKqYiwtXVjBI5B3O4E"
                            ),
                            contentDescription = "User Portrait",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Task Analytics",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
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
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
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
                            sweepAngle = 0.94f * 360f,
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
                        text = "$completedThisWeek Tasks Completed this week",
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
                            text = "+12% vs last week",
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
                            text = "Flow State: 09:00 - 11:00",
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
                        // Mock hourly task densities
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

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(
                        Triple("Work", "28 tasks", Color(0xFF0566D9)),
                        Triple("Health", "14 tasks", Color(0xFF4EDEA3)),
                        Triple("Growth", "12 tasks", Color(0xFF9C27B0)),
                        Triple("Finance", "10 tasks", Color(0xFFFFD700))
                    ).forEach { triple ->
                        val title = triple.first
                        val taskCount = triple.second
                        val categoryColor = triple.third

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurface
                                )
                                Text(
                                    text = taskCount,
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
                                val weight = when (title) {
                                    "Work" -> 0.44f
                                    "Health" -> 0.22f
                                    "Growth" -> 0.19f
                                    else -> 0.15f
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(weight)
                                        .clip(CircleShape)
                                        .background(categoryColor)
                                )
                            }
                        }
                    }
                }
            }

            // Rollover insights
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "EFFICIENCY",
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
                    // Side color bar
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
                                    text = "8% Rollover Rate",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Secondary
                                )
                                Text(
                                    text = "3 tasks rolled over from yesterday",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Spark",
                                tint = Secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Secondary.copy(alpha = 0.05f))
                                .border(1.dp, Secondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "AI Insight: You're most likely to roll over 'Growth' tasks on Tuesdays. Try scheduling these for Monday mornings instead.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
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
                            value = "42m",
                            icon = Icons.Default.Speed,
                            iconColor = Color(0xFF0566D9)
                        )
                        VelocityCard(
                            label = "GROWTH AVG.",
                            value = "55m",
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
                            value = "18m",
                            icon = Icons.Default.Timer,
                            iconColor = Color(0xFF4EDEA3)
                        )
                        VelocityCard(
                            label = "FINANCE AVG.",
                            value = "12m",
                            icon = Icons.Default.Payments,
                            iconColor = Color(0xFFFFD700)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
fun VelocityCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
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
