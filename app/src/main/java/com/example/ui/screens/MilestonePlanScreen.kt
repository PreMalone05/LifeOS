package com.example.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.GoalEntity
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestonePlanScreen(viewModel: LifeViewModel) {
    val allGoals by viewModel.allGoals.collectAsState()
    val selectedGoalId by viewModel.selectedGoalId.collectAsState()
    val milestones by viewModel.selectedMilestones.collectAsState()

    val goal = allGoals.find { it.id == selectedGoalId } ?: allGoals.firstOrNull()

    // Dialog state for AI Assisted Milestone Editing
    var showEditDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showScheduleTasksDialog by remember { mutableStateOf(false) }
    var editingMilestoneId by remember { mutableStateOf<Int?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var editedDesc by remember { mutableStateOf("") }
    var aiInstructionText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vision Timeline",
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
                actions = {
                    goal?.let { g ->
                        IconButton(onClick = {
                            viewModel.deleteGoal(g)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Goal",
                                tint = Error
                            )
                        }
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
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            goal?.let { g ->
                // Hero Vision Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceContainer)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = g.visionImage ?: "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600&auto=format&fit=crop&q=80"
                        ),
                        contentDescription = g.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )

                    IconButton(
                        onClick = { showImagePicker = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Change Vision Photo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SecondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${g.domain.uppercase()} • ${g.horizon.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = OnSecondaryContainer
                            )
                        }
                        Text(
                            text = g.title,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                            color = Color.White
                        )
                    }
                }

                // Completion progress card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VISION COMPLETED",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "${g.progressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(g.progressPercent / 100f)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(SecondaryContainer, Tertiary)
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "Estimated Horizon timeline: ${g.targetTimeline}",
                            style = TextStyle(fontSize = 12.sp, color = OnSurfaceVariant)
                        )
                    }
                }
            }

            // Milestone roadmap
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "ROADMAP MILESTONES",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                if (milestones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Configuring vision steps...", color = OnSurfaceVariant)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        milestones.forEachIndexed { idx, milestone ->
                            val isActive = milestone.status == "ACTIVE"
                            val isCompleted = milestone.status == "COMPLETED"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isActive) SurfaceContainerHigh else SurfaceContainer)
                                    .border(
                                        1.dp,
                                        if (isActive) Secondary else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        if (isActive || isCompleted) {
                                            viewModel.navigateTo("MILESTONE_CHECKIN", milestoneId = milestone.id)
                                        }
                                    }
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
                                            .background(
                                                if (isCompleted) OnTertiaryContainer
                                                else if (isActive) SecondaryContainer.copy(alpha = 0.1f)
                                                else SurfaceContainerHighest
                                            )
                                            .border(
                                                1.dp,
                                                if (isActive) Secondary else Color.Transparent,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (milestone.iconName) {
                                                "payments" -> Icons.Default.Payments
                                                "sports_motorsports" -> Icons.Default.SportsMotorsports
                                                "shield" -> Icons.Default.Shield
                                                "two_wheeler" -> Icons.Default.TwoWheeler
                                                "architecture" -> Icons.Default.Architecture
                                                "groups" -> Icons.Default.Groups
                                                "terminal" -> Icons.Default.Terminal
                                                else -> Icons.Default.WorkspacePremium
                                            },
                                            contentDescription = milestone.title,
                                            tint = if (isCompleted) OnTertiaryFixed
                                                   else if (isActive) Secondary
                                                   else OnSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "PHASE ${idx + 1} • ${milestone.title}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCompleted) OnSurfaceVariant.copy(alpha = 0.6f)
                                                    else if (isActive) OnSurface
                                                    else OnSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = milestone.description,
                                            style = TextStyle(fontSize = 13.sp, color = OnSurfaceVariant),
                                            maxLines = 2
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // AI ASSIST EDIT ACTION
                                    IconButton(
                                        onClick = {
                                            editingMilestoneId = milestone.id
                                            editedTitle = milestone.title
                                            editedDesc = milestone.description
                                            aiInstructionText = ""
                                            showEditDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Edit step with AI",
                                            tint = Secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isCompleted) OnTertiaryContainer
                                                else if (isActive) SecondaryContainer
                                                else SurfaceContainerHighest
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = milestone.status,
                                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                            color = if (isCompleted) OnTertiaryFixed
                                                   else if (isActive) OnSecondaryContainer
                                                   else OnSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DAILY PLANNER & WORK SCHEDULE CARD
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceContainerHigh
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Secondary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY EXECUTION BLUEPRINT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Secondary
                            )
                            Text(
                                text = "Put Phase Tasks into Daily Planner",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                    }

                    Text(
                        text = "Break down grand vision milestones into step-by-step daily tasks on your Planner — intelligently scheduled around your Full-Time or Part-Time work hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )

                    Button(
                        onClick = { showScheduleTasksDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnSurface,
                            contentColor = BaseDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCHEDULE DAILY TASKS (WITH JOB BLOCKS)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "No Goals",
                                tint = Secondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "No Long-Term Goals Defined Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Define a high-impact goal to generate an AI-tailored milestone roadmap and daily execution steps.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.navigateTo("DEFINE_GOAL") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Secondary,
                                contentColor = BaseDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Define a Goal with AI",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showScheduleTasksDialog) {
        com.example.ui.components.SchedulePhaseTasksDialog(
            viewModel = viewModel,
            goal = goal,
            milestones = milestones,
            onDismiss = { showScheduleTasksDialog = false },
            onTasksScheduled = { count ->
                // Handled in dialog
            }
        )
    }

    if (showImagePicker && goal != null) {
        com.example.ui.components.ImagePickerDialog(
            title = "Customize Vision Board Photo",
            currentImageUrl = goal.visionImage,
            onDismiss = { showImagePicker = false },
            onImageSelected = { newUrl ->
                viewModel.updateGoalVisionImage(goal.id, newUrl)
            }
        )
    }

    // custom Material 3 AlertDialog for Edit Step with AI Assist
    val activeEditingId = editingMilestoneId
    if (showEditDialog && activeEditingId != null) {
        val isRewritingMilestone by viewModel.isRewritingMilestone.collectAsState()

        AlertDialog(
            onDismissRequest = { if (!isRewritingMilestone) showEditDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Secondary
                    )
                    Text("Edit Step with AI Assist", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title Field
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Milestone Title", color = OnSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant,
                            focusedLabelColor = Secondary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRewritingMilestone
                    )

                    // Description Field
                    OutlinedTextField(
                        value = editedDesc,
                        onValueChange = { editedDesc = it },
                        label = { Text("Milestone Description", color = OnSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant,
                            focusedLabelColor = Secondary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRewritingMilestone,
                        maxLines = 4
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // AI Assist rewrite instructions
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "AI REWRITE ASSISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = aiInstructionText,
                            onValueChange = { aiInstructionText = it },
                            label = { Text("How should Gemini rewrite this?", color = OnSurfaceVariant) },
                            placeholder = { Text("e.g. Focus on savings instead, make it take 1 month, rewrite professionally", color = OnSurfaceVariant.copy(alpha = 0.4f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Secondary,
                                unfocusedBorderColor = OutlineVariant,
                                focusedLabelColor = Secondary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isRewritingMilestone,
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                if (aiInstructionText.isNotBlank()) {
                                    viewModel.editMilestoneWithAIAssist(
                                        milestoneId = activeEditingId,
                                        promptInstruction = aiInstructionText,
                                        onSuccess = { newTitle, newDesc ->
                                            editedTitle = newTitle
                                            editedDesc = newDesc
                                            aiInstructionText = ""
                                        }
                                    )
                                }
                            },
                            enabled = aiInstructionText.isNotBlank() && !isRewritingMilestone,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryContainer,
                                contentColor = OnSecondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isRewritingMilestone) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(color = OnSecondaryContainer, modifier = Modifier.size(16.dp))
                                    Text("Gemini is rewriting step...", style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Ask AI to Rewrite Step", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedTitle.isNotBlank() && editedDesc.isNotBlank()) {
                            viewModel.updateMilestoneDetails(
                                milestoneId = activeEditingId,
                                newTitle = editedTitle,
                                newDesc = editedDesc
                            )
                            showEditDialog = false
                        }
                    },
                    enabled = !isRewritingMilestone && editedTitle.isNotBlank() && editedDesc.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnSurface,
                        contentColor = BaseDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = !isRewritingMilestone
                ) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            },
            containerColor = SolidSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
