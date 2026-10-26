package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.TaskAddEditBottomSheet
import com.example.ui.components.TaskFilterChips
import com.example.ui.components.TaskItemCard
import com.example.ui.components.TaskStatsCard
import com.example.ui.viewmodel.TaskViewModel

/**
 * TaskListScreen is the primary screen displaying the user's tasks,
 * search input, status filters, statistics dashboard, and quick action FAB.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var isSearchActive by remember { mutableStateOf(false) }

    // Request POST_NOTIFICATIONS permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // React to user messages and deleted tasks with Undo Snackbar
    LaunchedEffect(uiState.userMessage, uiState.recentlyDeletedTask) {
        val message = uiState.userMessage
        if (message != null) {
            val actionLabel = if (uiState.recentlyDeletedTask != null) "Undo" else null
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndoDelete()
            }
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "TaskFlow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) {
                                viewModel.onSearchQueryChanged("")
                            }
                        },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Close Search" else "Search Tasks",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddTaskDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new task"
                    )
                },
                text = { Text("New Task") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_task_fab")
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Expandable Search Bar
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search by title or description...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_input_field")
                )
            }

            // Task List + Dashboard Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("task_list_lazy_column"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp // Generous bottom padding for FAB clearance
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Productivity stats header card
                item(key = "stats_card") {
                    TaskStatsCard(
                        totalTasks = uiState.totalCount,
                        completedTasks = uiState.completedCount,
                        completionPercentage = uiState.completionPercentage
                    )
                }

                // Filter & Sort chips
                item(key = "filters_row") {
                    TaskFilterChips(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.onCategorySelected(it) },
                        selectedStatus = uiState.selectedStatusFilter,
                        onStatusSelected = { viewModel.onStatusFilterSelected(it) },
                        selectedSortOrder = uiState.selectedSortOrder,
                        onSortOrderSelected = { viewModel.onSortOrderChanged(it) }
                    )
                }

                // Empty State or Task List
                if (uiState.filteredTasks.isEmpty()) {
                    item(key = "empty_state") {
                        EmptyTaskState(
                            hasSearchQuery = uiState.searchQuery.isNotBlank(),
                            onClearSearch = { viewModel.onSearchQueryChanged("") },
                            onAddTask = { viewModel.openAddTaskDialog() }
                        )
                    }
                } else {
                    items(
                        items = uiState.filteredTasks,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { viewModel.onToggleTaskCompletion(task) },
                            onClick = { viewModel.openEditTaskDialog(task) },
                            onDelete = { viewModel.onDeleteTask(task) }
                        )
                    }
                }
            }
        }

        // Add / Edit Task Modal Bottom Sheet
        if (uiState.isAddEditSheetOpen) {
            TaskAddEditBottomSheet(
                taskToEdit = uiState.taskToEdit,
                onDismiss = { viewModel.dismissAddEditDialog() },
                onSave = { title, desc, prio, cat, due, hour, min, reminder ->
                    viewModel.saveTask(
                        title = title,
                        description = desc,
                        priority = prio,
                        category = cat,
                        dueDate = due,
                        dueHour = hour,
                        dueMinute = min,
                        hasReminder = reminder
                    )
                }
            )
        }
    }
}

/**
 * Empty state component shown when no tasks match current filters.
 */
@Composable
private fun EmptyTaskState(
    hasSearchQuery: Boolean,
    onClearSearch: () -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("empty_task_state"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (hasSearchQuery) "No matching tasks found" else "All tasks done!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasSearchQuery) {
                    "Try checking your spelling or clear the search filter."
                } else {
                    "You have no tasks left. Enjoy your free time or add a new goal!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (hasSearchQuery) {
                Button(onClick = onClearSearch) {
                    Text("Clear Search")
                }
            } else {
                Button(
                    onClick = onAddTask,
                    modifier = Modifier.testTag("empty_state_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Create a Task")
                }
            }
        }
    }
}
