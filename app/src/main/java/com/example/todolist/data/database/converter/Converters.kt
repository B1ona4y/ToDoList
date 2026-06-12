package com.example.todolist.data.database.converter

import androidx.room.TypeConverter
import com.example.todolist.data.database.entity.Category
import com.example.todolist.data.database.entity.Priority
import com.example.todolist.data.database.entity.RecurrenceType
import com.example.todolist.data.database.entity.TaskStatus

class Converters {

    @TypeConverter fun taskStatusToString(value: TaskStatus): String = value.name
    @TypeConverter fun stringToTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter fun priorityToString(value: Priority): String = value.name
    @TypeConverter fun stringToPriority(value: String): Priority = Priority.valueOf(value)

    @TypeConverter fun categoryToString(value: Category): String = value.name
    @TypeConverter fun stringToCategory(value: String): Category = Category.valueOf(value)

    @TypeConverter fun recurrenceTypeToString(value: RecurrenceType?): String? = value?.name
    @TypeConverter fun stringToRecurrenceType(value: String?): RecurrenceType? =
        value?.let { RecurrenceType.valueOf(it) }
}
