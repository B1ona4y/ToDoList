package com.example.todolist.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.todolist.data.database.entity.TaskEntity
import com.example.todolist.ui.theme.ToDoListTheme
import com.example.todolist.viewmodel.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickNoteActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoListTheme {
                var title by remember { mutableStateOf("") }

                Dialog(onDismissRequest = { finish() }) {
                    Card(
                        shape     = MaterialTheme.shapes.large,
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Quick note", style = MaterialTheme.typography.titleMedium)

                            Spacer(Modifier.padding(8.dp))

                            OutlinedTextField(
                                value         = title,
                                onValueChange = { title = it },
                                label         = { Text("Title") },
                                singleLine    = true,
                                modifier      = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.padding(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = { finish() }) {
                                    Text("Cancel")
                                }
                                Spacer(Modifier.width(8.dp))
                                TextButton(
                                    onClick  = {
                                        if (title.isNotBlank()) {
                                            viewModel.saveTask(TaskEntity(title = title.trim()))
                                            finish()
                                        }
                                    },
                                    enabled  = title.isNotBlank()
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
