package com.example.todolist.ui.task.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.todolist.data.database.entity.Priority

@Composable
fun PrioritySelector(
    selected: Priority,
    onSelected: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Priority.entries.forEach { priority ->
            val color = priorityColor(priority)
            FilterChip(
                selected = selected == priority,
                onClick  = { onSelected(priority) },
                label    = { Text(priority.label) },
                modifier = Modifier.weight(1f),
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor      = color.copy(alpha = 0.2f),
                    selectedLabelColor          = color,
                    selectedLeadingIconColor    = color
                )
            )
        }
    }
}

fun priorityColor(priority: Priority) = when (priority) {
    Priority.LOW    -> Color(0xFF43A047)
    Priority.MEDIUM -> Color(0xFFFF9800)
    Priority.HIGH   -> Color(0xFFE53935)
}

private val Priority.label get() = when (this) {
    Priority.LOW    -> "Low"
    Priority.MEDIUM -> "Medium"
    Priority.HIGH   -> "High"
}
