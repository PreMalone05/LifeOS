package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.HabitEntity
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(viewModel: LifeViewModel) {
    val allHabits by viewModel.allHabits.collectAsState()

    val completedCount = allHabits.count { it.isCompleted }
    val totalCount = allHabits.size
    val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAUwhyQMwHw8x6bibeQ9OmkeLUQHUgax6YWlv-a2Jx9BBWMZ8AYE6bLW54tPmH50M0NOtgxgut9942nC0N83WF7mOjQYOah4hY_uTggquLNAX9Wg2Ikt2yaqDusHk0voduYzYOKHagA1FmmMczsaJ5xLt3PaBAqrFcApuu-_7QcA9IOu3PsFFt_ByWHyx2FptclrRAFbtXxep7TORwltElkyA1kKQ4ewcA-jb04YR4t8hrkEBib88LGQiKUy7b_hOyvsdcIHK2phgYo"
                            ),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Daily Habits",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = OnSurface
                        )
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Momentum / Progress Summary Header
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "MOMENTUM",
                    style = MaterialTheme.typography.labelMedium,
                    color = Secondary
                )
                Text(
                    text = "$progressPercent% Complete",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    color = OnSurface
                )
                Text(
                    text = "You're on a 12-day streak. Keep it going.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // Habits Grid (2 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val col1Habits = allHabits.filterIndexed { idx, _ -> idx % 2 == 0 }
                    col1Habits.forEach { habit ->
                        HabitCard(habit = habit, onCheckIn = { viewModel.checkInHabit(habit) })
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val col2Habits = allHabits.filterIndexed { idx, _ -> idx % 2 != 0 }
                    col2Habits.forEach { habit ->
                        HabitCard(habit = habit, onCheckIn = { viewModel.checkInHabit(habit) })
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
                        // 3 rows of 10 cells each representing 30 days
                        val cellOpacities = listOf(
                            0.2f, 0.4f, 1.0f, 0.6f, 0.1f, 0.8f, 1.0f, 1.0f, 0.9f, 0.4f,
                            1.0f, 1.0f, 1.0f, 0.1f, 0.0f, 0.8f, 1.0f, 1.0f, 1.0f, 0.9f,
                            1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f
                        )

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

@Composable
fun HabitCard(habit: HabitEntity, onCheckIn: () -> Unit) {
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
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                    color = OnSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (habit.unit == "Done" && habit.isCompleted) "Done" else "${habit.currentValue.toInt()} / ${habit.targetValue.toInt()} ${habit.unit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
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
