package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuningScreen(viewModel: LifeViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    var selectedArchetype by remember { mutableStateOf("The Stoic Mentor") }

    // Initialize selected archetype based on current settings
    LaunchedEffect(userProfile) {
        userProfile?.let {
            selectedArchetype = it.coachPersonality
        }
    }

    var isPlayingPreview by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customize Coach",
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "RE-ENGAGEMENT MODEL PROFILE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Secondary
                )
                Text(
                    text = "Choose Your Expert",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    color = OnSurface
                )
                Text(
                    text = "Select the tone, priority focus, and conversational styling of your automated daily LifeOS check-ins.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // Personality list
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(
                    Triple("The Stoic Mentor", "Duty-focused, structured accountability, long-term commitments, and zero-compromise on streaks.", Icons.Default.Shield),
                    Triple("The Strategic Architect", "Hyper-logical breakdown, efficiency time-blocks, data correlation, and micro-optimization tips.", Icons.Default.Architecture),
                    Triple("The Philosophical Guide", "Existential reflection, mindfulness pacing, deep 'Why' questions, and balanced work-life roots.", Icons.Default.MenuBook),
                    Triple("The High-Performance Coach", "High energy, positive reinforcement, athletic sprint pacing, and competitive reward setups.", Icons.Default.Speed)
                ).forEach { triple ->
                    val name = triple.first
                    val desc = triple.second
                    val icon = triple.third
                    val isSelected = selectedArchetype == name

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SurfaceContainerHigh else SurfaceContainer)
                            .border(
                                1.dp,
                                if (isSelected) Secondary else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedArchetype = name }
                            .padding(20.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SecondaryContainer else SurfaceContainerHighest)
                                .border(1.dp, if (isSelected) Secondary else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = if (isSelected) OnSecondaryContainer else OnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                                color = if (isSelected) Secondary else OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = OnSurfaceVariant
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedArchetype = name },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Secondary,
                                unselectedColor = OutlineVariant
                            )
                        )
                    }
                }
            }

            // Interactive Voice waveform preview card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "VOICE PREVIEW SYNTHESIS",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )

                    // Audio wave graphics
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "Waveform")
                        val scaleAnimations = (0 until 16).map { idx ->
                            infiniteTransition.animateFloat(
                                initialValue = 0.2f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = 300 + (idx * 50),
                                        easing = LinearEasing
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "Bar$idx"
                            )
                        }

                        (0 until 16).forEach { idx ->
                            val scale = if (isPlayingPreview) scaleAnimations[idx].value else 0.15f
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(scale)
                                    .clip(CircleShape)
                                    .background(if (isPlayingPreview) Secondary else OnSurfaceVariant.copy(alpha = 0.3f))
                            )
                        }
                    }

                    Button(
                        onClick = { isPlayingPreview = !isPlayingPreview },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingPreview) ErrorContainer else SecondaryContainer,
                            contentColor = if (isPlayingPreview) OnErrorContainer else OnSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isPlayingPreview) "STOP PREVIEW" else "PREVIEW ARCHETYPE RESPONSE",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Save Config
            Button(
                onClick = {
                    viewModel.setCoachPersonality(selectedArchetype)
                    viewModel.navigateTo("PROFILE")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnSurface,
                    contentColor = BaseDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "APPLY PROFILE SETUP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
