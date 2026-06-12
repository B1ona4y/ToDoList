package com.example.todolist.data.database.dao

import androidx.room.*
import com.example.todolist.data.database.entity.TaskEntity
import com.example.todolist.data.database.entity.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // --- Queries ---

    @Query("SELECT * FROM tasks ORDER BY priority DESC, dueDate ASC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("""
        SELECT * FROM tasks
        WHERE dueDate >= :startOfDay AND dueDate < :endOfDay
        ORDER BY priority DESC
    """)
    fun getForToday(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE dueDate < :now
          AND status != 'COMPLETED'
          AND status != 'CANCELLED'
        ORDER BY dueDate ASC
    """)
    fun getOverdue(now: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY priority DESC, dueDate ASC")
    fun getByStatus(status: TaskStatus): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE title LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY priority DESC
    """)
    fun search(query: String): Flow<List<TaskEntity>>

    // --- Writes ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
