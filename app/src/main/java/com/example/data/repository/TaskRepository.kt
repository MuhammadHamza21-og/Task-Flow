package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining data operations for Tasks.
 */
interface TaskRepository {
    val allTasks: Flow<List<TaskEntity>>
    fun getTaskById(id: Long): Flow<TaskEntity?>
    suspend fun getTaskByIdSync(id: Long): TaskEntity?
    suspend fun insertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    suspend fun deleteTaskById(id: Long)
    suspend fun toggleTaskCompletion(task: TaskEntity)
}

/**
 * Concrete implementation of TaskRepository delegating to Room TaskDao.
 */
class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    override fun getTaskById(id: Long): Flow<TaskEntity?> {
        return taskDao.getTaskById(id)
    }

    override suspend fun getTaskByIdSync(id: Long): TaskEntity? {
        return taskDao.getTaskByIdSync(id)
    }

    override suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    override suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun toggleTaskCompletion(task: TaskEntity) {
        val newStatus = !task.isCompleted
        taskDao.updateCompletionStatus(
            id = task.id,
            completed = newStatus,
            updatedAt = System.currentTimeMillis()
        )
    }
}
