package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.database.AppDatabase
import com.example.data.notification.TaskAlarmScheduler
import com.example.data.repository.TaskRepositoryImpl
import com.example.ui.screens.TaskListScreen
import com.example.ui.theme.TaskFlowTheme
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.TaskViewModelFactory

/**
 * MainActivity serves as the entry point of the TaskFlow application.
 * Follows Android MVVM architecture, initializes Room Database, Repository,
 * and sets up modern Jetpack Compose UI with edge-to-edge support.
 */
class MainActivity : ComponentActivity() {

    // ViewModel injection using ViewModelProvider.Factory
    private val taskViewModel: TaskViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepositoryImpl(database.taskDao())
        val alarmScheduler = TaskAlarmScheduler(applicationContext)
        TaskViewModelFactory(repository, alarmScheduler)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display for modern Android WindowInsets handling
        enableEdgeToEdge()

        setContent {
            TaskFlowTheme {
                TaskListScreen(viewModel = taskViewModel)
            }
        }
    }
}
