package com.example.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LifeViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefineGoalScreen(viewModel: LifeViewModel) {
    var goalTitle by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf("Career") }
    var selectedHorizon by remember { mutableStateOf("Quarterly") }
    var customImageUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Grand Vision",
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
                    text = "VISION ARCHITECTURE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Secondary
                )
                Text(
                    text = "Map Your Horizon",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                    color = OnSurface
                )
                Text(
                    text = "Commit to a specific grand vision. LifeOS will architect the milestones, intervals, and weekly check-in checklists.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            // Title input
            OutlinedTextField(
                value = goalTitle,
                onValueChange = { goalTitle = it },
                label = { Text("What is your grand commitment?", color = OnSurfaceVariant) },
                placeholder = { Text("e.g., Buy a Motorcycle, Learn System Design", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = OutlineVariant,
                    focusedLabelColor = Secondary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Domain Row Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SELECT DOMAIN",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Career", "Health", "Wealth", "Growth").forEach { domain ->
                        val isSelected = selectedDomain == domain
                        val domainColor = when (domain) {
                            "Career" -> Secondary
                            "Health" -> Tertiary
                            "Wealth" -> Error
                            else -> Primary
                        }

                        Box(
                            modifier = Modifier
                                .width(112.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) domainColor.copy(alpha = 0.15f) else SurfaceContainer)
                                .border(
                                    1.dp,
                                    if (isSelected) domainColor else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedDomain = domain }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (domain) {
                                        "Career" -> Icons.Default.Architecture
                                        "Health" -> Icons.Default.FitnessCenter
                                        "Wealth" -> Icons.Default.Payments
                                        else -> Icons.Default.MenuBook
                                    },
                                    contentDescription = domain,
                                    tint = if (isSelected) domainColor else OnSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = domain,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) OnSurface else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Horizon Selector
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "HORIZON RANGE",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Monthly", "Quarterly", "Yearly").forEach { horizon ->
                        val isSelected = selectedHorizon == horizon
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SecondaryContainer else SurfaceContainer)
                                .border(
                                    1.dp,
                                    if (isSelected) Secondary else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedHorizon = horizon }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = horizon,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) OnSecondaryContainer else OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Custom Image URL Input
            OutlinedTextField(
                value = customImageUrl,
                onValueChange = { customImageUrl = it },
                label = { Text("Vision Board Image URL (Optional)", color = OnSurfaceVariant) },
                placeholder = { Text("e.g., https://unsplash.com/photos/...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = OutlineVariant,
                    focusedLabelColor = Secondary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Submit Button
            Button(
                onClick = {
                    if (goalTitle.isNotBlank()) {
                        viewModel.createGoalFromVision(
                            title = goalTitle,
                            domain = selectedDomain,
                            horizon = selectedHorizon,
                            imageUrl = if (customImageUrl.isNotBlank()) customImageUrl else null
                        )
                    }
                },
                enabled = goalTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnSurface,
                    contentColor = BaseDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "ARCHITECT VISION TIMELINE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
