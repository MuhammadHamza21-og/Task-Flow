package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.notification.TaskAlarmScheduler
import com.example.data.repository.TaskRepository

/**
 * Factory class for instantiating TaskViewModel with its required dependencies.
 */
class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val alarmScheduler: TaskAlarmScheduler
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
