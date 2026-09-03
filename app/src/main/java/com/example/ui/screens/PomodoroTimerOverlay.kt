package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.LifeViewModel
import com.example.viewmodel.PomodoroMode
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PomodoroTimerOverlay(
    viewModel: LifeViewModel,
    modifier: Modifier = Modifier
) {
    val timeLeft by viewModel.timeLeftSeconds.collectAsState()
    val totalDuration by viewModel.totalDurationSeconds.collectAsState()
    val isRunning by viewModel.isTimerRunning.collectAsState()
    val currentMode by viewModel.currentTimerMode.collectAsState()
    val completedRounds by viewModel.completedRounds.collectAsState()
    val focusTarget by viewModel.focusTarget.collectAsState()
    val aiEncouragement by viewModel.aiEncouragement.collectAsState()
    val isFetchingEncouragement by viewModel.isFetchingEncouragement.collectAsState()
    val selectedNoise by viewModel.selectedNoise.collectAsState()
    val noiseVolume by viewModel.noiseVolume.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    // Pulsing animation for the active state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Layout alignment: Floating at BottomEnd, sitting perfectly on top of all screens
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Floating Mini Timer Pill
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            val modeColor = when (currentMode) {
                PomodoroMode.WORK -> MaterialTheme.colorScheme.secondary
                PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
                PomodoroMode.LONG_BREAK -> Color(0xFF10B981) // Green accent
            }

            Row(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .background(Color(0xFF1E2026).copy(alpha = 0.9f))
                    .border(
                        width = 1.5.dp,
                        color = modeColor.copy(alpha = if (isRunning) glowAlpha else 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { isExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Radial mini ring with time inside
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = if (totalDuration > 0) timeLeft.toFloat() / totalDuration else 0f
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background circle path
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        // Progress sweep path
                        drawArc(
                            color = modeColor,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Mini play/pause indicator dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) modeColor else Color.White.copy(alpha = 0.5f))
                    )
                }

                // Time String
                Text(
                    text = formatTime(timeLeft),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Compact active mode label
                Text(
                    text = when (currentMode) {
                        PomodoroMode.WORK -> "Work"
                        PomodoroMode.SHORT_BREAK -> "Short Break"
                        PomodoroMode.LONG_BREAK -> "Long Break"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = modeColor
                )
            }
        }
    }

    // Expanded Full Screen / Dialog Focus Board
    if (isExpanded) {
        Dialog(onDismissRequest = { isExpanded = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF1E2026), // Solid dark slate for premium look
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                tonalElevation = 12.dp
            ) {
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current
                var targetText by remember { mutableStateOf(focusTarget) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "DEEP FOCUS ENGINE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Mode Selection Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PomodoroMode.values().forEach { mode ->
                            val isSelected = currentMode == mode
                            val modeColor = when (mode) {
                                PomodoroMode.WORK -> MaterialTheme.colorScheme.secondary
                                PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
                                PomodoroMode.LONG_BREAK -> Color(0xFF10B981)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) modeColor.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) modeColor else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.setTimerMode(mode)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label.replace(" Session", ""),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) modeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Main Timer Display (Big Ring)
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val progress = if (totalDuration > 0) timeLeft.toFloat() / totalDuration else 0f
                        val mainColor = when (currentMode) {
                            PomodoroMode.WORK -> MaterialTheme.colorScheme.secondary
                            PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
                            PomodoroMode.LONG_BREAK -> Color(0xFF10B981)
                        }

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track
                            drawCircle(
                                color = Color.White.copy(alpha = 0.04f),
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Outer Neon Glow
                            drawCircle(
                                color = mainColor.copy(alpha = 0.08f),
                                style = Stroke(width = 16.dp.toPx())
                            )
                            // Active progress sweep
                            drawArc(
                                color = mainColor,
                                startAngle = -90f,
                                sweepAngle = progress * 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formatTime(timeLeft),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    isRunning -> "ACTIVE"
                                    timeLeft == totalDuration -> "READY"
                                    else -> "PAUSED"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = if (isRunning) mainColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset button
                        IconButton(
                            onClick = { viewModel.resetTimer() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Timer",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Play/Pause button
                        val mainColor = when (currentMode) {
                            PomodoroMode.WORK -> MaterialTheme.colorScheme.secondary
                            PomodoroMode.SHORT_BREAK -> MaterialTheme.colorScheme.tertiary
                            PomodoroMode.LONG_BREAK -> Color(0xFF10B981)
                        }

                        IconButton(
                            onClick = {
                                if (isRunning) {
                                    viewModel.pauseTimer()
                                } else {
                                    viewModel.startTimer()
                                }
                            },
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(mainColor)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Play",
                                tint = Color(0xFF0F1115),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Completed Sessions Counter
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(4) { idx ->
                                    val isRoundDone = idx < (completedRounds % 4)
                                    Icon(
                                        imageVector = if (isRoundDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isRoundDone) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Rounds: $completedRounds",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Focus Soundscapes (White Noise options)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "AMBIENT FOCUS SOUNDS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Horizontal selectable sounds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            com.example.audio.NoiseType.values().forEach { noise ->
                                val isSelected = selectedNoise == noise
                                val accentColor = MaterialTheme.colorScheme.secondary

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) accentColor else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.setNoiseType(noise)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (noise) {
                                            com.example.audio.NoiseType.OFF -> "Off"
                                            com.example.audio.NoiseType.WHITE -> "White"
                                            com.example.audio.NoiseType.PINK -> "Pink"
                                            com.example.audio.NoiseType.BROWN -> "Brown"
                                            com.example.audio.NoiseType.RAIN -> "Rain"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Volume Slider (only visible if a noise is selected)
                        if (selectedNoise != com.example.audio.NoiseType.OFF) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeDown,
                                    contentDescription = "Lower Volume",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Slider(
                                    value = noiseVolume,
                                    onValueChange = { viewModel.setNoiseVolume(it) },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.secondary,
                                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Higher Volume",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Focus Target Input (Gamified commitment input)
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = {
                            targetText = it
                            viewModel.updateFocusTarget(it)
                        },
                        label = { Text("What are you focusing on?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        placeholder = { Text("e.g. Architect vision roadmap...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            if (targetText.isNotBlank()) {
                                viewModel.generateFocusEncouragement()
                            }
                        }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Gemini AI Motivation Area
                    if (targetText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "GEMINI FOCUS ACCELERATOR",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    // Refresh inspiration button
                                    if (!isFetchingEncouragement) {
                                        IconButton(
                                            onClick = { viewModel.generateFocusEncouragement() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Psychology,
                                                contentDescription = "Get motivation",
                                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                if (isFetchingEncouragement) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Gemini is writing inspiration...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = aiEncouragement.ifBlank { "Specify your commitment above and click the Brain icon to generate tailor-made Gemini motivation!" },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontStyle = FontStyle.Italic,
                                            lineHeight = 16.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
