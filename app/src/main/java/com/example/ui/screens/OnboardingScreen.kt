package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.LifeViewModel
import com.example.viewmodel.OnboardingStep

@Composable
fun OnboardingScreen(viewModel: LifeViewModel) {
    val onboardingStep by viewModel.onboardingStep.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = onboardingStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "OnboardingStepTransition"
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep(viewModel)
                OnboardingStep.GOOGLE_SIGN_IN -> GoogleSignInStep(viewModel)
                OnboardingStep.INTERESTS -> InterestsStep(viewModel)
                OnboardingStep.AI_INTERVIEW -> AIInterviewStep(viewModel)
                OnboardingStep.REVIEW_CONFIRM -> ReviewConfirmStep(viewModel)
            }
        }
    }
}

// --- Step 1: Welcome ---
@Composable
fun WelcomeStep(viewModel: LifeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            // Glowing AI Badge
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Secondary.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .border(2.dp, Secondary.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LifeOS",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The AI Planner Built Around You",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No generic dummy templates. Experience a truly adaptive daily operating system customized through a dynamic AI interview.",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Feature Highlights
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                WelcomeFeatureCard(
                    icon = Icons.Default.PersonSearch,
                    title = "100% Personalized to Your Life",
                    desc = "Starts completely clean — calibrated directly to your work, studies, goals, and routine."
                )
                WelcomeFeatureCard(
                    icon = Icons.Default.Psychology,
                    title = "Adaptive AI Interview",
                    desc = "Answers are analyzed dynamically by Gemini to tailor your daily schedule and habits."
                )
                WelcomeFeatureCard(
                    icon = Icons.Default.VerifiedUser,
                    title = "Google Account Sync",
                    desc = "Seamless sign-in with your Google identity to preserve your preferences."
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.setOnboardingStep(OnboardingStep.GOOGLE_SIGN_IN) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BaseDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BaseDark
                )
            }
        }
    }
}

@Composable
fun WelcomeFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Secondary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontSize = 12.sp, color = OnSurfaceVariant, lineHeight = 16.sp)
            }
        }
    }
}

// --- Step 2: Google Sign-In ---
@Composable
fun GoogleSignInStep(viewModel: LifeViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val isSigningIn by viewModel.isSigningInWithGoogle.collectAsState()
    val signInError by viewModel.googleSignInError.collectAsState()

    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            IconButton(
                onClick = { viewModel.setOnboardingStep(OnboardingStep.WELCOME) },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Connect Your Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in with Google to personalize your AI profile, sync preferences, and secure your schedule.",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // If already linked or logged in
            if (userProfile?.isGoogleLinked == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                    border = BorderStroke(1.dp, Secondary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Connected Successfully!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userProfile?.name.orEmpty().ifBlank { "Google User" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Secondary
                        )
                        if (!userProfile?.email.isNullOrBlank()) {
                            Text(
                                text = userProfile?.email.orEmpty(),
                                fontSize = 13.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Google One-Tap / Credential Manager Button
                Button(
                    onClick = { viewModel.performGoogleSignIn(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    enabled = !isSigningIn
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF1F2937),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    }
                }

                if (signInError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notice: Credential prompt fallback available. You can also customize your name directly below.",
                        fontSize = 12.sp,
                        color = Color(0xFFF59E0B),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Name / Profile Entry
                OutlinedButton(
                    onClick = { showManualEntry = !showManualEntry },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = if (showManualEntry) "Hide Custom Name Entry" else "Or Enter Name Manually",
                        color = OnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                if (showManualEntry) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Your Display Name") },
                        placeholder = { Text("e.g. Alex Rivera") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Email (Optional)") },
                        placeholder = { Text("alex@example.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (customName.isNotBlank()) {
                                viewModel.setManualGoogleUser(customName.trim(), customEmail.trim(), null)
                                Toast.makeText(context, "Profile name saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = customName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary.copy(alpha = 0.85f))
                    ) {
                        Text("Save Profile Name", color = BaseDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            Button(
                onClick = { viewModel.setOnboardingStep(OnboardingStep.INTERESTS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Text(
                    text = "Continue to Focus Areas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BaseDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BaseDark
                )
            }
        }
    }
}

// --- Step 3: Interests & Intended-Use Selection ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsStep(viewModel: LifeViewModel) {
    val selectedInterests by viewModel.selectedOnboardingInterests.collectAsState()
    val customInput by viewModel.customInterestInput.collectAsState()

    val standardInterests = listOf(
        "Work & Career 💼",
        "Studying & Academics 📚",
        "Fitness & Conditioning 🏋️",
        "Health & Nutrition 🥗",
        "Personal Growth 🧠",
        "Habit Building ⚡",
        "Side Projects 🚀",
        "Entrepreneurship 🏢",
        "Finance & Wealth 💰",
        "Family & Home 🏡",
        "Social Life 👥",
        "Travel & Adventure ✈️",
        "Creative Arts ✍️",
        "Reading & Learning 📖",
        "Time Management ⏳",
        "Daily Organization 📋"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.setOnboardingStep(OnboardingStep.GOOGLE_SIGN_IN) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Badge(containerColor = Secondary.copy(alpha = 0.15f)) {
                    Text(
                        text = "${selectedInterests.size} Selected",
                        color = Secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "What will you use LifeOS for?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Select all areas you want to manage. Our AI will craft personalized interview questions based on your selections.",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Interest Chips FlowRow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                standardInterests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleOnboardingInterest(interest) },
                        label = {
                            Text(
                                text = interest,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Secondary,
                            selectedLabelColor = BaseDark,
                            containerColor = SurfaceContainer,
                            labelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Secondary else Color.White.copy(alpha = 0.1f)
                        )
                    )
                }

                // Custom added chips
                selectedInterests.filterNot { standardInterests.contains(it) }.forEach { customItem ->
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.removeCustomInterest(customItem) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = customItem,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Secondary,
                            selectedLabelColor = BaseDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add Custom Focus Area input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { viewModel.setCustomInterestInput(it) },
                    placeholder = { Text("Add custom focus (e.g. Marathon Prep)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.addCustomInterest() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (customInput.isNotBlank()) Secondary else Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = if (customInput.isNotBlank()) BaseDark else OnSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 16.dp)
        ) {
            Button(
                onClick = { viewModel.startAiInterview() },
                enabled = selectedInterests.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    disabledContainerColor = SurfaceContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (selectedInterests.isNotEmpty()) BaseDark else OnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start AI Interview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedInterests.isNotEmpty()) BaseDark else OnSurfaceVariant
                )
            }
        }
    }
}

// --- Step 4: Adaptive AI Interview ---
@Composable
fun AIInterviewStep(viewModel: LifeViewModel) {
    val currentQuestion by viewModel.currentAdaptiveQuestion.collectAsState()
    val isThinking by viewModel.isThinkingInterview.collectAsState()
    val interviewHistory by viewModel.interviewHistoryList.collectAsState()
    val interviewError by viewModel.interviewError.collectAsState()

    var selectedOption by remember(currentQuestion) { mutableStateOf<String?>(null) }
    var customAnswer by remember(currentQuestion) { mutableStateOf("") }

    val questionIndex = (currentQuestion?.questionIndex ?: (interviewHistory.size + 1))
    val totalEstimated = currentQuestion?.totalEstimatedQuestions ?: 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            // Header Row: Back button & Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.goToPreviousInterviewQuestion() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "Question $questionIndex of $totalEstimated",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { (questionIndex.toFloat() / totalEstimated.toFloat()).coerceIn(0.1f, 1.0f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Secondary,
                trackColor = Color.White.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isThinking) {
                // AI Thinking animation state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Secondary,
                            modifier = Modifier.size(44.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (questionIndex >= totalEstimated && interviewHistory.size >= 2) {
                                "Synthesizing your personalized daily blueprint..."
                            } else {
                                "LifeOS AI is generating the next tailored question..."
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Analyzing your specific commitments & rhythm",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (currentQuestion != null) {
                val q = currentQuestion!!

                // Topic Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Secondary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Secondary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = q.contextTopic.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Question Text
                Text(
                    text = q.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Multiple Choice Options
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    q.options.forEach { option ->
                        val isChosen = selectedOption == option && customAnswer.isBlank()
                        Card(
                            onClick = {
                                selectedOption = option
                                customAnswer = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChosen) Secondary.copy(alpha = 0.15f) else SurfaceContainer
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isChosen) Secondary else Color.White.copy(alpha = 0.06f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isChosen) Secondary else Color.Transparent)
                                        .border(
                                            2.dp,
                                            if (isChosen) Secondary else Color.White.copy(alpha = 0.3f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BaseDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
                                    fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isChosen) Color.White else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Optional Custom Answer Input
                OutlinedTextField(
                    value = customAnswer,
                    onValueChange = {
                        customAnswer = it
                        if (it.isNotBlank()) selectedOption = null
                    },
                    placeholder = { Text("Or enter your specific answer here...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            } else if (interviewError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Failed to load question. Please try again.",
                            color = Color(0xFFEF4444),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.startAiInterview() },
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                        ) {
                            Text("Retry Interview", color = BaseDark)
                        }
                    }
                }
            }
        }

        // Action Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            val hasAnswer = (selectedOption != null && selectedOption!!.isNotBlank()) || customAnswer.isNotBlank()
            val finalAnswer = if (customAnswer.isNotBlank()) customAnswer.trim() else selectedOption.orEmpty()

            Button(
                onClick = {
                    if (hasAnswer) {
                        viewModel.submitInterviewAnswer(finalAnswer)
                    }
                },
                enabled = hasAnswer && !isThinking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    disabledContainerColor = SurfaceContainer
                )
            ) {
                Text(
                    text = if (currentQuestion?.isFinalQuestion == true || questionIndex >= totalEstimated) {
                        "Complete Interview ✨"
                    } else {
                        "Next Question"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasAnswer) BaseDark else OnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (hasAnswer) BaseDark else OnSurfaceVariant
                )
            }
        }
    }
}

// --- Step 5: Review & Confirm ---
@Composable
fun ReviewConfirmStep(viewModel: LifeViewModel) {
    val config by viewModel.generatedPlannerConfig.collectAsState()
    val isApplying by viewModel.isApplyingPlan.collectAsState()
    val selectedInterests by viewModel.selectedOnboardingInterests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            // Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.goToPreviousInterviewQuestion() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "PLAN READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Personalized Blueprint",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Review what we understood from your interview before we construct your daily planner.",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 1: User Confirmed Facts
            Text(
                text = "USER CONFIRMED PROFILE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Secondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FactItem(
                        label = "Focus Areas",
                        value = selectedInterests.joinToString(", ")
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    FactItem(
                        label = "Primary Objective",
                        value = config?.topPriority ?: "Build daily consistency"
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    FactItem(
                        label = "Planning Structure",
                        value = config?.planningStyle ?: "Time Blocking + Tasks"
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    FactItem(
                        label = "Rhythm & Constraints",
                        value = config?.scheduleConstraints ?: "Standard work & study routine"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: AI Recommendations (Selectable)
            Text(
                text = "AI RECOMMENDED STARTER HABITS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Secondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            config?.suggestedStarterHabits?.forEachIndexed { index, habit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (habit.isSelected) Secondary.copy(alpha = 0.08f) else SurfaceContainer
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (habit.isSelected) Secondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = habit.isSelected,
                            onCheckedChange = { viewModel.toggleSuggestedHabit(index) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Secondary,
                                checkmarkColor = BaseDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = habit.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Target: ${habit.targetValue.toInt()} ${habit.unit} daily",
                                fontSize = 12.sp,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section 3: AI Starter Goal
            config?.suggestedStarterGoal?.let { goal ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AI RECOMMENDED STARTER VISION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (goal.isSelected) Secondary.copy(alpha = 0.08f) else SurfaceContainer
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (goal.isSelected) Secondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = goal.isSelected,
                            onCheckedChange = { viewModel.toggleSuggestedGoal() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Secondary,
                                checkmarkColor = BaseDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Phase 1: ${goal.firstMilestoneTitle}",
                                fontSize = 12.sp,
                                color = Secondary
                            )
                            Text(
                                text = goal.firstMilestoneDesc,
                                fontSize = 11.sp,
                                color = OnSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.applyPersonalizedPlan() },
                enabled = !isApplying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = BaseDark,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = BaseDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Build My Planner 🚀",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseDark
                    )
                }
            }

            TextButton(
                onClick = { viewModel.setOnboardingStep(OnboardingStep.AI_INTERVIEW) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Edit Interview Answers",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FactItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
