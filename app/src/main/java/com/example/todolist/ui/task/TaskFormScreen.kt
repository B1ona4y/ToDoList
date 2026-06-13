package com.example.todolist.ui.task

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todolist.data.database.entity.Category
import com.example.todolist.data.database.entity.Priority
import com.example.todolist.data.database.entity.RecurrenceType
import com.example.todolist.data.database.entity.TaskEntity
import com.example.todolist.ui.task.components.AttachmentSection
import com.example.todolist.ui.task.components.CategoryPicker
import com.example.todolist.ui.task.components.DateTimePicker
import com.example.todolist.ui.task.components.LocationPicker
import com.example.todolist.ui.task.components.PendingAttachment
import com.example.todolist.ui.task.components.PrioritySelector
import com.example.todolist.ui.task.components.RecurrencePicker
import com.example.todolist.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    taskId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val isEditMode = taskId != null

    // --- Form state ---
    var title               by rememberSaveable { mutableStateOf("") }
    var description         by rememberSaveable { mutableStateOf("") }
    var dueDate             by remember { mutableStateOf<Long?>(null) }
    var notificationTime    by remember { mutableStateOf<Long?>(null) }
    var priority            by rememberSaveable { mutableStateOf(Priority.MEDIUM) }
    var category            by rememberSaveable { mutableStateOf(Category.PERSONAL) }
    var locationName        by rememberSaveable { mutableStateOf("") }
    var latitude            by remember { mutableStateOf<Double?>(null) }
    var longitude           by remember { mutableStateOf<Double?>(null) }
    var isRecurring         by rememberSaveable { mutableStateOf(false) }
    var recurrenceType      by remember { mutableStateOf<RecurrenceType?>(null) }
    var recurrenceInterval  by rememberSaveable { mutableIntStateOf(1) }
    var attachments         by remember { mutableStateOf<List<PendingAttachment>>(emptyList()) }
    var isLoaded            by remember { mutableStateOf(false) }

    // --- Load existing task when editing ---
    val currentTask by viewModel.currentTask.collectAsState()

    LaunchedEffect(taskId) {
        taskId?.let { viewModel.loadTask(it) }
    }

    LaunchedEffect(currentTask) {
        if (isEditMode && !isLoaded && currentTask != null) {
            currentTask!!.let { task ->
                title              = task.title
                description        = task.description
                dueDate            = task.dueDate
                notificationTime   = task.notificationTime
                priority           = task.priority
                category           = task.category
                locationName       = task.locationName ?: ""
                latitude           = task.latitude
                longitude          = task.longitude
                isRecurring        = task.isRecurring
                recurrenceType     = task.recurrenceType
                recurrenceInterval = task.recurrenceInterval
            }
            isLoaded = true
        }
    }

    val titleError = title.isBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Task" else "New Task") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearCurrentTask()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = {
                            currentTask?.let { viewModel.deleteTask(it) }
                            viewModel.clearCurrentTask()
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete task")
                        }
                    }
                    TextButton(
                        onClick  = {
                            if (!titleError) {
                                val task = buildTask(
                                    existing           = currentTask,
                                    title              = title,
                                    description        = description,
                                    dueDate            = dueDate,
                                    notificationTime   = notificationTime,
                                    priority           = priority,
                                    category           = category,
                                    locationName       = locationName.ifBlank { null },
                                    latitude           = latitude,
                                    longitude          = longitude,
                                    isRecurring        = isRecurring,
                                    recurrenceType     = recurrenceType,
                                    recurrenceInterval = recurrenceInterval
                                )
                                if (isEditMode) viewModel.updateTask(task)
                                else viewModel.saveTask(task)
                                viewModel.clearCurrentTask()
                                onNavigateBack()
                            }
                        },
                        enabled = !titleError
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                label         = { Text("Title *") },
                isError       = titleError && title.isNotEmpty(),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Description") },
                minLines      = 3,
                maxLines      = 6,
                modifier      = Modifier.fillMaxWidth()
            )

            FormSection("Scheduling") {
                DateTimePicker(
                    selectedTimestamp  = dueDate,
                    label              = "due date",
                    onDateTimeSelected = { dueDate = it }
                )
                Spacer(Modifier.height(8.dp))
                DateTimePicker(
                    selectedTimestamp  = notificationTime,
                    label              = "reminder",
                    onDateTimeSelected = { notificationTime = it }
                )
            }

            FormSection("Priority") {
                PrioritySelector(
                    selected   = priority,
                    onSelected = { priority = it }
                )
            }

            FormSection("Category") {
                CategoryPicker(
                    selected   = category,
                    onSelected = { category = it }
                )
            }

            FormSection("Recurrence") {
                RecurrencePicker(
                    isRecurring        = isRecurring,
                    recurrenceType     = recurrenceType,
                    recurrenceInterval = recurrenceInterval,
                    onRecurringChanged = { isRecurring = it },
                    onTypeChanged      = { recurrenceType = it },
                    onIntervalChanged  = { recurrenceInterval = it }
                )
            }

            FormSection("Location") {
                LocationPicker(
                    locationName       = locationName,
                    latitude           = latitude,
                    longitude          = longitude,
                    onLocationSelected = { name, lat, lng ->
                        locationName = name
                        latitude     = lat
                        longitude    = lng
                    },
                    onLocationCleared  = {
                        locationName = ""
                        latitude     = null
                        longitude    = null
                    }
                )
            }

            FormSection("Attachments") {
                AttachmentSection(
                    attachments         = attachments,
                    onAttachmentsAdded  = { attachments = attachments + it },
                    onAttachmentRemoved = { attachments = attachments - it }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Spacer(Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(Modifier.height(12.dp))
    Text(
        text  = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(8.dp))
    content()
}

private fun buildTask(
    existing: TaskEntity?,
    title: String,
    description: String,
    dueDate: Long?,
    notificationTime: Long?,
    priority: Priority,
    category: Category,
    locationName: String?,
    latitude: Double?,
    longitude: Double?,
    isRecurring: Boolean,
    recurrenceType: RecurrenceType?,
    recurrenceInterval: Int
) = TaskEntity(
    id                 = existing?.id ?: 0,
    title              = title,
    description        = description,
    createdAt          = existing?.createdAt ?: System.currentTimeMillis(),
    dueDate            = dueDate,
    status             = existing?.status ?: com.example.todolist.data.database.entity.TaskStatus.PENDING,
    priority           = priority,
    category           = category,
    notificationTime   = notificationTime,
    latitude           = latitude,
    longitude          = longitude,
    locationName       = locationName,
    isRecurring        = isRecurring,
    recurrenceType     = if (isRecurring) recurrenceType else null,
    recurrenceInterval = recurrenceInterval
)
