package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium

/**
 * TaskPriority represents the urgency level of a task.
 * Used for filtering, sorting, and visual color coding.
 */
enum class TaskPriority(val displayName: String, val weight: Int) {
    HIGH("High", 3),
    MEDIUM("Medium", 2),
    LOW("Low", 1);

    fun getColor(): Color {
        return when (this) {
            HIGH -> PriorityHigh
            MEDIUM -> PriorityMedium
            LOW -> PriorityLow
        }
    }

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
