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
import androidx.compose.ui.window.Dialog
import com.example.data.AdaptiveRebalanceResult
import com.example.ui.theme.*

@Composable
fun AdaptiveRebalanceDialog(
    rebalanceResult: AdaptiveRebalanceResult?,
    isRebalancing: Boolean,
    onCalculateRebalance: (String) -> Unit,
    onApplyRebalance: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf("Running 45m Behind") }
    var customReason by remember { mutableStateOf("") }
    var isCustomMode by remember { mutableStateOf(false) }

    val presets = listOf(
        "Running 30m Behind",
        "Running 60m Behind",
        "Unexpected 1-hr Meeting",
        "Low Energy / Pacing Needed",
        "Need to finish day early (by 4 PM)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = SurfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Row
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Rebalance",
                                tint = OnSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MY DAY CHANGED",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "Interactive Schedule Rebalance",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = OnSurfaceVariant)
                    }
                }

                if (rebalanceResult == null) {
                    // Reason Selection Phase
                    Text(
                        text = "What happened? LifeOS will dynamically protect your Critical tasks and shift the rest realistically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.forEach { preset ->
                            val isSelected = (!isCustomMode && selectedPreset == preset)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SecondaryContainer.copy(alpha = 0.5f) else SurfaceContainerHigh)
                                    .border(
                                        1.dp,
                                        if (isSelected) Secondary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        isCustomMode = false
                                        selectedPreset = preset
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        isCustomMode = false
                                        selectedPreset = preset
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Secondary)
                                )
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) OnSurface else OnSurfaceVariant
                                )
                            }
                        }

                        // Custom option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCustomMode) SecondaryContainer.copy(alpha = 0.5f) else SurfaceContainerHigh)
                                .border(
                                    1.dp,
                                    if (isCustomMode) Secondary else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { isCustomMode = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isCustomMode,
                                onClick = { isCustomMode = true },
                                colors = RadioButtonDefaults.colors(selectedColor = Secondary)
                            )
                            Text(
                                text = "Custom reason...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCustomMode) OnSurface else OnSurfaceVariant
                            )
                        }

                        if (isCustomMode) {
                            OutlinedTextField(
                                value = customReason,
                                onValueChange = { customReason = it },
                                placeholder = { Text("e.g. Spent 2 hours helping a client...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val reason = if (isCustomMode) customReason.ifBlank { "Schedule conflict" } else selectedPreset
                            onCalculateRebalance(reason)
                        },
                        enabled = !isRebalancing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryContainer,
                            contentColor = OnSecondaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRebalancing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = OnSecondaryContainer,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REBALANCING DAY...")
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CALCULATE ADAPTIVE PLAN", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                } else {
                    // Rebalance Plan Review Phase
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Summary Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = rebalanceResult.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        }

                        // Kept Tasks Section
                        if (rebalanceResult.keptTasks.isNotEmpty()) {
                            Text(
                                text = "STAYS TODAY (${rebalanceResult.keptTasks.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Secondary
                            )
                            rebalanceResult.keptTasks.forEach { item ->
                                RebalanceItemRow(
                                    title = item.title,
                                    timeSlot = item.newTimeSlot,
                                    priority = item.priority,
                                    reason = item.reason,
                                    isDeferred = false
                                )
                            }
                        }

                        // Deferred Tasks Section
                        if (rebalanceResult.deferredTasks.isNotEmpty()) {
                            Text(
                                text = "POSTPONED TO TOMORROW (${rebalanceResult.deferredTasks.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Warning
                            )
                            rebalanceResult.deferredTasks.forEach { item ->
                                RebalanceItemRow(
                                    title = item.title,
                                    timeSlot = "Tomorrow",
                                    priority = item.priority,
                                    reason = item.reason,
                                    isDeferred = true
                                )
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onCalculateRebalance(selectedPreset) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("RETRY")
                            }

                            Button(
                                onClick = onApplyRebalance,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecondaryContainer,
                                    contentColor = OnSecondaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("APPLY CHANGES", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RebalanceItemRow(
    title: String,
    timeSlot: String,
    priority: String,
    reason: String,
    isDeferred: Boolean
) {
    val priorityColor = when (priority) {
        "CRITICAL" -> Error
        "IMPORTANT" -> Secondary
        "FLEXIBLE" -> Tertiary
        else -> OnSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerHighest.copy(alpha = 0.6f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isDeferred) Icons.Default.ScheduleSend else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isDeferred) Warning else Secondary,
            modifier = Modifier.size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(priorityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = priority,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = priorityColor
                    )
                }
            }
            if (reason.isNotBlank()) {
                Text(
                    text = reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
        Text(
            text = timeSlot,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = if (isDeferred) Warning else Secondary
        )
    }
}
