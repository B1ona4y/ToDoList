package com.example.todolist.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todolist.ui.home.components.CategoryFilterChips
import com.example.todolist.ui.home.components.SortMenu
import com.example.todolist.ui.home.components.TaskCard
import com.example.todolist.ui.home.components.TaskSearchBar
import com.example.todolist.viewmodel.TaskTab
import com.example.todolist.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTask: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks          by viewModel.taskList.collectAsState()
    val selectedTab    by viewModel.selectedTab.collectAsState()
    val searchQuery    by viewModel.searchQuery.collectAsState()
    val sortOrder      by viewModel.sortOrder.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Tasks") },
                    actions = {
                        SortMenu(
                            sortOrder = sortOrder,
                            onSortSelected = viewModel::setSortOrder
                        )
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
                TaskSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::setSearchQuery
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                TaskTab.entries.forEach { tab ->
                    Tab(
                        selected  = selectedTab == tab,
                        onClick   = { viewModel.selectTab(tab) },
                        text      = { Text(tab.label) }
                    )
                }
            }

            CategoryFilterChips(
                selectedCategory  = filterCategory,
                onCategorySelected = viewModel::setFilterCategory,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (tasks.isEmpty()) {
                EmptyTasksState(tab = selectedTab)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task      = task,
                            onClick   = { onTaskClick(task.id) },
                            onComplete = { viewModel.toggleComplete(task) },
                            onDelete  = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTasksState(tab: TaskTab) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (tab) {
                    TaskTab.ALL     -> "No tasks yet.\nTap + to create one."
                    TaskTab.TODAY   -> "Nothing due today."
                    TaskTab.OVERDUE -> "No overdue tasks."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
