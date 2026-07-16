package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Background
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.Secondary
import com.example.ui.theme.OnSurfaceVariant
import com.example.viewmodel.LifeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: LifeViewModel = viewModel()
                LifeOSAppShell(viewModel)
            }
        }
    }
}

@Composable
fun LifeOSAppShell(viewModel: LifeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    // 5 primary tabs of LifeOS bottom navigation
    val primaryTabs = listOf("TODAY", "PLANNER", "HABITS", "INSIGHTS", "PROFILE")
    val showBottomBar = currentScreen in primaryTabs

    Scaffold(
        modifier = Modifier.fillMaxSize().meshBackground(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SurfaceContainer,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    // Today Tab
                    NavigationBarItem(
                        selected = currentScreen == "TODAY",
                        onClick = { viewModel.navigateTo("TODAY") },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Today") },
                        label = { Text("Today", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Secondary,
                            selectedTextColor = Secondary,
                            indicatorColor = Color.White.copy(alpha = 0.05f),
                            unselectedIconColor = OnSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = OnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )

                    // Planner Tab
                    NavigationBarItem(
                        selected = currentScreen == "PLANNER",
                        onClick = { viewModel.navigateTo("PLANNER") },
                        icon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = "Planner") },
                        label = { Text("Planner", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Secondary,
                            selectedTextColor = Secondary,
                            indicatorColor = Color.White.copy(alpha = 0.05f),
                            unselectedIconColor = OnSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = OnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )

                    // Habits Tab
                    NavigationBarItem(
                        selected = currentScreen == "HABITS",
                        onClick = { viewModel.navigateTo("HABITS") },
                        icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Habits") },
                        label = { Text("Habits", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Secondary,
                            selectedTextColor = Secondary,
                            indicatorColor = Color.White.copy(alpha = 0.05f),
                            unselectedIconColor = OnSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = OnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )

                    // Insights Tab
                    NavigationBarItem(
                        selected = currentScreen == "INSIGHTS",
                        onClick = { viewModel.navigateTo("INSIGHTS") },
                        icon = { Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Insights") },
                        label = { Text("Insights", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Secondary,
                            selectedTextColor = Secondary,
                            indicatorColor = Color.White.copy(alpha = 0.05f),
                            unselectedIconColor = OnSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = OnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )

                    // Profile Tab
                    NavigationBarItem(
                        selected = currentScreen == "PROFILE",
                        onClick = { viewModel.navigateTo("PROFILE") },
                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Secondary,
                            selectedTextColor = Secondary,
                            indicatorColor = Color.White.copy(alpha = 0.05f),
                            unselectedIconColor = OnSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = OnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        containerColor = Background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) 0.dp else 0.dp) // Handled padding inside screens cleanly
        ) {
            when (currentScreen) {
                "TODAY" -> TodayScreen(viewModel)
                "PLANNER" -> PlannerScreen(viewModel)
                "HABITS" -> HabitsScreen(viewModel)
                "INSIGHTS" -> InsightsScreen(viewModel)
                "PROFILE" -> ProfileScreen(viewModel)
                "COACH_TUNING" -> TuningScreen(viewModel)
                "DEFINE_GOAL" -> DefineGoalScreen(viewModel)
                "MILESTONE_PLAN" -> MilestonePlanScreen(viewModel)
                "MILESTONE_CHECKIN" -> MilestoneCheckInScreen(viewModel)
                "MISSION_ACCOMPLISHED" -> CelebrationScreen(viewModel)
                else -> TodayScreen(viewModel)
            }
        }
    }
}

fun Modifier.meshBackground(): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height

    // Solid dark base
    drawRect(color = Color(0xFF0F1115))

    // Radial gradient at 0% 0% (#2E1065)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF2E1065), Color.Transparent),
            center = Offset(0f, 0f),
            radius = width * 1.2f
        )
    )

    // Radial gradient at 100% 0% (#1E1B4B)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E1B4B), Color.Transparent),
            center = Offset(width, 0f),
            radius = width * 1.2f
        )
    )

    // Radial gradient at 100% 100% (#4C1D95)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF4C1D95), Color.Transparent),
            center = Offset(width, height),
            radius = width * 1.4f
        )
    )

    // Radial gradient at 0% 100% (#0F172A)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0F172A), Color.Transparent),
            center = Offset(0f, height),
            radius = width * 1.2f
        )
    )
}
