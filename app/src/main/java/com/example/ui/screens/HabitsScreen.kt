package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.HabitEntity
import com.example.ui.theme.*
import com.example.viewmodel.LifeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: LifeViewModel) {
    val allHabits by viewModel.allHabits.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val completedCount = allHabits.count { it.isCompleted }
    val totalCount = allHabits.size
    val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    var habitFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "COMPLETED"

    val filteredHabits = remember(allHabits, habitFilter) {
        when (habitFilter) {
            "PENDING" -> allHabits.filter { !it.isCompleted }
            "COMPLETED" -> allHabits.filter { it.isCompleted }
            else -> allHabits
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var pickerTitle by remember { mutableStateOf("") }
    var pickerTarget by remember { mutableStateOf("HABITS_BANNER") }
    var habitName by remember { mutableStateOf("") }
    var habitTargetText by remember { mutableStateOf("1") }
    var habitUnit by remember { mutableStateOf("Done") }
    var habitIcon by remember { mutableStateOf("check_circle") }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = userProfile?.photoUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80"
                            ),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Secondary.copy(alpha = 0.6f), CircleShape)
                                .clickable {
                                    pickerTitle = "Change Profile Picture"
                                    pickerTarget = "AVATAR"
                                    showImagePicker = true
                                },
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Daily Habits",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddHabitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Habit",
                            tint = OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddHabitDialog = true },
                containerColor = Secondary,
                contentColor = BaseDark,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom Habit")
            }
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
            // Habits Hero Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainer)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = userProfile?.habitsBannerUrl ?: "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=600&auto=format&fit=crop&q=80"
                    ),
                    contentDescription = "Habits Banner Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.65f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )
                IconButton(
                    onClick = {
                        pickerTitle = "Customize Habits Banner Photo"
                        pickerTarget = "HABITS_BANNER"
                        showImagePicker = true
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Change Habits Banner",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "DAILY RITUALS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Secondary
                    )
                    Text(
                        text = "Build Consistency & Mastery",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Habit Progress Stats Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "HABIT FOCUS STREAK",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = if (totalCount == 0) "Set your first habit" else "$completedCount of $totalCount Completed",
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 22.sp),
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.titleMedium,
                                color = Secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Secondary,
                        trackColor = SurfaceContainerHighest
                    )
                }
            }

            // Filter Chips
            if (allHabits.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = habitFilter == "ALL",
                        onClick = { habitFilter = "ALL" },
                        label = { Text("All (${allHabits.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryContainer,
                            selectedLabelColor = OnSecondaryContainer
                        )
                    )
                    FilterChip(
                        selected = habitFilter == "PENDING",
                        onClick = { habitFilter = "PENDING" },
                        label = { Text("Pending (${allHabits.count { !it.isCompleted }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryContainer,
                            selectedLabelColor = OnSecondaryContainer
                        )
                    )
                    FilterChip(
                        selected = habitFilter == "COMPLETED",
                        onClick = { habitFilter = "COMPLETED" },
                        label = { Text("Done (${allHabits.count { it.isCompleted }})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryContainer,
                            selectedLabelColor = OnSecondaryContainer
                        )
                    )
                }
            }

            // Habits Grid (2 columns)
            if (filteredHabits.isEmpty()) {
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
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "No Habits",
                            tint = OnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (allHabits.isEmpty()) "No active habits configured." else "No habits match the '$habitFilter' filter.",
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (allHabits.isEmpty()) {
                            TextButton(onClick = { showAddHabitDialog = true }) {
                                Text("+ Create a Custom Habit", color = Secondary)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val col1Habits = filteredHabits.filterIndexed { idx, _ -> idx % 2 == 0 }
                        col1Habits.forEach { habit ->
                            SwipeToDeleteHabit(
                                habit = habit,
                                onDelete = {
                                    viewModel.deleteHabit(habit)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted: ${habit.name}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreHabit(habit)
                                        }
                                    }
                                }
                            ) {
                                HabitCard(
                                    habit = habit,
                                    onCheckIn = { viewModel.checkInHabit(habit) },
                                    onDelete = { viewModel.deleteHabit(habit) }
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val col2Habits = filteredHabits.filterIndexed { idx, _ -> idx % 2 != 0 }
                        col2Habits.forEach { habit ->
                            SwipeToDeleteHabit(
                                habit = habit,
                                onDelete = {
                                    viewModel.deleteHabit(habit)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted: ${habit.name}",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreHabit(habit)
                                        }
                                    }
                                }
                            ) {
                                HabitCard(
                                    habit = habit,
                                    onCheckIn = { viewModel.checkInHabit(habit) },
                                    onDelete = { viewModel.deleteHabit(habit) }
                                )
                            }
                        }
                    }
                }
            }

            // Habit Heatmap Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Habit Heatmap",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface
                    )
                    Text(
                        text = "LAST 30 DAYS",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 3 rows of 10 cells each representing 30 days based on actual user activity
                        val maxStreak = allHabits.maxOfOrNull { it.streak } ?: 0
                        val todayRatio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                        val cellOpacities = remember(allHabits, completedCount, totalCount) {
                            List(30) { idx ->
                                val daysAgo = 29 - idx
                                when {
                                    totalCount == 0 -> 0.0f
                                    daysAgo == 0 -> todayRatio
                                    daysAgo <= maxStreak -> ((maxStreak - daysAgo + 1).toFloat() / (maxStreak + 1).coerceAtLeast(1)).coerceIn(0.2f, 1.0f)
                                    else -> 0.0f
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (row in 0 until 3) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (col in 0 until 10) {
                                        val idx = (row * 10) + col
                                        val opacity = cellOpacities.getOrElse(idx) { 0.5f }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(horizontal = 3.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (opacity == 0.0f) Color.Transparent
                                                    else Tertiary.copy(alpha = opacity)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (opacity == 0.0f) Color.White.copy(alpha = 0.05f)
                                                    else Color.Transparent,
                                                    RoundedCornerShape(4.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Less consistent",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = OnSurfaceVariant
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(0.1f, 0.4f, 0.7f, 1.0f).forEach { op ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Tertiary.copy(alpha = op))
                                    )
                                }
                            }
                            Text(
                                text = "Unstoppable",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Motivational Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .background(OutlineVariant)
                )

                Text(
                    text = "\"Future you is waiting.\"",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium
                    ),
                    color = OnSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Secondary))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Tertiary))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Primary))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

    if (showImagePicker) {
        com.example.ui.components.ImagePickerDialog(
            title = pickerTitle,
            currentImageUrl = if (pickerTarget == "AVATAR") userProfile?.photoUrl else userProfile?.habitsBannerUrl,
            onDismiss = { showImagePicker = false },
            onImageSelected = { newUrl ->
                if (pickerTarget == "AVATAR") {
                    viewModel.updateProfilePhoto(newUrl)
                } else {
                    viewModel.updateTabBanner("HABITS", newUrl)
                }
            }
        )
    }

    // Add Habit Dialog
    if (showAddHabitDialog) {
        AlertDialog(
            onDismissRequest = { showAddHabitDialog = false },
            containerColor = SolidSurface,
            title = {
                Text("Configure New Habit", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Habit Name") },
                        placeholder = { Text("e.g. Drink Water, Gym Session") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = habitTargetText,
                            onValueChange = { habitTargetText = it },
                            label = { Text("Goal Value") },
                            placeholder = { Text("e.g. 8, 45, 1") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Secondary,
                                unfocusedBorderColor = OutlineVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = habitUnit,
                            onValueChange = { habitUnit = it },
                            label = { Text("Unit") },
                            placeholder = { Text("e.g. L, min, Done") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Secondary,
                                unfocusedBorderColor = OutlineVariant
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Choose Icon Simple Row
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Choose Habit Icon",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                Pair("water_drop", Icons.Default.WaterDrop),
                                Pair("menu_book", Icons.Default.MenuBook),
                                Pair("self_improvement", Icons.Default.SelfImprovement),
                                Pair("fitness_center", Icons.Default.FitnessCenter),
                                Pair("check_circle", Icons.Default.CheckCircle)
                            ).forEach { pair ->
                                val name = pair.first
                                val icon = pair.second
                                val isSelected = habitIcon == name

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Secondary else SurfaceContainerHigh)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.08f),
                                            CircleShape
                                        )
                                        .clickable { habitIcon = name }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = name,
                                        tint = if (isSelected) BaseDark else OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            val target = habitTargetText.toFloatOrNull() ?: 1f
                            viewModel.addHabit(habitName, target, habitUnit, habitIcon)
                            // reset
                            habitName = ""
                            habitTargetText = "1"
                            habitUnit = "Done"
                            habitIcon = "check_circle"
                        }
                        showAddHabitDialog = false
                    }
                ) {
                    Text("ADD HABIT", color = Secondary, style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHabitDialog = false }) {
                    Text("CANCEL", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}

@Composable
fun HabitCard(habit: HabitEntity, onCheckIn: () -> Unit, onDelete: () -> Unit) {
    val progressXp = if (habit.targetValue > 0) habit.currentValue / habit.targetValue else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressXp, label = "Progress")

    val accentColor = when (habit.iconName) {
        "water_drop" -> Secondary
        "menu_book" -> Tertiary
        "self_improvement" -> Primary
        "fitness_center" -> Secondary
        else -> Tertiary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceContainer)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Delete Habit Button in top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .clip(CircleShape)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete Habit",
                tint = OnSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Circular Ring with Vector Icon
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background Circle
                    drawCircle(
                        color = SurfaceContainerHighest,
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    // Foreground Animated Sweep
                    drawArc(
                        color = accentColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Icon(
                    imageVector = when (habit.iconName) {
                        "water_drop" -> Icons.Default.WaterDrop
                        "menu_book" -> Icons.Default.MenuBook
                        "self_improvement" -> Icons.Default.SelfImprovement
                        "fitness_center" -> Icons.Default.FitnessCenter
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = habit.name,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
                    color = OnSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (habit.unit == "Done" && habit.isCompleted) "Done" else "${habit.currentValue.toInt()} / ${habit.targetValue.toInt()} ${habit.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (habit.isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnTertiaryContainer)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = OnTertiaryFixed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "COMPLETE",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnTertiaryFixed
                        )
                    }
                }
            } else {
                Button(
                    onClick = onCheckIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (habit.iconName == "menu_book") SurfaceContainerHigh else SecondaryContainer,
                        contentColor = if (habit.iconName == "menu_book") OnSurface else OnSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    border = if (habit.iconName == "menu_book") BorderStroke(1.dp, OutlineVariant) else null
                ) {
                    Text(
                        text = when (habit.iconName) {
                            "menu_book" -> "START"
                            "fitness_center" -> "RESUME"
                            else -> "CHECK IN"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteHabit(
    habit: HabitEntity,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            if (dismissState.dismissDirection != SwipeToDismissBoxValue.Settled) {
                val color = Error.copy(alpha = 0.85f)
                val alignment = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }
                val icon = Icons.Outlined.Delete

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }
        },
        content = {
            content()
        }
    )
}
