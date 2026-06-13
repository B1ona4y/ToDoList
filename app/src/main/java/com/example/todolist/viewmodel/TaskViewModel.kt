package com.example.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.database.entity.*
import com.example.todolist.data.database.entity.AttachmentEntity
import com.example.todolist.data.repository.TaskRepository
import com.example.todolist.ui.task.components.PendingAttachment
import com.example.todolist.utils.AlarmScheduler
import com.example.todolist.utils.FileHelper
import com.example.todolist.utils.GeofenceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceHelper: GeofenceHelper,
    private val fileHelper: FileHelper
) : ViewModel() {

    // --- Filter state (drives the task list) ---

    private val _selectedTab    = MutableStateFlow(TaskTab.ALL)
    private val _searchQuery    = MutableStateFlow("")
    private val _sortOrder      = MutableStateFlow(SortOrder.BY_PRIORITY)
    private val _filterCategory = MutableStateFlow<Category?>(null)

    val selectedTab:    StateFlow<TaskTab>    = _selectedTab.asStateFlow()
    val searchQuery:    StateFlow<String>     = _searchQuery.asStateFlow()
    val sortOrder:      StateFlow<SortOrder>  = _sortOrder.asStateFlow()
    val filterCategory: StateFlow<Category?>  = _filterCategory.asStateFlow()

    fun selectTab(tab: TaskTab)             { _selectedTab.value = tab }
    fun setSearchQuery(query: String)       { _searchQuery.value = query }
    fun setSortOrder(order: SortOrder)      { _sortOrder.value = order }
    fun setFilterCategory(cat: Category?)   { _filterCategory.value = cat }

    // --- Task list (reacts to filter state automatically) ---

    private data class FilterState(
        val tab: TaskTab,
        val query: String,
        val sort: SortOrder,
        val category: Category?
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val taskList: StateFlow<List<TaskEntity>> = combine(
        _selectedTab, _searchQuery, _sortOrder, _filterCategory, ::FilterState
    ).flatMapLatest { filter ->
        val source = when {
            filter.query.isNotBlank()      -> repository.search(filter.query)
            filter.tab == TaskTab.TODAY    -> repository.getTasksForToday()
            filter.tab == TaskTab.OVERDUE  -> repository.getOverdueTasks()
            else                           -> repository.getAllTasks()
        }
        source.map { tasks ->
            tasks
                .filter { filter.category == null || it.category == filter.category }
                .sortedWith(filter.sort.comparator)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- Single task (used by TaskFormScreen) ---

    private val _currentTask = MutableStateFlow<TaskEntity?>(null)
    val currentTask: StateFlow<TaskEntity?> = _currentTask.asStateFlow()

    fun loadTask(id: Long) = viewModelScope.launch {
        _currentTask.value = repository.getById(id)
    }

    fun clearCurrentTask() {
        _currentTask.value = null
    }

    // --- CRUD ---

    suspend fun getAttachmentsOnce(taskId: Long): List<AttachmentEntity> =
        repository.getAttachmentsOnce(taskId)

    fun saveTask(task: TaskEntity, pending: List<PendingAttachment> = emptyList()) =
        viewModelScope.launch {
            val id = repository.save(task)
            val savedTask = task.copy(id = id)
            alarmScheduler.schedule(savedTask)
            geofenceHelper.register(savedTask)
            pending.forEach { p ->
                val path = fileHelper.copyToInternalStorage(p.uri, p.fileName)
                repository.addAttachment(AttachmentEntity(taskId = id, fileName = p.fileName, filePath = path, mimeType = p.mimeType))
            }
        }

    fun updateTask(task: TaskEntity, newAttachments: List<PendingAttachment> = emptyList(), removedAttachments: List<AttachmentEntity> = emptyList()) =
        viewModelScope.launch {
            repository.update(task)
            alarmScheduler.cancel(task)
            alarmScheduler.schedule(task)
            geofenceHelper.remove(task)
            geofenceHelper.register(task)
            removedAttachments.forEach { a ->
                fileHelper.delete(a.filePath)
                repository.removeAttachment(a)
            }
            newAttachments.forEach { p ->
                val path = fileHelper.copyToInternalStorage(p.uri, p.fileName)
                repository.addAttachment(AttachmentEntity(taskId = task.id, fileName = p.fileName, filePath = path, mimeType = p.mimeType))
            }
        }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        alarmScheduler.cancel(task)
        geofenceHelper.remove(task)
        repository.delete(task)
    }

    fun toggleComplete(task: TaskEntity) = viewModelScope.launch {
        val newStatus = if (task.status == TaskStatus.COMPLETED)
            TaskStatus.PENDING else TaskStatus.COMPLETED
        repository.update(task.copy(status = newStatus))
    }
}

// --- Supporting types ---

enum class TaskTab(val label: String) {
    ALL("All"), TODAY("Today"), OVERDUE("Overdue")
}

enum class SortOrder(val label: String, val comparator: Comparator<TaskEntity>) {
    BY_PRIORITY("By Priority", compareByDescending { it.priority.ordinal }),
    BY_DUE_DATE("By Due Date", compareBy(nullsLast()) { it.dueDate }),
    BY_CREATED ("By Created",  compareByDescending { it.createdAt })
}