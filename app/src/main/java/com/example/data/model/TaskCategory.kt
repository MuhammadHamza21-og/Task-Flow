package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CategoryHealth
import com.example.ui.theme.CategoryPersonal
import com.example.ui.theme.CategoryShopping
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryWork

/**
 * TaskCategory provides predefined category tags for task organization.
 */
enum class TaskCategory(val displayName: String, val icon: ImageVector, val color: Color) {
    WORK("Work", Icons.Default.Work, CategoryWork),
    PERSONAL("Personal", Icons.Default.Person, CategoryPersonal),
    STUDY("Study", Icons.Default.School, CategoryStudy),
    SHOPPING("Shopping", Icons.Default.ShoppingCart, CategoryShopping),
    HEALTH("Health", Icons.Default.FitnessCenter, CategoryHealth),
    OTHER("Other", Icons.Default.Bookmark, CategoryPersonal);

    companion object {
        val ALL_CATEGORIES = entries.map { it.displayName }

        fun getIconForCategory(categoryName: String): ImageVector {
            return entries.firstOrNull { it.displayName.equals(categoryName, ignoreCase = true) }?.icon
                ?: Icons.Default.Bookmark
        }

        fun getColorForCategory(categoryName: String): Color {
            return entries.firstOrNull { it.displayName.equals(categoryName, ignoreCase = true) }?.color
                ?: CategoryPersonal
        }
    }
}
