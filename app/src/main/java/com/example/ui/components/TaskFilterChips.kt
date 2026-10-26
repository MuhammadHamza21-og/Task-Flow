package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskCategory
import com.example.ui.viewmodel.TaskFilterStatus
import com.example.ui.viewmodel.TaskSortOrder

/**
 * Filter and category selection chips bar.
 */
@Composable
fun TaskFilterChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    selectedStatus: TaskFilterStatus,
    onStatusSelected: (TaskFilterStatus) -> Unit,
    selectedSortOrder: TaskSortOrder,
    onSortOrderSelected: (TaskSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status filter row + Sort button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status filters (All, Active, Done)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TaskFilterStatus.entries.forEach { status ->
                    val isSelected = selectedStatus == status
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { onStatusSelected(status) },
                        label = {
                            Text(
                                text = status.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.testTag("filter_status_${status.name.lowercase()}")
                    )
                }
            }

            // Sort Menu Button
            Box {
                OutlinedButton(
                    onClick = { isSortMenuExpanded = true },
                    modifier = Modifier.testTag("sort_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort Tasks",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedSortOrder.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = { isSortMenuExpanded = false }
                ) {
                    TaskSortOrder.entries.forEach { sortOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = sortOption.displayName,
                                    fontWeight = if (selectedSortOrder == sortOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onSortOrderSelected(sortOption)
                                isSortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (selectedSortOrder == sortOption) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // Categories horizontal scrolling chip list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All" chip
            val isAllSelected = selectedCategory == "All"
            ElevatedFilterChip(
                selected = isAllSelected,
                onClick = { onCategorySelected("All") },
                label = { Text("All Categories") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("category_chip_all")
            )

            // Category specific chips
            TaskCategory.entries.forEach { category ->
                val isSelected = selectedCategory.equals(category.displayName, ignoreCase = true)
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category.displayName) },
                    label = { Text(category.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else category.color,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                )
            }
        }
    }
}
