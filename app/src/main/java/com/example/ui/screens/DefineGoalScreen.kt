package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefineGoalScreen(viewModel: LifeViewModel) {
    var goalTitle by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf("Career") }
    var selectedHorizon by remember { mutableStateOf("Quarterly") }
    var targetTimeline by remember { mutableStateOf("3 Months") }
    var customImageUrl by remember { mutableStateOf("") }

    // Interview States
    var useAiInterview by remember { mutableStateOf(true) }
    var isInterviewMode by remember { mutableStateOf(false) }
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isCustomSelected by remember { mutableStateOf(false) }
    var customAnswerText by remember { mutableStateOf("") }
    val answersList = remember { mutableStateListOf<Pair<String, String>>() }

    val interviewQuestions by viewModel.interviewQuestions.collectAsState()
    val isFetchingQuestions by viewModel.isFetchingQuestions.collectAsState()
    val isGeneratingRoadmap by viewModel.isGeneratingTailoredRoadmap.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isInterviewMode) "AI Roadmap Interview" else "Grand Vision",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isInterviewMode) {
                            if (currentQuestionIdx > 0) {
                                currentQuestionIdx--
                                if (answersList.isNotEmpty()) {
                                    answersList.removeAt(answersList.size - 1)
                                }
                                selectedOption = null
                                isCustomSelected = false
                                customAnswerText = ""
                            } else {
                                isInterviewMode = false
                                currentQuestionIdx = 0
                                answersList.clear()
                                selectedOption = null
                                isCustomSelected = false
                                customAnswerText = ""
                                viewModel.clearInterviewState()
                            }
                        } else {
                            viewModel.navigateTo("TODAY")
                        }
                    }) {
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            if (isGeneratingRoadmap) {
                // LOADING ROADMAP GENERATION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Secondary, modifier = Modifier.size(48.dp))
                        Text(
                            text = "Synthesizing Tailored Roadmap...",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gemini is factoring your multiple-choice answers, weekly capacity, and baseline constraints to construct a personalized 3-phase execution roadmap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else if (isInterviewMode) {
                if (isFetchingQuestions) {
                    // LOADING QUESTIONS WITH PULSING AI EFFECT
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Secondary, modifier = Modifier.size(48.dp))
                            Text(
                                text = "Generating Tailored Assessment...",
                                style = MaterialTheme.typography.titleMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gemini is dynamically architecting multiple-choice questions for \"$goalTitle\" ($selectedDomain)...",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else if (interviewQuestions.isNotEmpty()) {
                    // ACTIVE MULTIPLE CHOICE INTERVIEW FLOW
                    val currentQuestion = interviewQuestions.getOrNull(currentQuestionIdx)

                    if (currentQuestion != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            // Header badge and step indicator
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
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Secondary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "QUESTION ${currentQuestionIdx + 1} OF ${interviewQuestions.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Secondary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceContainerHighest)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = currentQuestion.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "Target: $targetTimeline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }

                            // Step Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (i in interviewQuestions.indices) {
                                    val isPastOrCurrent = i <= currentQuestionIdx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isPastOrCurrent) Secondary else SurfaceContainerHighest)
                                    )
                                }
                            }

                            // Question Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceContainer)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI Interviewer",
                                            tint = Secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "AI Roadmap Tailoring",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Secondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = currentQuestion.question,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface,
                                        lineHeight = 24.sp
                                    )
                                }
                            }

                            Text(
                                text = "SELECT YOUR ANSWER",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            // Multiple Choice Options List
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                currentQuestion.options.forEach { option ->
                                    val isSelected = !isCustomSelected && selectedOption == option
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) Secondary.copy(alpha = 0.14f) else SurfaceContainer)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) Secondary else Color.White.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                selectedOption = option
                                                isCustomSelected = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                                contentDescription = if (isSelected) "Selected" else "Unselected",
                                                tint = if (isSelected) Secondary else OnSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) OnSurface else OnSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Custom / Specific Text Option
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isCustomSelected) Secondary.copy(alpha = 0.14f) else SurfaceContainer)
                                        .border(
                                            width = if (isCustomSelected) 1.5.dp else 1.dp,
                                            color = if (isCustomSelected) Secondary else Color.White.copy(alpha = 0.05f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            isCustomSelected = true
                                            selectedOption = null
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCustomSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                                contentDescription = if (isCustomSelected) "Selected" else "Unselected",
                                                tint = if (isCustomSelected) Secondary else OnSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Text(
                                                text = "Other / Specify Custom Details...",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isCustomSelected) OnSurface else OnSurfaceVariant,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        if (isCustomSelected) {
                                            OutlinedTextField(
                                                value = customAnswerText,
                                                onValueChange = { customAnswerText = it },
                                                placeholder = { Text("Type your specific answer or constraint...", color = OnSurfaceVariant.copy(alpha = 0.4f), style = MaterialTheme.typography.bodySmall) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = OnSurface,
                                                    unfocusedTextColor = OnSurface,
                                                    focusedBorderColor = Secondary,
                                                    unfocusedBorderColor = OutlineVariant
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(90.dp),
                                                maxLines = 3
                                            )
                                        }
                                    }
                                }
                            }

                            // Summary of prior answered choices
                            if (answersList.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "ANSWERED PREFERENCES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                    answersList.forEachIndexed { idx, pair ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Done",
                                                tint = Tertiary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = pair.second,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Action Buttons
                            val isCurrentValid = (selectedOption != null) || (isCustomSelected && customAnswerText.isNotBlank())
                            val isLastQuestion = currentQuestionIdx >= interviewQuestions.size - 1

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                                Button(
                                    onClick = {
                                        if (isCurrentValid) {
                                            val finalAnswer = if (isCustomSelected) customAnswerText.trim() else (selectedOption ?: "")
                                            answersList.add(Pair(currentQuestion.question, finalAnswer))

                                            if (!isLastQuestion) {
                                                currentQuestionIdx++
                                                selectedOption = null
                                                isCustomSelected = false
                                                customAnswerText = ""
                                            } else {
                                                // Finish interview & build tailored roadmap!
                                                viewModel.createGoalWithTailoredRoadmap(
                                                    title = goalTitle,
                                                    domain = selectedDomain,
                                                    horizon = selectedHorizon,
                                                    targetTimeline = targetTimeline.ifBlank { "Est. 6 Months" },
                                                    imageUrl = if (customImageUrl.isNotBlank()) customImageUrl else null,
                                                    questionsAndAnswers = answersList.toList()
                                                )
                                            }
                                        }
                                    },
                                    enabled = isCurrentValid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OnSurface,
                                        contentColor = BaseDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 14.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLastQuestion) Icons.Default.AutoAwesome else Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (!isLastQuestion) "CONTINUE TO NEXT QUESTION" else "GENERATE TAILORED ROADMAP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (currentQuestionIdx > 0) {
                                        TextButton(
                                            onClick = {
                                                currentQuestionIdx--
                                                if (answersList.isNotEmpty()) {
                                                    answersList.removeAt(answersList.size - 1)
                                                }
                                                selectedOption = null
                                                isCustomSelected = false
                                                customAnswerText = ""
                                            }
                                        ) {
                                            Text(
                                                text = "Previous Question",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    TextButton(
                                        onClick = {
                                            // Fallback / Skip to normal generation
                                            viewModel.createGoalFromVision(
                                                title = goalTitle,
                                                domain = selectedDomain,
                                                horizon = selectedHorizon,
                                                targetTimeline = targetTimeline.ifBlank { "Est. 6 Months" },
                                                imageUrl = if (customImageUrl.isNotBlank()) customImageUrl else null
                                            )
                                        }
                                    ) {
                                        Text(
                                            text = "Skip interview",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // STANDARD GOAL DEFINITION FORM
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "VISION ARCHITECTURE",
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary
                    )
                    Text(
                        text = "Map Your Horizon",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        color = OnSurface
                    )
                    Text(
                        text = "Commit to a specific grand vision. LifeOS will architect the milestones, intervals, and weekly check-in checklists.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }

                // Title input
                OutlinedTextField(
                    value = goalTitle,
                    onValueChange = { goalTitle = it },
                    label = { Text("What is your grand commitment?", color = OnSurfaceVariant) },
                    placeholder = { Text("e.g., Buy a Motorcycle, Learn System Design", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = OutlineVariant,
                        focusedLabelColor = Secondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Domain Row Selector
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SELECT DOMAIN",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Career", "Health", "Wealth", "Growth").forEach { domain ->
                            val isSelected = selectedDomain == domain
                            val domainColor = when (domain) {
                                "Career" -> Secondary
                                "Health" -> Tertiary
                                "Wealth" -> Error
                                else -> Primary
                            }

                            Box(
                                modifier = Modifier
                                    .width(112.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) domainColor.copy(alpha = 0.15f) else SurfaceContainer)
                                    .border(
                                        1.dp,
                                        if (isSelected) domainColor else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedDomain = domain }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (domain) {
                                            "Career" -> Icons.Default.Architecture
                                            "Health" -> Icons.Default.FitnessCenter
                                            "Wealth" -> Icons.Default.Payments
                                            else -> Icons.Default.MenuBook
                                        },
                                        contentDescription = domain,
                                        tint = if (isSelected) domainColor else OnSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = domain,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) OnSurface else OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Horizon Selector
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "HORIZON RANGE",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Monthly", "Quarterly", "Yearly").forEach { horizon ->
                            val isSelected = selectedHorizon == horizon
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SecondaryContainer else SurfaceContainer)
                                    .border(
                                        1.dp,
                                        if (isSelected) Secondary else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedHorizon = horizon }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = horizon,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) OnSecondaryContainer else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Target Timeline Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Timeline",
                            tint = Secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "TARGET TIMELINE",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = targetTimeline,
                        onValueChange = { targetTimeline = it },
                        label = { Text("When do you want to achieve this?", color = OnSurfaceVariant) },
                        placeholder = { Text("e.g., By December 2026, In 6 Months, 3 Weeks", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant,
                            focusedLabelColor = Secondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1 Month", "3 Months", "6 Months", "1 Year", "By Dec 2026").forEach { timelineSuggestion ->
                            val isSelected = targetTimeline == timelineSuggestion
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SecondaryContainer else SurfaceContainerLow)
                                    .clickable { targetTimeline = timelineSuggestion }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = timelineSuggestion,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) OnSecondaryContainer else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Custom Image URL Input
                OutlinedTextField(
                    value = customImageUrl,
                    onValueChange = { customImageUrl = it },
                    label = { Text("Vision Board Image URL (Optional)", color = OnSurfaceVariant) },
                    placeholder = { Text("e.g., https://unsplash.com/photos/...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = OutlineVariant,
                        focusedLabelColor = Secondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Interactive AI Tailoring Toggle Option (Material 3 Card)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .clickable { useAiInterview = !useAiInterview }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Secondary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "AI Customization",
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Tailor with AI Interview",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Clarifies your skills and constraints for a perfectly customized roadmap.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = useAiInterview,
                            onCheckedChange = { useAiInterview = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BaseDark,
                                checkedTrackColor = Secondary,
                                uncheckedThumbColor = OnSurfaceVariant,
                                uncheckedTrackColor = SurfaceContainerHigh
                            )
                        )
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        if (goalTitle.isNotBlank()) {
                            if (useAiInterview) {
                                isInterviewMode = true
                                viewModel.generateQuestionsForGoal(
                                    title = goalTitle,
                                    domain = selectedDomain,
                                    targetTimeline = targetTimeline.ifBlank { "Est. 6 Months" }
                                )
                            } else {
                                viewModel.createGoalFromVision(
                                    title = goalTitle,
                                    domain = selectedDomain,
                                    horizon = selectedHorizon,
                                    targetTimeline = targetTimeline.ifBlank { "Est. 6 Months" },
                                    imageUrl = if (customImageUrl.isNotBlank()) customImageUrl else null
                                )
                            }
                        }
                    },
                    enabled = goalTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnSurface,
                        contentColor = BaseDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = if (useAiInterview) "START AI ROADMAP ASSESSMENT" else "ARCHITECT VISION TIMELINE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}
