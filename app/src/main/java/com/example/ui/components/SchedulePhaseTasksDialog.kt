package com.example.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GoalEntity
import com.example.data.MilestoneEntity
import com.example.ui.theme.*
import com.example.viewmodel.JobScheduleType
import com.example.viewmodel.LifeViewModel
import kotlinx.coroutines.launch

@Composable
fun SchedulePhaseTasksDialog(
    viewModel: LifeViewModel,
    goal: GoalEntity?,
    milestones: List<MilestoneEntity>,
    initialMilestoneId: Int? = null,
    onDismiss: () -> Unit,
    onTasksScheduled: (Int) -> Unit
) {
    val isGenerating by viewModel.isGeneratingPhaseDailyTasks.collectAsState()

    var selectedMilestoneId by remember { 
        mutableStateOf(initialMilestoneId ?: milestones.firstOrNull { it.status == "ACTIVE" }?.id ?: milestones.firstOrNull()?.id) 
    }
    var selectedJobType by remember { mutableStateOf(JobScheduleType.FULL_TIME) }
    var customJobTimeSlot by remember { mutableStateOf("") }
    var includeJobBlocks by remember { mutableStateOf(true) }
    var isSuccess by remember { mutableStateOf(false) }
    var generatedCount by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isGenerating) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SolidSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Planner",
                                tint = Secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Daily Vision Planner",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "Step-by-Step Tasks + Work Schedule",
                                style = MaterialTheme.typography.labelSmall,
                                color = Secondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (!isGenerating) onDismiss() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (isGenerating) {
                    // Loading State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Secondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Architecting Daily Tasks in Planner...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Gemini is factoring your ${selectedJobType.title.substringBefore(" (")} schedule to generate step-by-step milestone tasks with zero work overlaps.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (isSuccess) {
                    // Success View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Tertiary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Tertiary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Planner Synchronized!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Created $generatedCount daily step-by-step tasks and employment blocks across October 21–27.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDismiss() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("DONE", color = OnSurface)
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    viewModel.navigateTo("PLANNER")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecondaryContainer,
                                    contentColor = OnSecondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OPEN PLANNER", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // 1. Target Phase Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1. SELECT GRAND VISION PHASE",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )

                        if (milestones.isEmpty()) {
                            Text(
                                text = "Goal: ${goal?.title ?: "Grand Vision"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                milestones.forEachIndexed { index, m ->
                                    val isSelected = selectedMilestoneId == m.id
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Secondary.copy(alpha = 0.12f) else SurfaceContainer)
                                            .border(
                                                1.dp,
                                                if (isSelected) Secondary else Color.White.copy(alpha = 0.05f),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedMilestoneId = m.id }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isSelected) Secondary else OnSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Phase ${index + 1}: ${m.title}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) OnSurface else OnSurfaceVariant
                                                )
                                                Text(
                                                    text = m.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = OnSurfaceVariant.copy(alpha = 0.7f),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Work / Job Schedule Commitment
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. JOB / WORK COMMITMENT",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Auto-Deconflicts Schedule",
                                    style = TextStyle(fontSize = 9.sp, color = Primary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            JobScheduleType.values().forEach { jobType ->
                                val isSelected = selectedJobType == jobType
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Secondary.copy(alpha = 0.12f) else SurfaceContainer)
                                        .border(
                                            1.dp,
                                            if (isSelected) Secondary else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedJobType = jobType }
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                            contentDescription = null,
                                            tint = if (isSelected) Secondary else OnSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = jobType.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) OnSurface else OnSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = jobType.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = OnSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Optional Custom Hours / Toggle for Job Blocks
                    if (selectedJobType != JobScheduleType.NO_JOB_FULL_FOCUS) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainer)
                                    .clickable { includeJobBlocks = !includeJobBlocks }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Work,
                                        contentDescription = "Work Block",
                                        tint = Secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Add Job Work Blocks to Planner (Mon–Fri)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurface
                                    )
                                }
                                Switch(
                                    checked = includeJobBlocks,
                                    onCheckedChange = { includeJobBlocks = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Secondary,
                                        checkedTrackColor = Secondary.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.size(width = 38.dp, height = 24.dp)
                                )
                            }

                            OutlinedTextField(
                                value = customJobTimeSlot,
                                onValueChange = { customJobTimeSlot = it },
                                label = { Text("Custom Work Hours (Optional)", color = OnSurfaceVariant, fontSize = 12.sp) },
                                placeholder = { Text(selectedJobType.defaultTimeSlot, color = OnSurfaceVariant.copy(alpha = 0.4f), fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface,
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    // Informational Strategy Note
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerHigh)
                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Smart Schedule",
                                tint = Tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Smart Conflict-Free Scheduling",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Tertiary
                                )
                                Text(
                                    text = "Grand Vision tasks are planned in morning focus windows (7:00–8:30 AM), lunch sprints (12:30–1:15 PM), or evening blocks (6:30–8:00 PM) around your job, plus deep work blocks on weekends.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val targetGoalId = goal?.id ?: 1
                                viewModel.generatePhaseDailyTasksForPlanner(
                                    goalId = targetGoalId,
                                    milestoneId = selectedMilestoneId,
                                    jobScheduleType = selectedJobType,
                                    customJobTimeSlot = customJobTimeSlot.ifBlank { null },
                                    includeJobBlocks = includeJobBlocks,
                                    startDate = "2024-10-21",
                                    numDays = 7,
                                    onComplete = { count ->
                                        generatedCount = count
                                        isSuccess = true
                                        onTasksScheduled(count)
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnSurface,
                                contentColor = BaseDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GENERATE & PUT IN PLANNER",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
