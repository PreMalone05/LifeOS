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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(viewModel: LifeViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()
    var selectedDate by remember { mutableStateOf("2024-10-24") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val dayTasks = allTasks.filter { it.date == selectedDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Planner",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Secondary
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = OnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCwIr8Vz-f0DrP2mYgHtdlHxxdVCAens_6Gs3A2Cm1RHyRVbmRxjl7ECV-mjxNtdNHbeVkuMHM5i3F6cQa6ehvXxl8TXkGkWyaz-wCeTZj8JH-0B6SyW2l-o6ZS20LtzHXWBkcVtwbB0QxtXgbs0dL4J4Hiuw3rZ4j50Cjh--J1rSWPC5dLMT6F643IMo1ocAF0F9eAl0IHA12HnVXwLHPDIrREpNMvrR4QomnsZU7vSYOTqahEFsoVWpXbCxkHavIrQoq0hhPk7I0t"
                        ),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
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
            // Week Picker Row
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OCTOBER 2024",
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
                    listOf(
                        "2024-10-21" to "MON" to "21",
                        "2024-10-22" to "TUE" to "22",
                        "2024-10-23" to "WED" to "23",
                        "2024-10-24" to "THU" to "24",
                        "2024-10-25" to "FRI" to "25",
                        "2024-10-26" to "SAT" to "26",
                        "2024-10-27" to "SUN" to "27"
                    ).forEach { pair ->
                        val dateKey = pair.first.first
                        val dayLabel = pair.first.second
                        val dayNum = pair.second
                        val isSelected = dateKey == selectedDate

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

                            // Notification dot on Wed 23 as shown in mockup
                            if (dayNum == "23" && !isSelected) {
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
            }

            // Roll-over Notification Banner
            val rolloverCount = allTasks.count { it.isRollover && !it.isCompleted }
            if (rolloverCount > 0 && selectedDate == "2024-10-24") {
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

            // Timeline Schedule Title
            Text(
                text = "DAILY SCHEDULE",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )

            // Timeline chronological vertical blocks
            if (dayTasks.isEmpty()) {
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
                            text = "No focus blocks planned for today.",
                            color = OnSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    dayTasks.forEachIndexed { index, task ->
                        val leftColor = when (task.category) {
                            "WORK" -> SecondaryContainer
                            "HEALTH" -> Tertiary
                            "REPLY" -> Error
                            "ADMIN" -> Primary
                            else -> Outline
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
                                    .width(64.dp)
                                    .padding(top = 4.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                val timeOnly = task.timeSlot.substringBefore(" -")
                                Text(
                                    text = timeOnly,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.End
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Timeline track separator and cards
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceContainer)
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.toggleTaskCompleted(task) }
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
                                        .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontSize = 18.sp,
                                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                                                ),
                                                color = if (task.isCompleted) OnSurfaceVariant.copy(alpha = 0.6f) else OnSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (task.isRollover) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Error.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "ROLL-OVER",
                                                        style = TextStyle(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Error
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        if (task.isRollover) {
                                            Icon(
                                                imageVector = Icons.Default.PriorityHigh,
                                                contentDescription = "Rollover Urgent",
                                                tint = Error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(leftColor.copy(alpha = 0.1f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${task.durationHours}H",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                                    color = leftColor
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                        color = OnSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = when (task.category) {
                                                    "WORK" -> Icons.Default.Terminal
                                                    "HEALTH" -> Icons.Default.FitnessCenter
                                                    "REPLY" -> Icons.Default.ChatBubbleOutline
                                                    "ADMIN" -> Icons.Default.ReceiptLong
                                                    else -> Icons.Default.PinDrop
                                                },
                                                contentDescription = "Icon",
                                                tint = OnSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = task.location ?: "Local Studio",
                                                style = TextStyle(fontSize = 12.sp, color = OnSurfaceVariant),
                                                maxLines = 1
                                            )
                                        }

                                        // Avatars for design sprint to mimic mockups
                                        if (task.title == "Design Sprint") {
                                            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                                listOf(
                                                    "https://lh3.googleusercontent.com/aida-public/AB6AXuA-fjwkmniONwiFxmV1-rF7eP3UhY4uxRbfrzW7AJogvmosvVtsVUt5EDsNnYtlc1F6IAWofRROLELh_ahE_xGQifN6StG6p5qyN8Y5DcJrnjFZmrGWowXb9FtNDXhEYs-BLLxI1bH-FrDYdJfXzEvrXKDvHiTXgC2DdjnJXYtQxL6a_rOXbws-9EGUgi5-S550QpXRIcwQq0jwKdvEI9XjnDrt5JNJC2K7vP87oK2d4QTS0UuJ1uN39e-d7JSgYrSqN2Jszyn1JBXx",
                                                    "https://lh3.googleusercontent.com/aida-public/AB6AXuBaT8W65fxWuyJSNYlsDBk1SHNJlryMxhmZfCQXhRBkdkaRIJ2uYjBZ8W94A1n6Ooi_PkWvL_IySvmHAfJ351wXZtUDxWH0ozPKNXYRMO4RL6QPnTTwLVsHpFBd8Xty7yxs9ci6gGb22X0mLQODir9T5xl2aV0weNmSCac7wBAXbpUCvt14lM4KQkntyVebAiO7c_72teiCn4UhMmWX-R0ZTCuObmhtYNKpnD0rAArr4C-cBYtKfNASVHXpI3k6ml0BIrCrFattyX6d"
                                                ).forEach { avatarUrl ->
                                                    Image(
                                                        painter = rememberAsyncImagePainter(model = avatarUrl),
                                                        contentDescription = "Sprint Member",
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .border(1.dp, SurfaceContainer, CircleShape),
                                                        contentScale = ContentScale.Crop
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
            }

            Spacer(modifier = Modifier.height(64.dp))
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
                Text("New Planner Block", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
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
                                date = selectedDate
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
}
