package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.notification.TaskAlarmScheduler
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Filter options for task completion status.
 */
enum class TaskFilterStatus(val displayName: String) {
    ALL("All"),
    PENDING("Active"),
    COMPLETED("Done")
}

/**
 * Sort options for ordering tasks.
 */
enum class TaskSortOrder(val displayName: String) {
    DUE_DATE("Due Date"),
    PRIORITY("Priority"),
    CREATED_AT("Recent"),
    TITLE("Title (A-Z)")
}

/**
 * Immutable UI State representing current tasks, filters, and dialog state.
 */
data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val filteredTasks: List<TaskEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val selectedStatusFilter: TaskFilterStatus = TaskFilterStatus.ALL,
    val selectedSortOrder: TaskSortOrder = TaskSortOrder.DUE_DATE,
    val isAddEditSheetOpen: Boolean = false,
    val taskToEdit: TaskEntity? = null,
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val userMessage: String? = null,
    val recentlyDeletedTask: TaskEntity? = null
) {
    val completionPercentage: Float
        get() = if (totalCount == 0) 0f else (completedCount.toFloat() / totalCount.toFloat())
}

/**
 * ViewModel managing the business logic, state transformations, and Room queries for TaskFlow.
 */
class TaskViewModel(
    private val repository: TaskRepository,
    private val alarmScheduler: TaskAlarmScheduler
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _selectedStatusFilter = MutableStateFlow(TaskFilterStatus.ALL)
    private val _selectedSortOrder = MutableStateFlow(TaskSortOrder.DUE_DATE)
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    private val _taskToEdit = MutableStateFlow<TaskEntity?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _recentlyDeletedTask = MutableStateFlow<TaskEntity?>(null)

    // Combine raw Room database tasks with UI filters and sort logic reactively
    val uiState: StateFlow<TaskUiState> = combine(
        repository.allTasks,
        _searchQuery,
        _selectedCategory,
        _selectedStatusFilter,
        _selectedSortOrder
    ) { tasks, query, category, statusFilter, sortOrder ->
        // 1. Filter by search query
        val searched = if (query.isBlank()) {
            tasks
        } else {
            val q = query.trim().lowercase()
            tasks.filter {
                it.title.lowercase().contains(q) || it.description.lowercase().contains(q)
            }
        }

        // 2. Filter by category
        val categoryFiltered = if (category == "All") {
            searched
        } else {
            searched.filter { it.category.equals(category, ignoreCase = true) }
        }

        // 3. Filter by status (All, Pending, Completed)
        val statusFiltered = when (statusFilter) {
            TaskFilterStatus.ALL -> categoryFiltered
            TaskFilterStatus.PENDING -> categoryFiltered.filter { !it.isCompleted }
            TaskFilterStatus.COMPLETED -> categoryFiltered.filter { it.isCompleted }
        }

        // 4. Sort tasks
        val sorted = when (sortOrder) {
            TaskSortOrder.DUE_DATE -> statusFiltered.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { it.dueDate ?: Long.MAX_VALUE }
                    .thenByDescending { it.createdAt }
            )
            TaskSortOrder.PRIORITY -> statusFiltered.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenByDescending { TaskPriority.fromString(it.priority).weight }
                    .thenByDescending { it.createdAt }
            )
            TaskSortOrder.CREATED_AT -> statusFiltered.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenByDescending { it.createdAt }
            )
            TaskSortOrder.TITLE -> statusFiltered.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            )
        }

        val total = tasks.size
        val completed = tasks.count { it.isCompleted }

        TaskUiState(
            tasks = tasks,
            filteredTasks = sorted,
            searchQuery = query,
            selectedCategory = category,
            selectedStatusFilter = statusFilter,
            selectedSortOrder = sortOrder,
            isAddEditSheetOpen = _isAddEditSheetOpen.value,
            taskToEdit = _taskToEdit.value,
            totalCount = total,
            completedCount = completed,
            userMessage = _userMessage.value,
            recentlyDeletedTask = _recentlyDeletedTask.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onStatusFilterSelected(status: TaskFilterStatus) {
        _selectedStatusFilter.value = status
    }

    fun onSortOrderChanged(sortOrder: TaskSortOrder) {
        _selectedSortOrder.value = sortOrder
    }

    fun openAddTaskDialog() {
        _taskToEdit.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditTaskDialog(task: TaskEntity) {
        _taskToEdit.value = task
        _isAddEditSheetOpen.value = true
    }

    fun dismissAddEditDialog() {
        _isAddEditSheetOpen.value = false
        _taskToEdit.value = null
    }

    /**
     * Toggles the completion status of a task and manages alarm reminders.
     */
    fun onToggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.toggleTaskCompletion(task)

            if (updatedTask.isCompleted) {
                // Cancel reminder when task is completed
                alarmScheduler.cancel(task)
                _userMessage.value = "Completed: ${task.title}"
            } else {
                // Re-schedule reminder if uncompleted
                if (updatedTask.hasReminder) {
                    alarmScheduler.schedule(updatedTask)
                }
                _userMessage.value = "Active: ${task.title}"
            }
        }
    }

    /**
     * Deletes a task from the database, cancels any scheduled alarm,
     * and preserves the item for possible Undo action.
     */
    fun onDeleteTask(task: TaskEntity) {
        viewModelScope.launch {
            _recentlyDeletedTask.value = task
            alarmScheduler.cancel(task)
            repository.deleteTask(task)
            _userMessage.value = "Deleted: ${task.title}"
        }
    }

    /**
     * Restores the most recently deleted task.
     */
    fun onUndoDelete() {
        val taskToRestore = _recentlyDeletedTask.value ?: return
        viewModelScope.launch {
            val restoredId = repository.insertTask(taskToRestore)
            val restoredTask = taskToRestore.copy(id = restoredId)
            if (restoredTask.hasReminder && !restoredTask.isCompleted) {
                alarmScheduler.schedule(restoredTask)
            }
            _recentlyDeletedTask.value = null
            _userMessage.value = "Restored: ${restoredTask.title}"
        }
    }

    /**
     * Validates input fields and saves (inserts or updates) a task.
     * Returns true if successful, false if validation fails.
     */
    fun saveTask(
        title: String,
        description: String,
        priority: TaskPriority,
        category: String,
        dueDate: Long?,
        dueHour: Int?,
        dueMinute: Int?,
        hasReminder: Boolean
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            _userMessage.value = "Please enter a task title"
            return false
        }

        viewModelScope.launch {
            val currentEditing = _taskToEdit.value
            if (currentEditing != null) {
                // Update existing task
                val updated = currentEditing.copy(
                    title = cleanTitle,
                    description = description.trim(),
                    priority = priority.name,
                    category = category.trim(),
                    dueDate = dueDate,
                    dueTimeHour = dueHour,
                    dueTimeMinute = dueMinute,
                    hasReminder = hasReminder
                )
                repository.updateTask(updated)
                if (hasReminder && !updated.isCompleted) {
                    alarmScheduler.schedule(updated)
                } else {
                    alarmScheduler.cancel(updated)
                }
                _userMessage.value = "Task updated successfully"
            } else {
                // Insert new task
                val newTask = TaskEntity(
                    title = cleanTitle,
                    description = description.trim(),
                    priority = priority.name,
                    category = category.trim(),
                    dueDate = dueDate,
                    dueTimeHour = dueHour,
                    dueTimeMinute = dueMinute,
                    hasReminder = hasReminder
                )
                val newId = repository.insertTask(newTask)
                val savedTask = newTask.copy(id = newId)
                if (hasReminder) {
                    alarmScheduler.schedule(savedTask)
                }
                _userMessage.value = "Task created successfully"
            }
            dismissAddEditDialog()
        }
        return true
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
