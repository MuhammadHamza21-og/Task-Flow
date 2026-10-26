package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * TaskItemCard renders a single task row with completion toggle,
 * priority pills, categorization, due date alerts, and quick actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priority = TaskPriority.fromString(task.priority)
    val categoryIcon = TaskCategory.getIconForCategory(task.category)
    val categoryColor = TaskCategory.getColorForCategory(task.category)

    val cardBgColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 300),
        label = "cardBgColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isCompleted) 0.dp else 2.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (task.isCompleted) {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            } else {
                priority.getColor().copy(alpha = 0.25f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Interactive Checkbox with >=48dp touch target
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(onClick = onToggleComplete)
                        .testTag("task_checkbox_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Mark task incomplete",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = "Mark task complete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title and Description
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("task_delete_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips: Priority, Category, Due Date, Reminder
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Priority Chip
                PriorityBadge(priority = priority)

                // Category Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = categoryColor
                        )
                    }
                }

                // Due Date Chip (if set)
                task.dueDate?.let { dueDateMillis ->
                    val dueDateStatus = formatDueDate(
                        dueDateMillis = dueDateMillis,
                        dueHour = task.dueTimeHour,
                        dueMinute = task.dueTimeMinute
                    )

                    val isOverdue = !task.isCompleted && isTaskOverdue(dueDateMillis, task.dueTimeHour, task.dueTimeMinute)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOverdue) {
                            PriorityHigh.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isOverdue) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = dueDateStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOverdue) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Reminder Bell Chip (if active)
                if (task.hasReminder) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder scheduled",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val bgColor = priority.getColor().copy(alpha = 0.15f)
    val textColor = priority.getColor()

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = "${priority.displayName} Priority",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

/**
 * Checks if a task's due date and time is in the past compared to current system time.
 */
private fun isTaskOverdue(dueDateMillis: Long, dueHour: Int?, dueMinute: Int?): Boolean {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dueDateMillis
        set(Calendar.HOUR_OF_DAY, dueHour ?: 23)
        set(Calendar.MINUTE, dueMinute ?: 59)
        set(Calendar.SECOND, 59)
    }
    return calendar.timeInMillis < System.currentTimeMillis()
}

/**
 * Formats due date timestamp into a readable human string ("Today", "Tomorrow", "Oct 14, 3:00 PM").
 */
private fun formatDueDate(dueDateMillis: Long, dueHour: Int?, dueMinute: Int?): String {
    val taskCal = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
    val nowCal = Calendar.getInstance()

    val isSameDay = taskCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            taskCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    nowCal.add(Calendar.DAY_OF_YEAR, 1)
    val isTomorrow = taskCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            taskCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)

    val timeString = if (dueHour != null && dueMinute != null) {
        String.format(Locale.getDefault(), " %02d:%02d", dueHour, dueMinute)
    } else ""

    return when {
        isSameDay -> "Today$timeString"
        isTomorrow -> "Tomorrow$timeString"
        else -> {
            val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
            "${formatter.format(Date(dueDateMillis))}$timeString"
        }
    }
}
