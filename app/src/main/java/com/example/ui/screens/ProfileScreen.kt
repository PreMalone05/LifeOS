package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: LifeViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    
    val completedTasksCount = allTasks.count { it.isCompleted }
    
    val smartNotificationsEnabled by viewModel.smartNotificationsEnabled.collectAsState()
    val pomodoroNotificationsEnabled by viewModel.pomodoroNotificationsEnabled.collectAsState()
    val habitNotificationsEnabled by viewModel.habitNotificationsEnabled.collectAsState()
    val isGeneratingNotification by viewModel.isGeneratingNotification.collectAsState()
    val calendarPermissionGranted by viewModel.calendarPermissionGranted.collectAsState()
    val activeAiProviderId by viewModel.activeAiProviderId.collectAsState()
    val activeAiModelId by viewModel.activeAiModelId.collectAsState()
    
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showGoogleLoginDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var isSigningInGoogle by remember { mutableStateOf(false) }
    
    var editName by remember { mutableStateOf("") }
    var editVibe by remember { mutableStateOf("") }
    var startFreshChecked by remember { mutableStateOf(false) }
    
    var customGoogleName by remember { mutableStateOf("") }
    var customGoogleEmail by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var showAlarmsSheet by remember { mutableStateOf(false) }
    var showWorkScheduleDialog by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
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
                                    showImagePicker = true
                                },
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = "Mastery Metrics",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        userProfile?.let {
                            editName = it.name
                            editVibe = it.currentVibe
                            showEditProfileDialog = true
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
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
                            .clickable {
                                editName = profile.name
                                editVibe = profile.currentVibe
                                showEditProfileDialog = true
                            }
                    ) {
                        // Avatar view
                        if (!profile.photoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SolidSurface)
                                    .border(2.dp, Secondary, CircleShape)
                                    .clickable { showImagePicker = true }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = profile.photoUrl),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else if (profile.isGoogleLinked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Tertiary.copy(alpha = 0.2f))
                                    .border(2.dp, Tertiary, CircleShape)
                                    .clickable { showImagePicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Tertiary
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SolidSurface)
                                    .border(2.dp, Secondary, CircleShape)
                                    .clickable { showImagePicker = true }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80"
                                    ),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Edit Profile Photo Button Badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Secondary)
                                .border(2.dp, Background, CircleShape)
                                .align(Alignment.BottomEnd)
                                .clickable { showImagePicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Picture",
                                tint = OnSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
                        
                        Text(
                            text = "\"${profile.currentVibe}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(color = OnSurfaceVariant),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
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

            // Google Integration Card
            userProfile?.let { profile ->
                if (profile.isGoogleLinked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Tertiary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF4285F4)
                                        )
                                    }
                                    Text(
                                        text = "Google Account Linked",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = OnSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Tertiary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        color = Tertiary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Text(
                                text = profile.email ?: "SeifHazem129@gmail.com",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )

                            Divider(color = Color.White.copy(alpha = 0.05f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.unlinkGoogleAccount() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                                ) {
                                    Text("Disconnect Google Account", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                            .padding(18.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Link Your Google Account",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Use your Google identity to personalize your coaching files and progress summaries directly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            Button(
                                onClick = { showGoogleLoginDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "G",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF4285F4)
                                        )
                                    }
                                    Text(
                                        text = "Sign in with Google",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
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
                        Triple(completedTasksCount.toString(), "COMPLETED TASKS", Icons.Default.TaskAlt),
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

            // AI Brain & Multi-Provider Settings Card
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "AI BRAIN & NEURAL ENGINE",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.navigateTo("AI_SETTINGS") }
                        .padding(20.dp),
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
                                .background(Secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Settings",
                                tint = Secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Provider Architecture",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Secondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = activeAiProviderId.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Secondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Active Model: $activeAiModelId",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Configure AI Engine",
                        tint = OnSurfaceVariant
                    )
                }
            }

            // Smart Notifications Control Card
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SMART COGNITIVE ALERTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Smart notifications toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    .background(Tertiary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Smart Nudges",
                                    tint = Tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Smart Coach Nudges",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "AI coach will generate smart, adaptive motivational warnings.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = smartNotificationsEnabled,
                            onCheckedChange = { viewModel.setSmartNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Tertiary,
                                checkedTrackColor = Tertiary.copy(alpha = 0.3f),
                                uncheckedThumbColor = OnSurfaceVariant.copy(alpha = 0.4f),
                                uncheckedTrackColor = SurfaceContainerHigh
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Pomodoro toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    .background(Secondary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Focus Timer",
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Pomodoro Reminders",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Get notified immediately when work/break timers finish.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = pomodoroNotificationsEnabled,
                            onCheckedChange = { viewModel.setPomodoroNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Secondary,
                                checkedTrackColor = Secondary.copy(alpha = 0.3f),
                                uncheckedThumbColor = OnSurfaceVariant.copy(alpha = 0.4f),
                                uncheckedTrackColor = SurfaceContainerHigh
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Daily habit reminder toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = "Habits Alerts",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Habit Check-ins",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Receive proactive nudges to complete your pending daily habits.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = habitNotificationsEnabled,
                            onCheckedChange = { viewModel.setHabitNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                uncheckedThumbColor = OnSurfaceVariant.copy(alpha = 0.4f),
                                uncheckedTrackColor = SurfaceContainerHigh
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Recurring Alarms Manager
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAlarmsSheet = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    .background(Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = "Recurring Alarms",
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Recurring Alarms & Alarms",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Configure custom repeating alarms, notifications & test alerts.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Alarms",
                            tint = Primary
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Testing button for dynamic AI Nudges
                    Button(
                        onClick = { viewModel.triggerSmartNotificationManual() },
                        enabled = !isGeneratingNotification,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (smartNotificationsEnabled) Tertiary else OnSurface.copy(alpha = 0.1f),
                            contentColor = if (smartNotificationsEnabled) Color.Black else OnSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isGeneratingNotification) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "SYNCHRONIZING WITH COACH...",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.SendAndArchive,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "TRIGGER TEST SMART NUDGE",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Work, WFH, Weekends & Vacation Settings Card
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "WORK, WFH & VACATIONS",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                val profile = userProfile ?: com.example.data.UserProfileEntity()
                val todayStr = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
                val dayStatus = viewModel.getScheduleStatusForDate(todayStr)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header row with Icon and status summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Secondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkHistory,
                                    contentDescription = "Work Schedule",
                                    tint = Secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Work & Rest Routine",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Today: ${dayStatus.label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (dayStatus.isVacation) Tertiary else if (dayStatus.isWfh) Secondary else OnSurfaceVariant
                                )
                            }
                        }

                        // Vacation quick toggle indicator
                        if (profile.isVacationMode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Tertiary.copy(alpha = 0.2f))
                                    .border(1.dp, Tertiary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "VACATION ON",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Tertiary
                                )
                            }
                        }
                    }

                    // Key Schedule Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Work Hours Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SolidSurface)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                                    Text("HOURS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = OnSurfaceVariant)
                                }
                                Text(
                                    text = "${profile.workStartTime} – ${profile.workEndTime}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Daily Target",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                        }

                        // WFH Days Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SolidSurface)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                                    Text("WFH DAYS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = OnSurfaceVariant)
                                }
                                Text(
                                    text = profile.wfhDays.ifBlank { "None (Office)" },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Remote Schedule",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Weekends & Vacations Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Weekends Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SolidSurface)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Weekend, contentDescription = null, tint = Tertiary, modifier = Modifier.size(14.dp))
                                    Text("WEEKENDS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = OnSurfaceVariant)
                                }
                                Text(
                                    text = profile.weekendDays.ifBlank { "Sat, Sun" },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = "Rest Days",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = OnSurfaceVariant
                                )
                            }
                        }

                        // Vacation Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (profile.isVacationMode) Tertiary.copy(alpha = 0.12f) else SolidSurface)
                                .border(
                                    1.dp,
                                    if (profile.isVacationMode) Tertiary.copy(alpha = 0.35f) else OutlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.BeachAccess, contentDescription = null, tint = Tertiary, modifier = Modifier.size(14.dp))
                                    Text("VACATION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = OnSurfaceVariant)
                                }
                                Text(
                                    text = if (profile.isVacationMode) "Active 🏖️" else if (!profile.vacationStartDate.isNullOrBlank()) "Upcoming" else "Not Set",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (profile.isVacationMode) Tertiary else OnSurface
                                )
                                Text(
                                    text = if (profile.vacationNotes.isNotBlank()) profile.vacationNotes else "Time Off & Reset",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = OnSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Action Button
                    Button(
                        onClick = { showWorkScheduleDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = null,
                            tint = OnSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADJUST WORK, WFH & VACATIONS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSecondaryContainer
                        )
                    }
                }
            }

            // Calendar Integration Card
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "CALENDAR INTEGRATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    viewModel.setCalendarPermissionGranted(isGranted)
                    if (isGranted) {
                        val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        viewModel.loadCalendarEvents(todayDateStr)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                    .background(Secondary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Calendar",
                                    tint = Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "System Calendar Link",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = if (calendarPermissionGranted) "Connected to local calendar system" else "Sync local device calendar with LifeOS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = calendarPermissionGranted,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    permissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
                                } else {
                                    viewModel.setCalendarPermissionGranted(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Secondary,
                                checkedTrackColor = Secondary.copy(alpha = 0.3f),
                                uncheckedThumbColor = OnSurfaceVariant.copy(alpha = 0.4f),
                                uncheckedTrackColor = SurfaceContainerHigh
                            )
                        )
                    }
                }
            }

            // AI Personalization Blueprint Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "AI PLANNER PERSONALIZATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                    border = BorderStroke(1.dp, Secondary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Secondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = Secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Adaptive AI Blueprint",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (userProfile?.isOnboarded == true) "Calibrated to your routine" else "Not personalized yet",
                                    fontSize = 12.sp,
                                    color = Secondary
                                )
                            }
                        }

                        if (userProfile?.selectedInterests?.isNotBlank() == true) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Focus Areas:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = userProfile?.selectedInterests.orEmpty(),
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }

                        if (userProfile?.priorityStatement?.isNotBlank() == true) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Primary Target:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = userProfile?.priorityStatement.orEmpty(),
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.startPersonalizationAgain() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BaseDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Personalize My Planner Again",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BaseDark
                            )
                        }
                    }
                }
            }

            // Configuration Options (System resetting)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Secondary.copy(alpha = 0.08f))
                        .border(1.dp, Secondary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.startPersonalizationAgain() }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Run AI Setup",
                        color = Secondary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Error.copy(alpha = 0.08f))
                        .border(1.dp, Error.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.clearAllUserData()
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clear & Reset All",
                        color = Error,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

    // Google Sign In Account Selector Dialog
    if (showGoogleLoginDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSigningInGoogle) showGoogleLoginDialog = false },
            containerColor = SolidSurface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF4285F4)
                        )
                    }
                    Text("Sign in with Google", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Choose an account to synchronize with LifeOS:",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (isSigningInGoogle) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Tertiary, modifier = Modifier.size(36.dp))
                            Text(
                                text = "Securing OAuth credentials with Google...",
                                color = OnSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Option 1: Seif Hazem from ADDITIONAL_METADATA
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerHigh)
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        isSigningInGoogle = true
                                        delay(1500)
                                        if (startFreshChecked) {
                                            viewModel.clearAllUserData()
                                        }
                                        viewModel.linkGoogleAccount(customGoogleName, customGoogleEmail, null)
                                        isSigningInGoogle = false
                                        showGoogleLoginDialog = false
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Tertiary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customGoogleName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Tertiary
                                    )
                                }
                                Column {
                                    Text(customGoogleName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                                    Text(customGoogleEmail, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                                }
                            }
                        }

                        // Customize Google Info Card
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Or customize Google credentials:",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                            OutlinedTextField(
                                value = customGoogleName,
                                onValueChange = { customGoogleName = it },
                                label = { Text("Google Name") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface,
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = customGoogleEmail,
                                onValueChange = { customGoogleEmail = it },
                                label = { Text("Google Email") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface,
                                    focusedBorderColor = Secondary,
                                    unfocusedBorderColor = OutlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Start fresh option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable { startFreshChecked = !startFreshChecked }
                        ) {
                            Checkbox(
                                checked = startFreshChecked,
                                onCheckedChange = { startFreshChecked = it },
                                colors = CheckboxDefaults.colors(checkedColor = Tertiary)
                            )
                            Column {
                                Text(
                                    text = "Start Fresh",
                                    color = OnSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Purge all standard dummy tasks/goals.",
                                    color = OnSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                if (!isSigningInGoogle) {
                    TextButton(onClick = { showGoogleLoginDialog = false }) {
                        Text("CANCEL", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = SolidSurface,
            title = {
                Text("Customize Profile", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Profile Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editVibe,
                        onValueChange = { editVibe = it },
                        label = { Text("Current Mindset Vibe") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = OutlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editName.isNotBlank()) {
                            userProfile?.let { profile ->
                                viewModel.linkGoogleAccount(editName, profile.email ?: "", profile.photoUrl)
                                // We can use linkGoogleAccount to also edit name/email directly!
                            }
                        }
                        showEditProfileDialog = false
                    }
                ) {
                    Text("SAVE CHANGES", color = Secondary, style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("CANCEL", color = OnSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showImagePicker) {
        com.example.ui.components.ImagePickerDialog(
            title = "Change Profile Picture",
            currentImageUrl = userProfile?.photoUrl,
            onDismiss = { showImagePicker = false },
            onImageSelected = { newUrl ->
                viewModel.updateProfilePhoto(newUrl)
            }
        )
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            containerColor = SolidSurface,
            title = {
                Text("Reset All Data?", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
            },
            text = {
                Text(
                    "This action is irreversible and will reset your user profile, tasks, habits, and active milestones back to standard default settings.",
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

    if (showAlarmsSheet) {
        com.example.ui.components.RecurringAlarmsBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAlarmsSheet = false }
        )
    }

    if (showWorkScheduleDialog) {
        val current = userProfile ?: com.example.data.UserProfileEntity()
        com.example.ui.components.WorkScheduleDialog(
            currentProfile = current,
            onDismiss = { showWorkScheduleDialog = false },
            onSaveSchedule = { start, end, wfh, workDays, weekends, vacationMode, vStart, vEnd, vNotes ->
                viewModel.updateWorkSchedule(
                    workStartTime = start,
                    workEndTime = end,
                    wfhDays = wfh,
                    workDays = workDays,
                    weekendDays = weekends
                )
                viewModel.updateVacationSettings(
                    isVacationMode = vacationMode,
                    startDate = vStart,
                    endDate = vEnd,
                    notes = vNotes
                )
            }
        )
    }
}
