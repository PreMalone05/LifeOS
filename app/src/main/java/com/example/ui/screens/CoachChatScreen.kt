package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachChatScreen(viewModel: LifeViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isSending by viewModel.isSendingChatMessage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val coachName = userProfile?.coachPersonality ?: "Stoic Mentor"
    val userName = userProfile?.name ?: "Alex"

    var textInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Suggestions to make the chat experience interactive and immediate
    val suggestionChips = listOf(
        "Review today's schedule",
        "Give me a focus mantra",
        "How can I level up faster?",
        "Help with procrastination"
    )

    // Automatically scroll to bottom when a new message arrives
    LaunchedEffect(messages.size, isSending) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = coachName.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = OnSurface
                        )
                        Text(
                            text = "AI Productivity Companion",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo("TODAY") },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Today",
                            tint = OnSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.testTag("chat_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat History",
                            tint = Error.copy(alpha = 0.9f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BaseDark.copy(alpha = 0.8f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(), // Ensure keyboard moves input field
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Scrollable Message Board
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            // Left-aligned message bubble (Coach)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                                    .background(SurfaceContainer)
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = coachName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = Secondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 20.sp
                                    ),
                                    color = OnSurface
                                )
                            }
                        } else {
                            // Right-aligned message bubble (User)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 0.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                SecondaryContainer.copy(alpha = 0.8f),
                                                TertiaryContainer.copy(alpha = 0.8f)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Secondary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 0.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = userName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = PrimaryFixedDim,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 20.sp
                                    ),
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }

                // Thinking / Typing Indicator
                if (isSending) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                                    .background(SurfaceContainer)
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
                                    )
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = Secondary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "$coachName is formulating strategy...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2. Interactive Suggestion Row & Text Input Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BaseDark.copy(alpha = 0.9f))
                    .padding(vertical = 12.dp)
            ) {
                // Horizontal chips scroll row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestionChips.forEach { chipText ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLow)
                                .clickable {
                                    if (!isSending) {
                                        viewModel.sendChatMessage(chipText)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = chipText,
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Consult with $coachName...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerHigh,
                            unfocusedContainerColor = SurfaceContainer,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4
                    )

                    FilledIconButton(
                        onClick = {
                            if (textInput.isNotBlank() && !isSending) {
                                viewModel.sendChatMessage(textInput)
                                textInput = ""
                                keyboardController?.hide()
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Secondary,
                            contentColor = OnSecondary,
                            disabledContainerColor = SurfaceContainer,
                            disabledContentColor = OnSurfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button"),
                        enabled = textInput.isNotBlank() && !isSending
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
