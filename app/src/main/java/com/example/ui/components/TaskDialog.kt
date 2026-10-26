package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TaskEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TaskAddEditBottomSheet displays a bottom sheet for adding a new task or editing an existing one.
 * Includes form validation, date & time pickers, priority chips, category chips, and reminder toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskAddEditBottomSheet(
    taskToEdit: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        priority: TaskPriority,
        category: String,
        dueDate: Long?,
        dueHour: Int?,
        dueMinute: Int?,
        hasReminder: Boolean
    ) -> Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form states initialized with taskToEdit if editing, or defaults if creating
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var priority by remember {
        mutableStateOf(
            if (taskToEdit != null) TaskPriority.fromString(taskToEdit.priority) else TaskPriority.MEDIUM
        )
    }
    var category by remember { mutableStateOf(taskToEdit?.category ?: "Personal") }
    var dueDate by remember { mutableStateOf<Long?>(taskToEdit?.dueDate) }
    var dueHour by remember { mutableStateOf<Int?>(taskToEdit?.dueTimeHour) }
    var dueMinute by remember { mutableStateOf<Int?>(taskToEdit?.dueTimeMinute) }
    var hasReminder by remember { mutableStateOf(taskToEdit?.hasReminder ?: false) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.testTag("task_add_edit_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (taskToEdit != null) "Edit Task" else "Create New Task",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_dialog_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close dialog"
                    )
                }
            }

            // Task Title Input with validation
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = null
                },
                label = { Text("Task Title *") },
                placeholder = { Text("e.g. Finish quarterly project proposal") },
                isError = titleError != null,
                supportingText = {
                    if (titleError != null) {
                        Text(
                            text = titleError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("${title.length}/80 characters")
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            // Task Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Add notes, checklists, or key details...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_description_input")
            )

            // Priority Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { p ->
                        val isSelected = priority == p
                        val pColor = p.getColor()
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { priority = p }
                                .testTag("priority_option_${p.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) pColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, pColor) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(pColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = p.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Category Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskCategory.entries.forEach { cat ->
                        val isSelected = category.equals(cat.displayName, ignoreCase = true)
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { category = cat.displayName },
                            label = { Text(cat.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else cat.color
                                )
                            },
                            modifier = Modifier.testTag("dialog_category_${cat.name.lowercase()}")
                        )
                    }
                }
            }

            // Due Date & Time Picker Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Deadline & Scheduling",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Button
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_date_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dueDate != null) {
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dueDate!!))
                            } else {
                                "Pick Date"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    // Time Button (active if date is selected)
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("select_time_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (dueHour != null && dueMinute != null) {
                                String.format(Locale.getDefault(), "%02d:%02d", dueHour, dueMinute)
                            } else {
                                "Pick Time"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    // Clear date button
                    if (dueDate != null) {
                        IconButton(
                            onClick = {
                                dueDate = null
                                dueHour = null
                                dueMinute = null
                                hasReminder = false
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear deadline",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Reminder Switch
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Reminder Notification",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (dueDate != null) "Alert when due date arrives" else "Select a due date first",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = hasReminder && dueDate != null,
                        onCheckedChange = { isChecked ->
                            if (dueDate == null && isChecked) {
                                // Default due date to today if user switches reminder on
                                dueDate = System.currentTimeMillis()
                                if (dueHour == null) {
                                    dueHour = 9
                                    dueMinute = 0
                                }
                            }
                            hasReminder = isChecked
                        },
                        modifier = Modifier.testTag("reminder_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dialog_cancel_button")
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = "Task title cannot be empty"
                            return@Button
                        }
                        val success = onSave(
                            title,
                            description,
                            priority,
                            category,
                            dueDate,
                            dueHour,
                            dueMinute,
                            hasReminder
                        )
                        if (!success) {
                            titleError = "Please enter a valid title"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dialog_save_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (taskToEdit != null) "Save Changes" else "Add Task")
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = dueHour ?: 9,
            initialMinute = dueMinute ?: 0,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueHour = timePickerState.hour
                        dueMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Due Time",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}
