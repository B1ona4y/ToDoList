package com.example.todolist.data.repository

import com.example.todolist.data.database.dao.AttachmentDao
import com.example.todolist.data.database.dao.TaskDao
import com.example.todolist.data.database.entity.AttachmentEntity
import com.example.todolist.data.database.entity.TaskEntity
import com.example.todolist.data.database.entity.TaskStatus
import com.example.todolist.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val attachmentDao: AttachmentDao
) {

    fun getAllTasks(): Flow<List<TaskEntity>> =
        taskDao.getAll()

    fun getTasksForToday(): Flow<List<TaskEntity>> {
        val (start, end) = DateUtils.todayRange()
        return taskDao.getForToday(start, end)
    }

    fun getOverdueTasks(): Flow<List<TaskEntity>> =
        taskDao.getOverdue(System.currentTimeMillis())

    fun getByStatus(status: TaskStatus): Flow<List<TaskEntity>> =
        taskDao.getByStatus(status)

    fun search(query: String): Flow<List<TaskEntity>> =
        taskDao.search(query)

    suspend fun getById(id: Long): TaskEntity? =
        taskDao.getById(id)

    suspend fun save(task: TaskEntity): Long =
        taskDao.insert(task)

    suspend fun update(task: TaskEntity) =
        taskDao.update(task)

    suspend fun delete(task: TaskEntity) =
        taskDao.delete(task)


    fun getAttachments(taskId: Long): Flow<List<AttachmentEntity>> =
        attachmentDao.getForTask(taskId)

    suspend fun getAttachmentsOnce(taskId: Long): List<AttachmentEntity> =
        attachmentDao.getForTaskOnce(taskId)

    suspend fun addAttachment(attachment: AttachmentEntity): Long =
        attachmentDao.insert(attachment)

    suspend fun removeAttachment(attachment: AttachmentEntity) =
        attachmentDao.delete(attachment)
}