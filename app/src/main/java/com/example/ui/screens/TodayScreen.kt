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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(viewModel: LifeViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    val todayTasks = allTasks.filter { it.date == "2024-10-24" }
    val completedCount = todayTasks.count { it.isCompleted }
    val totalCount = todayTasks.size
    val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

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
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBlXDk7scnpDRpS_7xYER9DJWZkdrc5biHbrwxGUXBFAutECywB8Duoh0cY-SfCDc_SGPDRKNOpzwAvmTMBtWl2zoSXjSOCOKqbDlz_ebU_3tf5x4pxm2iF2qCbzkdEnGygqD7iU0x7VQbXip2WE-o1fKuWaNnr2c5oR9KxFQX6lQ6LAF8kPWROP4IwPQrOXtoTAAjTvAIkpeoV9BEolQUkP9XYgy_idRKVXfq--lz1o9p-bcsWNkSRnCHKvaq0pDpm0LjkYoEOWinG"
                            ),
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = "Today, Oct 24",
                                style = MaterialTheme.typography.headlineSmall,
                                color = OnSurface
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo("PROFILE") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
                    Column {
                        Text(
                            text = "WELCOME BACK",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "Good Morning, ${profile.name.substringBefore(" ")}",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                            color = OnSurface
                        )
                    }
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

            // AI Hero Suggestion (LifeOS Insight)
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

                    Text(
                        text = "You usually complete your work before noon. Finish your hardest task now to maintain your 14-day streak.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurface
                    )

                    Button(
                        onClick = {
                            // Find active check-in milestone or navigate to planning
                            viewModel.navigateTo("MILESTONE_CHECKIN", milestoneId = 9)
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
                                            Text(
                                                text = task.timeSlot,
                                                style = TextStyle(fontSize = 12.sp, color = OnSurfaceVariant),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                                    contentDescription = "Task State",
                                    tint = if (task.isCompleted) Tertiary else OnSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
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
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBjY1S_3QA_VJRKTB6ivRGmRaXZqO6zOWVxrmLcHBH8aoqx9X_6UMfj-s-9a8onLiYjsBn-hvI3iRpwnW0NCowty-GFhtFB_Xc2lfF9bjvniR_2jnMcy28ACGtFYDsmUKbkbpOcu0DVs3T79T0TwYFWDTS-JULwXyp5LSrV5cmDsHAnTI2Wo1DYtMaNcOOWQz5D9ZtonAXHU_rynRJFpaFlc-ugTJ-b6uYY8m0UOoUAQt4p1l4s6r5g7QWi1kxObR2rjedLndXnu8ki"
                    ),
                    contentDescription = "Atmospheric Studio Dawn",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f
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

    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskCategory by remember { mutableStateOf("WORK") }
        var taskTimeSlot by remember { mutableStateOf("09:00 - 10:30 AM") }
        var taskDescription by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = SurfaceContainer,
            title = {
                Text("New Focus Task", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                date = "2024-10-24"
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
}
