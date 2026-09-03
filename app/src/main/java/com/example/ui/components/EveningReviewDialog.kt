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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.EveningReviewSummary
import com.example.data.TaskEntity
import com.example.ui.theme.*

@Composable
fun EveningReviewDialog(
    eveningSummary: EveningReviewSummary?,
    pendingTasks: List<TaskEntity>,
    isLoading: Boolean,
    onCompleteReview: (scoreRating: String, notes: String, rolledTaskIds: Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedScore by remember { mutableStateOf(eveningSummary?.suggestedScore ?: "BALANCED") }
    var reflectionNotes by remember { mutableStateOf("") }
    val rolledTaskIds = remember { mutableStateOf(pendingTasks.map { it.id }.toMutableSet()) }

    val scoreRatings = listOf("DOMINANT", "BALANCED", "RECOVERY", "TOUGH")

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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nightlight,
                                contentDescription = "Evening Review",
                                tint = OnTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "EVENING WRAP-UP",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                            Text(
                                text = "Daily Reflection & Task Triage",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = OnSurfaceVariant)
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = Secondary, modifier = Modifier.size(28.dp))
                            Text(
                                text = "Reflecting on your daily execution...",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Coach Praise & Summary
                    eveningSummary?.coachPraise?.takeIf { it.isNotBlank() }?.let { praise ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = praise,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                        }
                    }

                    // Score Rating Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "HOW DID TODAY FEEL?",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            scoreRatings.forEach { rating ->
                                val isSelected = selectedScore == rating
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) SecondaryContainer else SurfaceContainerHigh)
                                        .border(
                                            1.dp,
                                            if (isSelected) Secondary else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedScore = rating }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rating,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) OnSecondaryContainer else OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Leftover Task Triage
                    if (pendingTasks.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "TRIAGE LEFTOVER TASKS (${pendingTasks.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurfaceVariant
                            )
                            pendingTasks.forEach { task ->
                                val isRolled = rolledTaskIds.value.contains(task.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceContainerHigh)
                                        .clickable {
                                            if (isRolled) {
                                                rolledTaskIds.value = (rolledTaskIds.value - task.id).toMutableSet()
                                            } else {
                                                rolledTaskIds.value = (rolledTaskIds.value + task.id).toMutableSet()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = isRolled,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                rolledTaskIds.value = (rolledTaskIds.value + task.id).toMutableSet()
                                            } else {
                                                rolledTaskIds.value = (rolledTaskIds.value - task.id).toMutableSet()
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Secondary)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = OnSurface
                                        )
                                        Text(
                                            text = if (isRolled) "Will roll over to tomorrow" else "Will remain pending in today's log",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isRolled) Secondary else OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Optional Notes Input
                    OutlinedTextField(
                        value = reflectionNotes,
                        onValueChange = { reflectionNotes = it },
                        label = { Text("Daily notes & wins (optional)") },
                        placeholder = { Text("What worked well today? What can be improved?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant
                        )
                    )

                    // Complete Button
                    Button(
                        onClick = {
                            onCompleteReview(selectedScore, reflectionNotes, rolledTaskIds.value)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SecondaryContainer,
                            contentColor = OnSecondaryContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CLOSE DAY (+100 XP)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
