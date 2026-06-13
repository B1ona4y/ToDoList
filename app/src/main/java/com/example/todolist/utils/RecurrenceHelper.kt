package com.example.todolist.utils

import com.example.todolist.data.database.entity.RecurrenceType
import com.example.todolist.data.database.entity.TaskEntity
import java.util.concurrent.TimeUnit

object RecurrenceHelper {

    fun nextOccurrence(task: TaskEntity): Long {
        val base = task.notificationTime ?: return -1L
        val days = when (task.recurrenceType) {
            RecurrenceType.DAILY   -> 1L
            RecurrenceType.WEEKLY  -> 7L
            RecurrenceType.MONTHLY -> 30L
            RecurrenceType.CUSTOM  -> task.recurrenceInterval.toLong()
            null                   -> return -1L
        }
        return base + TimeUnit.DAYS.toMillis(days)
    }
}
