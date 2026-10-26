package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * TaskDao provides Data Access Object methods for querying,
 * inserting, updating, and deleting tasks in the SQLite Room database.
 */
@Dao
interface TaskDao {

    /**
     * Observes all tasks ordered by completion status (active first) and creation date descending.
     */
    @Query("SELECT * FROM tasks ORDER BY is_completed ASC, created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * Observes a single task by its unique ID.
     */
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: Long): Flow<TaskEntity?>

    /**
     * Synchronous one-shot query for background notification or worker lookup.
     */
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskByIdSync(id: Long): TaskEntity?

    /**
     * Inserts a new task or replaces in case of ID conflict.
     * Returns the auto-generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    /**
     * Updates an existing task with new field values.
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Deletes a specific task record.
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Deletes a task by ID.
     */
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    /**
     * Updates only the completion status of a task.
     */
    @Query("UPDATE tasks SET is_completed = :completed, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, completed: Boolean, updatedAt: Long)
}
