package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkScheduleDialog(
    currentProfile: UserProfileEntity,
    onDismiss: () -> Unit,
    onSaveSchedule: (
        workStartTime: String,
        workEndTime: String,
        wfhDays: String,
        workDays: String,
        weekendDays: String,
        isVacationMode: Boolean,
        vacationStartDate: String?,
        vacationEndDate: String?,
        vacationNotes: String
    ) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Work Hours, 1: WFH & Office, 2: Weekends, 3: Vacations

    // State holders initialized from profile
    var workStart by remember { mutableStateOf(currentProfile.workStartTime.ifBlank { "09:00" }) }
    var workEnd by remember { mutableStateOf(currentProfile.workEndTime.ifBlank { "17:00" }) }
    
    val initialWfh = remember(currentProfile.wfhDays) {
        currentProfile.wfhDays.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }
    var selectedWfhDays by remember { mutableStateOf(initialWfh.ifEmpty { setOf("Mon", "Wed", "Fri") }) }

    val initialWeekends = remember(currentProfile.weekendDays) {
        currentProfile.weekendDays.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }
    var selectedWeekendDays by remember { mutableStateOf(initialWeekends.ifEmpty { setOf("Sat", "Sun") }) }

    var vacationMode by remember { mutableStateOf(currentProfile.isVacationMode) }
    var vacationStart by remember { mutableStateOf(currentProfile.vacationStartDate ?: "") }
    var vacationEnd by remember { mutableStateOf(currentProfile.vacationEndDate ?: "") }
    var vacationNotes by remember { mutableStateOf(currentProfile.vacationNotes) }

    val allWeekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                            .background(Secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkHistory,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Work & Life Schedule",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = OnSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Adjust your daily working window, remote work days, weekend rest periods, and vacation mode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SolidSurface)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(
                        "Hours" to Icons.Default.Schedule,
                        "WFH" to Icons.Default.HomeWork,
                        "Weekends" to Icons.Default.Weekend,
                        "Vacations" to Icons.Default.BeachAccess
                    ).forEachIndexed { index, (title, icon) ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) Secondary else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) BaseDark else OnSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) BaseDark else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // TAB 0: WORK HOURS
                if (selectedTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "DAILY WORKING HOURS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Secondary
                        )

                        // Quick Presets
                        Text(
                            text = "Quick Presets",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "9 AM - 5 PM" to ("09:00" to "17:00"),
                                "8 AM - 4 PM" to ("08:00" to "16:00"),
                                "10 AM - 6 PM" to ("10:00" to "18:00")
                            ).forEach { (label, times) ->
                                val isSelected = workStart == times.first && workEnd == times.second
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        workStart = times.first
                                        workEnd = times.second
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SecondaryContainer,
                                        selectedLabelColor = OnSecondaryContainer,
                                        containerColor = SolidSurface,
                                        labelColor = OnSurfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) Secondary else OutlineVariant.copy(alpha = 0.3f))
                                )
                            }
                        }

                        // Additional Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "7 AM - 3 PM (Early)" to ("07:00" to "15:00"),
                                "10 AM - 2 PM (Part-time)" to ("10:00" to "14:00")
                            ).forEach { (label, times) ->
                                val isSelected = workStart == times.first && workEnd == times.second
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        workStart = times.first
                                        workEnd = times.second
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SecondaryContainer,
                                        selectedLabelColor = OnSecondaryContainer,
                                        containerColor = SolidSurface,
                                        labelColor = OnSurfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) Secondary else OutlineVariant.copy(alpha = 0.3f))
                                )
                            }
                        }

                        Divider(color = OutlineVariant.copy(alpha = 0.2f))

                        // Custom time selectors
                        Text(
                            text = "Custom Times (24h or HH:MM)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = workStart,
                                onValueChange = { workStart = it },
                                label = { Text("Start Time") },
                                placeholder = { Text("09:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                                    focusedContainerColor = SolidSurface,
                                    unfocusedContainerColor = SolidSurface
                                )
                            )

                            OutlinedTextField(
                                value = workEnd,
                                onValueChange = { workEnd = it },
                                label = { Text("End Time") },
                                placeholder = { Text("17:00") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Default.NightsStay, contentDescription = null, tint = Tertiary, modifier = Modifier.size(18.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                                    focusedContainerColor = SolidSurface,
                                    unfocusedContainerColor = SolidSurface
                                )
                            )
                        }

                        // Summary badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Secondary.copy(alpha = 0.1f))
                                .border(1.dp, Secondary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Daily work focus window: $workStart to $workEnd. The AI coach will schedule deep work priorities inside this block.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }

                // TAB 1: WORK FROM HOME (WFH) & HYBRID
                if (selectedTab == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "WORK FROM HOME (WFH) SCHEDULE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Secondary
                        )
                        Text(
                            text = "Select which days you work remotely from home versus in-office.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        // Quick Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedWfhDays == setOf("Mon", "Wed", "Fri"),
                                onClick = { selectedWfhDays = setOf("Mon", "Wed", "Fri") },
                                label = { Text("Hybrid (M/W/F)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainer,
                                    selectedLabelColor = OnSecondaryContainer,
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )
                            FilterChip(
                                selected = selectedWfhDays == setOf("Mon", "Tue", "Wed", "Thu", "Fri"),
                                onClick = { selectedWfhDays = setOf("Mon", "Tue", "Wed", "Thu", "Fri") },
                                label = { Text("100% Remote", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainer,
                                    selectedLabelColor = OnSecondaryContainer,
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )
                            FilterChip(
                                selected = selectedWfhDays.isEmpty(),
                                onClick = { selectedWfhDays = emptySet() },
                                label = { Text("In-Office Only", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainer,
                                    selectedLabelColor = OnSecondaryContainer,
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )
                        }

                        // Day of week selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allWeekDays.forEach { day ->
                                val isWeekend = selectedWeekendDays.contains(day)
                                val isWfh = selectedWfhDays.contains(day) && !isWeekend

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isWeekend) SolidSurface.copy(alpha = 0.5f) else SolidSurface)
                                        .border(
                                            1.dp,
                                            if (isWfh) Secondary.copy(alpha = 0.5f) else OutlineVariant.copy(alpha = 0.25f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = !isWeekend) {
                                            selectedWfhDays = if (selectedWfhDays.contains(day)) {
                                                selectedWfhDays - day
                                            } else {
                                                selectedWfhDays + day
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isWfh) Secondary else if (isWeekend) OutlineVariant.copy(alpha = 0.2f) else Primary.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.take(1),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isWfh) BaseDark else OnSurface
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = day,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isWeekend) OnSurfaceVariant.copy(alpha = 0.5f) else OnSurface
                                            )
                                            Text(
                                                text = if (isWeekend) "Weekend Rest" else if (isWfh) "Work From Home (Remote)" else "In-Office (In-Person)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isWeekend) OnSurfaceVariant.copy(alpha = 0.5f) else if (isWfh) Secondary else OnSurfaceVariant
                                            )
                                        }
                                    }

                                    if (!isWeekend) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isWfh) Secondary.copy(alpha = 0.2f) else Primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isWfh) Icons.Default.Home else Icons.Default.Business,
                                                    contentDescription = null,
                                                    tint = if (isWfh) Secondary else Primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = if (isWfh) "WFH" else "OFFICE",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isWfh) Secondary else Primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: WEEKENDS & REST DAYS
                if (selectedTab == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "WEEKENDS & REST DAYS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Secondary
                        )
                        Text(
                            text = "Customize your weekly rest days. Habit routines and morning briefings adapt to weekend pacing on these days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        // Quick Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedWeekendDays == setOf("Sat", "Sun"),
                                onClick = { selectedWeekendDays = setOf("Sat", "Sun") },
                                label = { Text("Sat & Sun (Standard)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainer,
                                    selectedLabelColor = OnSecondaryContainer,
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )
                            FilterChip(
                                selected = selectedWeekendDays == setOf("Fri", "Sat"),
                                onClick = { selectedWeekendDays = setOf("Fri", "Sat") },
                                label = { Text("Fri & Sat", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainer,
                                    selectedLabelColor = OnSecondaryContainer,
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )
                        }

                        // Day chips for custom weekend selection
                        Text(
                            text = "Select Rest Days:",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            allWeekDays.forEach { day ->
                                val isSelected = selectedWeekendDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Tertiary else SolidSurface)
                                        .border(
                                            1.dp,
                                            if (isSelected) Tertiary else OutlineVariant.copy(alpha = 0.4f),
                                            CircleShape
                                        )
                                        .clickable {
                                            selectedWeekendDays = if (selectedWeekendDays.contains(day)) {
                                                selectedWeekendDays - day
                                            } else {
                                                selectedWeekendDays + day
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) BaseDark else OnSurface
                                    )
                                }
                            }
                        }

                        // Summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Tertiary.copy(alpha = 0.1f))
                                .border(1.dp, Tertiary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Weekend, contentDescription = null, tint = Tertiary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Rest days set: ${if (selectedWeekendDays.isEmpty()) "None (7-day schedule)" else selectedWeekendDays.joinToString(", ")}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }

                // TAB 3: VACATIONS & TIME OFF
                if (selectedTab == 3) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "VACATIONS & TIME OFF",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Secondary
                        )
                        Text(
                            text = "Enable Vacation Mode to freeze streak penalties, pause aggressive rollover nudges, and receive recovery-focused AI briefings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        // Vacation Mode switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (vacationMode) Tertiary.copy(alpha = 0.15f) else SolidSurface)
                                .border(
                                    1.dp,
                                    if (vacationMode) Tertiary else OutlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (vacationMode) Tertiary else SolidSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BeachAccess,
                                        contentDescription = null,
                                        tint = if (vacationMode) BaseDark else Tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Vacation Mode Active",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (vacationMode) Tertiary else OnSurface
                                    )
                                    Text(
                                        text = if (vacationMode) "Streak protection & relaxation mode on" else "Normal productivity tracking active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = vacationMode,
                                onCheckedChange = { vacationMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Tertiary,
                                    checkedTrackColor = Tertiary.copy(alpha = 0.3f),
                                    uncheckedThumbColor = OnSurfaceVariant.copy(alpha = 0.4f),
                                    uncheckedTrackColor = SurfaceContainerHigh
                                )
                            )
                        }

                        // Vacation Notes / Destination
                        OutlinedTextField(
                            value = vacationNotes,
                            onValueChange = { vacationNotes = it },
                            label = { Text("Vacation Trip / Destination (Optional)") },
                            placeholder = { Text("e.g. Hawaii Trip 🏖️ or Family Time 🌴") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Secondary,
                                unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                                focusedContainerColor = SolidSurface,
                                unfocusedContainerColor = SolidSurface
                            )
                        )

                        // Date Ranges
                        Text(
                            text = "Vacation Date Range (Optional YYYY-MM-DD)",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = vacationStart,
                                onValueChange = { vacationStart = it },
                                label = { Text("Start Date") },
                                placeholder = { Text("2026-08-25") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                                    focusedContainerColor = SolidSurface,
                                    unfocusedContainerColor = SolidSurface
                                )
                            )

                            OutlinedTextField(
                                value = vacationEnd,
                                onValueChange = { vacationEnd = it },
                                label = { Text("End Date") },
                                placeholder = { Text("2026-09-01") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                                    focusedContainerColor = SolidSurface,
                                    unfocusedContainerColor = SolidSurface
                                )
                            )
                        }

                        // Quick date range presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
                            val plus7Str = remember {
                                val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }
                                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
                            }
                            val plus14Str = remember {
                                val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 14) }
                                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
                            }

                            FilterChip(
                                selected = vacationStart == todayStr && vacationEnd == plus7Str,
                                onClick = {
                                    vacationStart = todayStr
                                    vacationEnd = plus7Str
                                    vacationMode = true
                                },
                                label = { Text("Next 7 Days", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TertiaryContainer,
                                    selectedLabelColor = OnTertiaryContainer,
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )

                            FilterChip(
                                selected = vacationStart == todayStr && vacationEnd == plus14Str,
                                onClick = {
                                    vacationStart = todayStr
                                    vacationEnd = plus14Str
                                    vacationMode = true
                                },
                                label = { Text("Next 14 Days", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TertiaryContainer,
                                    selectedLabelColor = OnTertiaryContainer,
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )

                            FilterChip(
                                selected = false,
                                onClick = {
                                    vacationStart = ""
                                    vacationEnd = ""
                                    vacationMode = false
                                },
                                label = { Text("Clear Dates", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SolidSurface,
                                    labelColor = OnSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val workDaysList = allWeekDays.filterNot { selectedWeekendDays.contains(it) }.joinToString(",")
                    val wfhDaysStr = selectedWfhDays.joinToString(",")
                    val weekendDaysStr = selectedWeekendDays.joinToString(",")

                    onSaveSchedule(
                        workStart,
                        workEnd,
                        wfhDaysStr,
                        workDaysList,
                        weekendDaysStr,
                        vacationMode,
                        vacationStart.ifBlank { null },
                        vacationEnd.ifBlank { null },
                        vacationNotes
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_schedule_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = BaseDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Schedule", color = BaseDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        },
        containerColor = SolidSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
