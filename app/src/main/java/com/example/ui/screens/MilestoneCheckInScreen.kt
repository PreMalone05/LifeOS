package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneCheckInScreen(viewModel: LifeViewModel) {
    val selectedMilestoneId by viewModel.selectedMilestoneId.collectAsState()
    val selectedMilestone by viewModel.selectedMilestone.collectAsState()
    val subTasks by viewModel.selectedSubTasks.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val milestoneId = selectedMilestoneId ?: 9
    val milestoneTitle = selectedMilestone?.title ?: "Architectural System Overhaul"
    val milestoneDesc = selectedMilestone?.description ?: "Implement core distributed architecture event streams and optimize schema layouts."

    var showAddChecklistDialog by remember { mutableStateOf(false) }
    var showScheduleTasksDialog by remember { mutableStateOf(false) }
    var journalText by remember { mutableStateOf("Working through event interface specifications. V2 schema draft is complete, and performance is looking excellent.") }

    val coachPersonality = userProfile?.coachPersonality ?: "The Stoic Mentor"
    val userName = userProfile?.name?.substringBefore(" ") ?: "Alex"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Active Check-in",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo("PROFILE") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
            // Milestone Header
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PHASE ACTIVE CHECK-IN",
                    style = MaterialTheme.typography.labelMedium,
                    color = Secondary
                )
                Text(
                    text = milestoneTitle,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    color = OnSurface
                )
                Text(
                    text = milestoneDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // Checklist section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBTASK CHECKLIST",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "+ Add Step",
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary,
                        modifier = Modifier.clickable { showAddChecklistDialog = true }
                    )
                }

                if (subTasks.isEmpty()) {
                    // Inject basic mock checklists if empty for demonstration
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Setup your checklist steps by clicking '+ Add Step' above.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        subTasks.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceContainer)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.toggleSubTask(sub) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (sub.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Checkbox",
                                        tint = if (sub.isCompleted) Tertiary else OnSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = sub.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null,
                                            fontWeight = if (sub.isCompleted) FontWeight.Normal else FontWeight.Medium
                                        ),
                                        color = if (sub.isCompleted) OnSurfaceVariant.copy(alpha = 0.6f) else OnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Journal entry textarea
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "METRIC SCRATCHPAD & JOURNAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Generate AI Feedback",
                        style = MaterialTheme.typography.labelMedium,
                        color = Tertiary,
                        modifier = Modifier.clickable {
                            viewModel.generateMilestoneEvaluation(milestoneTitle, milestoneDesc, journalText)
                        }
                    )
                }

                OutlinedTextField(
                    value = journalText,
                    onValueChange = { journalText = it },
                    placeholder = { Text("Log your reflections, challenges, and blocker strategies...", color = OnSurfaceVariant.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = OutlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 6
                )
            }

            val milestoneEvaluation by viewModel.milestoneEvaluation.collectAsState()
            val isLoadingEvaluation by viewModel.isLoadingEvaluation.collectAsState()

            // AI Coach Quote Box
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ACTIVE COACH EVALUATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Spark",
                                tint = Tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = coachPersonality.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Tertiary
                            )
                        }

                        if (isLoadingEvaluation) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            val coachPhrase = milestoneEvaluation ?: when (coachPersonality) {
                                "The Stoic Mentor" -> "\"Let progress be incremental but absolute, $userName. What stands in the way becomes the way. Master this layout refactor now.\""
                                "The Strategic Architect" -> "\"Calculating focus efficiency densities, $userName. Completing this refactor eliminates 82% of future code integration latency. Finish strong.\""
                                "The Philosophical Guide" -> "\"Ponder the space of creation, $userName. Writing pristine code is an act of deep clarity. Take breath, then execute carefully.\""
                                else -> "\"Epic effort on the database layer, $userName! Crunch the remaining steps, log the win, and let's level up today!\""
                            }

                            Text(
                                text = coachPhrase,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 18.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = OnSurface
                            )
                        }
                    }
                }
            }

            // Action controls
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showScheduleTasksDialog = true },
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
                        text = "SCHEDULE DAILY TASKS WITH WORK HOURS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        viewModel.markMilestoneAsComplete(milestoneId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryContainer,
                        contentColor = OnSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "COMPLETE MILESTONE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

    if (showScheduleTasksDialog) {
        val allGoals by viewModel.allGoals.collectAsState()
        val allMilestones by viewModel.selectedMilestones.collectAsState()
        val selectedGoalId by viewModel.selectedGoalId.collectAsState()
        val goal = allGoals.find { it.id == selectedGoalId } ?: allGoals.firstOrNull()

        com.example.ui.components.SchedulePhaseTasksDialog(
            viewModel = viewModel,
            goal = goal,
            milestones = allMilestones,
            initialMilestoneId = milestoneId,
            onDismiss = { showScheduleTasksDialog = false },
            onTasksScheduled = { }
        )
    }

    if (showAddChecklistDialog) {
        var checklistTitle by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddChecklistDialog = false },
            containerColor = SolidSurface,
            title = {
                Text("Add Checklist Step", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            },
            text = {
                OutlinedTextField(
                    value = checklistTitle,
                    onValueChange = { checklistTitle = it },
                    label = { Text("Task description", color = OnSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = OutlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (checklistTitle.isNotBlank()) {
                            viewModel.addSubTaskToMilestone(milestoneId, checklistTitle)
                            showAddChecklistDialog = false
                        }
                    }
                ) {
                    Text("ADD STEP", color = Secondary, style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChecklistDialog = false }) {
                    Text("CANCEL", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}
