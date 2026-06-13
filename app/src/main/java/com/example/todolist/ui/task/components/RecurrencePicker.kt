package com.example.todolist.ui.task.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.todolist.data.database.entity.RecurrenceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrencePicker(
    isRecurring: Boolean,
    recurrenceType: RecurrenceType?,
    recurrenceInterval: Int,
    onRecurringChanged: (Boolean) -> Unit,
    onTypeChanged: (RecurrenceType) -> Unit,
    onIntervalChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var typeExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Repeat task", modifier = Modifier.weight(1f))
            Switch(
                checked         = isRecurring,
                onCheckedChange = onRecurringChanged
            )
        }

        if (isRecurring) {
            Spacer(Modifier.height(8.dp))

            // Recurrence type dropdown
            ExposedDropdownMenuBox(
                expanded          = typeExpanded,
                onExpandedChange  = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    value        = recurrenceType?.label ?: "Select type",
                    onValueChange = {},
                    readOnly     = true,
                    label        = { Text("Repeat every") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier     = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded          = typeExpanded,
                    onDismissRequest  = { typeExpanded = false }
                ) {
                    RecurrenceType.entries.forEach { type ->
                        DropdownMenuItem(
                            text    = { Text(type.label) },
                            onClick = { onTypeChanged(type); typeExpanded = false }
                        )
                    }
                }
            }

            // Interval input (only shown for CUSTOM)
            if (recurrenceType == RecurrenceType.CUSTOM) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = recurrenceInterval.toString(),
                    onValueChange = { it.toIntOrNull()?.let(onIntervalChanged) },
                    label         = { Text("Every N days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private val RecurrenceType.label get() = when (this) {
    RecurrenceType.DAILY   -> "Daily"
    RecurrenceType.WEEKLY  -> "Weekly"
    RecurrenceType.MONTHLY -> "Monthly"
    RecurrenceType.CUSTOM  -> "Custom"
}
