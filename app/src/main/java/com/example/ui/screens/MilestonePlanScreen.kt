package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.GoalEntity
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestonePlanScreen(viewModel: LifeViewModel) {
    val allGoals by viewModel.allGoals.collectAsState()
    val selectedGoalId by viewModel.selectedGoalId.collectAsState()
    val milestones by viewModel.selectedMilestones.collectAsState()

    val goal = allGoals.find { it.id == selectedGoalId } ?: allGoals.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vision Timeline",
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
            goal?.let { g ->
                // Hero Vision Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceContainer)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = g.visionImage ?: "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=600&auto=format&fit=crop&q=80"
                        ),
                        contentDescription = g.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SecondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${g.domain.uppercase()} • ${g.horizon.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = OnSecondaryContainer
                            )
                        }
                        Text(
                            text = g.title,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                            color = Color.White
                        )
                    }
                }

                // Completion progress card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VISION COMPLETED",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                            Text(
                                text = "${g.progressPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(g.progressPercent / 100f)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(SecondaryContainer, Tertiary)
                                        )
                                    )
                            )
                        }

                        Text(
                            text = "Estimated Horizon timeline: ${g.targetTimeline}",
                            style = TextStyle(fontSize = 12.sp, color = OnSurfaceVariant)
                        )
                    }
                }
            }

            // Milestone roadmap
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "ROADMAP MILESTONES",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                if (milestones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Configuring vision steps...", color = OnSurfaceVariant)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        milestones.forEachIndexed { idx, milestone ->
                            val isActive = milestone.status == "ACTIVE"
                            val isCompleted = milestone.status == "COMPLETED"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isActive) SurfaceContainerHigh else SurfaceContainer)
                                    .border(
                                        1.dp,
                                        if (isActive) Secondary else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        if (isActive || isCompleted) {
                                            viewModel.navigateTo("MILESTONE_CHECKIN", milestoneId = milestone.id)
                                        }
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCompleted) OnTertiaryContainer
                                                else if (isActive) SecondaryContainer.copy(alpha = 0.1f)
                                                else SurfaceContainerHighest
                                            )
                                            .border(
                                                1.dp,
                                                if (isActive) Secondary else Color.Transparent,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (milestone.iconName) {
                                                "payments" -> Icons.Default.Payments
                                                "sports_motorsports" -> Icons.Default.SportsMotorsports
                                                "shield" -> Icons.Default.Shield
                                                "two_wheeler" -> Icons.Default.TwoWheeler
                                                "architecture" -> Icons.Default.Architecture
                                                "groups" -> Icons.Default.Groups
                                                "terminal" -> Icons.Default.Terminal
                                                else -> Icons.Default.WorkspacePremium
                                            },
                                            contentDescription = milestone.title,
                                            tint = if (isCompleted) OnTertiaryFixed
                                                   else if (isActive) Secondary
                                                   else OnSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "PHASE ${idx + 1} • ${milestone.title}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCompleted) OnSurfaceVariant.copy(alpha = 0.6f)
                                                    else if (isActive) OnSurface
                                                    else OnSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = milestone.description,
                                            style = TextStyle(fontSize = 13.sp, color = OnSurfaceVariant),
                                            maxLines = 2
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isCompleted) OnTertiaryContainer
                                            else if (isActive) SecondaryContainer
                                            else SurfaceContainerHighest
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = milestone.status,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                        color = if (isCompleted) OnTertiaryFixed
                                               else if (isActive) OnSecondaryContainer
                                               else OnSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
