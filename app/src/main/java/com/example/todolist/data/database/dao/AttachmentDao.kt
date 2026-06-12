package com.example.todolist.data.database.dao

import androidx.room.*
import com.example.todolist.data.database.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE taskId = :taskId ORDER BY createdAt DESC")
    fun getForTask(taskId: Long): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity): Long

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Long)
}
