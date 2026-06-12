package com.example.todolist.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todolist.data.database.converter.Converters
import com.example.todolist.data.database.dao.AttachmentDao
import com.example.todolist.data.database.dao.TaskDao
import com.example.todolist.data.database.entity.AttachmentEntity
import com.example.todolist.data.database.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, AttachmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun attachmentDao(): AttachmentDao
}
