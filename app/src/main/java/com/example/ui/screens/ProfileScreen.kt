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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: LifeViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    var showResetConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mastery Metrics",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
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
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Profile Header / Avatar
            userProfile?.let { profile ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                            .border(2.dp, Secondary, CircleShape)
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCbG_ReN96DPYGs021D4500gJ8BkR_6cMbOhCZ1GRUtjkpD5L-bNbVKpxLGcHfAqquJM5idvfZlsNkPeIYdt-CPZfnMx9JLgjcBmKPaIhXlc2_c2IlbwpvR7ovEXBkHtbmLCLLk7yOCluE2pkh3l6lFzKwVENPf9vb2pZiGMcs4b7-EU_Y119VJgB9pFr7drwBWlYj-j9yq4NAk-EX2opJ6ymd1mNC3Cz00BI3hr-_mUhb387JF-Y0mvydPsZrPRda2DmU1w2WrdB4R"
                            ),
                            contentDescription = "Julian Thorne Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                            color = OnSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "LEVEL ${profile.level}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Secondary
                            )
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(OnSurfaceVariant.copy(alpha = 0.4f))
                            )
                            Text(
                                text = profile.coachPersonality,
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Gamified metrics bento row
            userProfile?.let { profile ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        Triple("148", "COMPLETED TASKS", Icons.Default.TaskAlt),
                        Triple("${profile.uptime}%", "WEEKLY UPTIME", Icons.Default.TrendingUp),
                        Triple("TOP ${profile.rankPercent}%", "WORLD RANK", Icons.Default.Leaderboard)
                    ).forEach { triple ->
                        val value = triple.first
                        val label = triple.second
                        val icon = triple.third

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainer)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                                        color = OnSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                        color = OnSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mastery Tree
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "MASTERY PATHWAY & SKILLS",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        Triple("Stoic Prioritization", "Level II • Shield Node", Icons.Default.Shield),
                        Triple("Strategic Planning", "Level IV • Architect Node", Icons.Default.Architecture),
                        Triple("Flow Engine Optimization", "Level I • Speed Node", Icons.Default.Speed),
                        Triple("Adaptive Resilience", "Level III • Spark Node", Icons.Default.AutoAwesome)
                    ).forEach { triple ->
                        val name = triple.first
                        val level = triple.second
                        val icon = triple.third

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainer)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Secondary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = name,
                                        tint = Secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = OnSurface
                                    )
                                    Text(
                                        text = level,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Unlocked",
                                tint = Tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Coaching tuning shortcut card
            userProfile?.let { profile ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ACTIVE ARCHITECT",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                            .clickable { viewModel.navigateTo("COACH_TUNING") }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Tertiary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Coach Profile",
                                    tint = Tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = profile.coachPersonality,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Tap to tune AI coach parameters",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Tune Coach",
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }

            // Configuration Options (System resetting)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Error.copy(alpha = 0.05f))
                    .border(1.dp, Error.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .clickable { showResetConfirmation = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset Local Configurations & Reload",
                    color = Error,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            containerColor = SurfaceContainer,
            title = {
                Text("Reset All Data?", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            },
            text = {
                Text(
                    "This action is irreversible and will reset your user profile, tasks, habits, and active milestones back to default settings.",
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSystemData()
                        showResetConfirmation = false
                    }
                ) {
                    Text("RESET SYSTEM", color = Error, style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("KEEP DATA", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
}
