package com.example.todolist.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.PERSONAL,
    val notificationTime: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType? = null,
    val recurrenceInterval: Int = 1
)

enum class TaskStatus    { PENDING, IN_PROGRESS, COMPLETED, CANCELLED }
enum class Priority      { LOW, MEDIUM, HIGH }
enum class Category      { PERSONAL, WORK, SHOPPING, HEALTH, EDUCATION, OTHER }
enum class RecurrenceType { DAILY, WEEKLY, MONTHLY, CUSTOM }
