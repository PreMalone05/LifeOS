package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun WhatShouldIDoNowCard(
    result: WhatShouldIDoNowResult,
    onStartTask: (Int) -> Unit,
    onChooseAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("what_should_i_do_now_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.12f),
                            Surface
                        )
                    )
                )
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WHAT SHOULD I DO NOW?",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = Primary
                        )
                        Text(
                            text = "Next Optimal Action",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                // Window Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${result.focusWindowAvailableMinutes}m window",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task Title
            Text(
                text = result.recommendedTaskTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityBadge(priority = result.priority)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariant
                ) {
                    Text(
                        text = "⏱ ~${result.durationMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariant
                ) {
                    Text(
                        text = "⚡ ${result.energyLevel} Energy",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reason Text
            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (result.isActionable && result.recommendedTaskId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onStartTask(result.recommendedTaskId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("start_now_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = OnPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Now", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    if (result.secondaryOptionTaskId != null) {
                        OutlinedButton(
                            onClick = onChooseAnother,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("choose_another_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = OnSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Another", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = OnSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MorningBriefingCard(
    briefing: MorningBriefing,
    aiEnhancedText: String?,
    isLoadingAi: Boolean,
    onGenerateAiCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("morning_briefing_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "☀️ Morning Orientation",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (briefing.capacityStatus) {
                        "OPTIMAL" -> Success.copy(alpha = 0.15f)
                        "MODERATE LOAD" -> Warning.copy(alpha = 0.15f)
                        else -> Error.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = briefing.capacityStatus,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (briefing.capacityStatus) {
                            "OPTIMAL" -> Success
                            "MODERATE LOAD" -> Warning
                            else -> Error
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main priority & focus window
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Main Focus: ${briefing.mainPriorityTask}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Optimal Window: ${briefing.bestFocusWindow}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            if (!briefing.potentialIssue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = briefing.potentialIssue,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            if (aiEnhancedText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(text = "🤖 ", fontSize = 14.sp)
                        Text(
                            text = aiEnhancedText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = OnSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onGenerateAiCoach,
                    enabled = !isLoadingAi
                ) {
                    if (isLoadingAi) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Synthesizing...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI Coach Insight", style = MaterialTheme.typography.labelMedium, color = Primary)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanDivergenceAlertCard(
    report: PlanDivergenceReport,
    onRebalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("plan_divergence_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Warning.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Your Day Has Changed",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Warning.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${report.divergenceScore}% Divergence",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Warning,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            report.reasons.take(2).forEach { reason ->
                Text(
                    text = "• $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRebalance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("rebalance_divergence_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Warning)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rebalance My Day",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun PredictiveRecommendationCard(
    recommendation: PredictiveRecommendation,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFeedbackMenu by remember { mutableStateOf(false) }

    val borderColor = when (recommendation.priority) {
        "CRITICAL" -> Error.copy(alpha = 0.4f)
        "IMPORTANT" -> Primary.copy(alpha = 0.35f)
        else -> OutlineVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recommendation_card_${recommendation.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (recommendation.type) {
                        RecommendationType.CAPACITY_WARNING -> Icons.Default.Warning
                        RecommendationType.DEADLINE_WARNING -> Icons.Default.Timer
                        RecommendationType.HABIT_RISK -> Icons.Default.Spa
                        RecommendationType.PLAN_DIVERGENCE -> Icons.Default.Bolt
                        else -> Icons.Default.Lightbulb
                    }
                    val iconTint = when (recommendation.priority) {
                        "CRITICAL" -> Error
                        "IMPORTANT" -> Primary
                        else -> Secondary
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recommendation.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    IconButton(
                        onClick = { showFeedbackMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showFeedbackMenu,
                        onDismissRequest = { showFeedbackMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("👍 Helpful") },
                            onClick = {
                                showFeedbackMenu = false
                                onFeedback("HELPFUL")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("👎 Not Helpful") },
                            onClick = {
                                showFeedbackMenu = false
                                onFeedback("NOT_HELPFUL")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🚫 Don't Suggest Again") },
                            onClick = {
                                showFeedbackMenu = false
                                onFeedback("DONT_SUGGEST_AGAIN")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recommendation.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "💡 ${recommendation.suggestedAction}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineVariant)
                ) {
                    Text("Dismiss", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recommendation.priority == "CRITICAL") Error else Primary
                    )
                ) {
                    Text(
                        text = when (recommendation.actionType) {
                            "REBALANCE" -> "Rebalance"
                            "START_TASK" -> "Start Task"
                            "VIEW_HABIT" -> "View Habit"
                            else -> "Take Action"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
fun TomorrowPreviewDialog(
    report: TomorrowPreviewReport,
    onDismiss: () -> Unit,
    onRebalanceTomorrow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomorrow Preview", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Capacity vs Workload Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Planned Load", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("${report.plannedWorkloadHours}h", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Safe Capacity", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("${report.expectedCapacityHours}h", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Risk", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text(
                                report.overloadRisk.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (report.overloadRisk) {
                                    OverloadRiskLevel.LOW -> Success
                                    OverloadRiskLevel.MODERATE -> Warning
                                    OverloadRiskLevel.HIGH -> Error
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Recommended Focus Period: ${report.recommendedFocusPeriod}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface
                )

                if (report.potentialConflicts.isNotEmpty()) {
                    Text("Potential Conflicts:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    report.potentialConflicts.forEach { conflict ->
                        Text("• $conflict", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }

                if (report.tasksToPostpone.isNotEmpty()) {
                    Text("Suggested for Postponement if Busy:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    report.tasksToPostpone.take(2).forEach { taskName ->
                        Text("• $taskName", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            if (report.overloadRisk == OverloadRiskLevel.HIGH) {
                Button(
                    onClick = {
                        onDismiss()
                        onRebalanceTomorrow()
                    }
                ) {
                    Text("Rebalance Tomorrow")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (priority.uppercase()) {
        "CRITICAL" -> Triple(Error.copy(alpha = 0.2f), Error, "CRITICAL")
        "IMPORTANT" -> Triple(Primary.copy(alpha = 0.2f), Primary, "IMPORTANT")
        "FLEXIBLE" -> Triple(Tertiary.copy(alpha = 0.2f), Tertiary, "FLEXIBLE")
        else -> Triple(SurfaceVariant, OnSurfaceVariant, priority)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

