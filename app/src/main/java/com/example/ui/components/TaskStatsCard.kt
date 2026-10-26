package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TaskStatsCard displays a sleek productivity progress dashboard
 * showing completion percentage, finished task counts, and motivational feedback.
 */
@Composable
fun TaskStatsCard(
    totalTasks: Int,
    completedTasks: Int,
    completionPercentage: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = completionPercentage,
        animationSpec = tween(durationMillis = 600),
        label = "progressAnimation"
    )

    val pendingCount = (totalTasks - completedTasks).coerceAtLeast(0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_stats_card"),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (completedTasks == totalTasks && totalTasks > 0)
                                    Icons.Default.CheckCircle else Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Daily Progress",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (totalTasks == 0) {
                            "Ready to start your day?"
                        } else if (completedTasks == totalTasks) {
                            "All tasks completed! 🎉"
                        } else {
                            "$pendingCount tasks remaining"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "$completedTasks of $totalTasks finished (${(completionPercentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Circular Progress Indicator with centered percentage
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 6.dp,
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp,
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
