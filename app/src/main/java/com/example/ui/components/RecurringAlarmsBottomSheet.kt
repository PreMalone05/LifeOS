package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecurringAlarmEntity
import com.example.ui.theme.*
import com.example.viewmodel.LifeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringAlarmsBottomSheet(
    viewModel: LifeViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val recurringAlarms by viewModel.allRecurringAlarms.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var testNoticeMsg by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SolidSurface,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Alarms",
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Recurring Alarms & Nudges",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Automated exact alarms with audio & notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(SurfaceContainerHigh, CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            testNoticeMsg?.let { msg ->
                Surface(
                    color = Secondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }

            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Alarm",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Alarm",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.triggerTestAlarmNow(
                            context = context,
                            title = "Test Alarm Triggered! ⏰",
                            message = "Your recurring alarm sound & notification system is working perfectly!",
                            delaySeconds = 5
                        )
                        testNoticeMsg = "Test alarm set for 5 seconds from now! Lock or minimize app to test notification."
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Secondary.copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Test Alarm",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test (5s)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = SurfaceContainerHigh)

            if (recurringAlarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlarmOff,
                            contentDescription = "No Alarms",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No recurring alarms created yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 380.dp)
                ) {
                    items(recurringAlarms, key = { it.id }) { alarm ->
                        RecurringAlarmCard(
                            alarm = alarm,
                            onToggle = { viewModel.toggleRecurringAlarm(context, alarm) },
                            onDelete = { viewModel.deleteRecurringAlarm(context, alarm) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringAlarmDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, message, hour, minute, repeatType, soundEnabled ->
                viewModel.addRecurringAlarm(
                    context = context,
                    title = title,
                    message = message,
                    hour = hour,
                    minute = minute,
                    repeatType = repeatType,
                    soundEnabled = soundEnabled
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecurringAlarmCard(
    alarm: RecurringAlarmEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = remember(alarm.hour, alarm.minute) {
        val amPm = if (alarm.hour >= 12) "PM" else "AM"
        val hour12 = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
        String.format(Locale.US, "%02d:%02d %s", hour12, alarm.minute, amPm)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) SurfaceContainerHighest else SurfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (alarm.isEnabled) Primary else OnSurfaceVariant
                    )

                    Surface(
                        color = if (alarm.isEnabled) Secondary.copy(alpha = 0.2f) else SurfaceContainerHigh,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = alarm.repeatType,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (alarm.isEnabled) Secondary else OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (alarm.soundEnabled) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Sound On",
                            tint = Secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = alarm.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )

                if (alarm.message.isNotBlank()) {
                    Text(
                        text = alarm.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = OnSurfaceVariant,
                        uncheckedTrackColor = SurfaceContainerHigh
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Alarm",
                        tint = Error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, message: String, hour: Int, minute: Int, repeatType: String, soundEnabled: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("Daily Focus Nudge") }
    var message by remember { mutableStateOf("Time to start your top priorities!") }
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(30) }
    var repeatType by remember { mutableStateOf("DAILY") }
    var soundEnabled by remember { mutableStateOf(true) }

    val repeatOptions = listOf("DAILY", "WEEKDAYS", "WEEKENDS")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SolidSurface,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AlarmAdd,
                    contentDescription = null,
                    tint = Primary
                )
                Text("Set Recurring Alarm", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alarm Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = SurfaceContainerHigh,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Nudge Message / Detail") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = SurfaceContainerHigh,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Pickers (Hour & Minute Selector)
                Text(
                    text = "Scheduled Time: ${String.format(Locale.US, "%02d:%02d (%s)", if (hour % 12 == 0) 12 else hour % 12, minute, if (hour >= 12) "PM" else "AM")}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hour (0-23)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Slider(
                            value = hour.toFloat(),
                            onValueChange = { hour = it.toInt() },
                            valueRange = 0f..23f,
                            steps = 22,
                            colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Minute (0-59)", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Slider(
                            value = minute.toFloat(),
                            onValueChange = { minute = (it.toInt() / 5) * 5 }, // Snap to 5 min increments
                            valueRange = 0f..55f,
                            steps = 10,
                            colors = SliderDefaults.colors(thumbColor = Secondary, activeTrackColor = Secondary)
                        )
                    }
                }

                // Frequency Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Repeat Schedule", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeatOptions.forEach { option ->
                            FilterChip(
                                selected = repeatType == option,
                                onClick = { repeatType = option },
                                label = { Text(option, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = SurfaceContainerHigh,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Sound Option Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerHigh)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = if (soundEnabled) Secondary else OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Play Alarm Sound",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Secondary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, message, hour, minute, repeatType, soundEnabled)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black)
            ) {
                Text("Schedule Alarm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        }
    )
}
