package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TaskPriority

/**
 * TaskEntity represents the SQLite table structure for tasks in Room database.
 * Holds all task details, completion status, categorization, and scheduling metadata.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "priority")
    val priority: String = TaskPriority.MEDIUM.name,

    @ColumnInfo(name = "category")
    val category: String = "Personal",

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,

    @ColumnInfo(name = "due_time_hour")
    val dueTimeHour: Int? = null,

    @ColumnInfo(name = "due_time_minute")
    val dueTimeMinute: Int? = null,

    @ColumnInfo(name = "has_reminder")
    val hasReminder: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
