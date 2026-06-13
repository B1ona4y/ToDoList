package com.example.todolist.ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.todolist.data.database.entity.Priority
import com.example.todolist.data.database.entity.TaskEntity
import com.example.todolist.data.database.entity.TaskStatus
import com.example.todolist.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onComplete(); false }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        },
        positionalThreshold = { it * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF43A047)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE53935)
                    else                              -> MaterialTheme.colorScheme.surfaceVariant
                },
                label = "swipe_color"
            )
            val alignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else                             -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = MaterialTheme.shapes.medium),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                        else                             -> Icons.Default.Delete
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    ) {
        TaskCardContent(task = task, onClick = onClick)
    }
}

@Composable
private fun TaskCardContent(task: TaskEntity, onClick: () -> Unit) {
    val priorityColor = when (task.priority) {
        Priority.HIGH   -> Color(0xFFE53935)
        Priority.MEDIUM -> Color(0xFFFF9800)
        Priority.LOW    -> Color(0xFF43A047)
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Priority colour bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Title + status icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (task.status == TaskStatus.COMPLETED)
                            TextDecoration.LineThrough else null
                    )
                    if (task.status == TaskStatus.COMPLETED) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Description
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Meta row: due date · category · badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Due date
                    task.dueDate?.let { dueDate ->
                        val overdue = DateUtils.isOverdue(dueDate) &&
                                task.status != TaskStatus.COMPLETED
                        Text(
                            text = DateUtils.formatDate(dueDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (overdue) Color(0xFFE53935)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Category pill
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = task.category.name
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Recurring icon
                    if (task.isRecurring) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Recurring",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Location icon
                    if (task.locationName != null) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Has location",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
