package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdaptiveCapacityReport
import com.example.ui.theme.*

@Composable
fun AdaptiveCapacityCard(
    capacityReport: AdaptiveCapacityReport?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onOpenRebalance: () -> Unit,
    onOpenEveningReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        Secondary.copy(alpha = 0.35f),
                        Tertiary.copy(alpha = 0.35f)
                    )
                ),
                RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Secondary.copy(alpha = 0.2f), Tertiary.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Capacity Engine",
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ADAPTIVE CAPACITY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.1.sp
                            ),
                            color = OnSurface
                        )
                        val status = capacityReport?.capacityStatus ?: "OPTIMAL"
                        val (statusText, statusColor) = when (status) {
                            "OVERCOMMITTED" -> "Overcommitted (+Load)" to Error
                            "HEAVY" -> "Heavy Focus Load" to Warning
                            "LIGHT" -> "Light / Recovery" to Tertiary
                            else -> "Optimal Capacity" to Color(0xFF4CAF50)
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = statusColor
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Secondary, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Recalculate Capacity",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Planned vs Available Progress Bar
            val plannedHours = capacityReport?.plannedHours ?: 4.0f
            val availableHours = capacityReport?.availableHours ?: 6.0f
            val ratio = if (availableHours > 0) (plannedHours / availableHours).coerceIn(0f, 1.5f) else 0.5f

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Planned Load: ${String.format("%.1f", plannedHours)}h",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurface
                    )
                    Text(
                        text = "Focus Window: ${String.format("%.1f", availableHours)}h",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { (ratio / 1.5f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (ratio > 1.0f) Error else Secondary,
                    trackColor = SurfaceContainerHighest
                )
            }

            // Reality Insight & Evidence Tags
            capacityReport?.realityInsight?.takeIf { it.isNotBlank() }?.let { insight ->
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface
                )
            }

            // Transparent Observation Chips
            if (!capacityReport?.transparentObservations.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    capacityReport!!.transparentObservations.take(2).forEach { obs ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerHigh)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Observed",
                                tint = Tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = obs,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action Buttons: Rebalance & Evening Review
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenRebalance,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SecondaryContainer,
                        contentColor = OnSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Rebalance",
                            modifier = Modifier.size(16.dp)
                        )
                        Text("REBALANCE DAY", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                OutlinedButton(
                    onClick = onOpenEveningReview,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nightlight,
                            contentDescription = "Review",
                            modifier = Modifier.size(16.dp),
                            tint = Tertiary
                        )
                        Text("EVENING REVIEW", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
