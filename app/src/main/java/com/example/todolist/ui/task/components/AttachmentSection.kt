package com.example.todolist.ui.task.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.todolist.data.database.entity.AttachmentEntity

data class PendingAttachment(
    val uri: Uri,
    val fileName: String,
    val mimeType: String
)

@Composable
fun AttachmentSection(
    existingAttachments: List<AttachmentEntity>,
    pendingAttachments: List<PendingAttachment>,
    onExistingRemoved: (AttachmentEntity) -> Unit,
    onPendingAdded: (List<PendingAttachment>) -> Unit,
    onPendingRemoved: (PendingAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val pending = uris.map { uri ->
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            PendingAttachment(
                uri      = uri,
                fileName = uri.lastPathSegment ?: "file",
                mimeType = mimeType
            )
        }
        if (pending.isNotEmpty()) onPendingAdded(pending)
    }

    Column(modifier = modifier) {
        existingAttachments.forEach { attachment ->
            AttachmentRow(
                name     = attachment.fileName,
                mimeType = attachment.mimeType,
                onRemove = { onExistingRemoved(attachment) }
            )
        }

        pendingAttachments.forEach { attachment ->
            AttachmentRow(
                name     = attachment.fileName,
                mimeType = attachment.mimeType,
                onRemove = { onPendingRemoved(attachment) }
            )
        }

        if (existingAttachments.isNotEmpty() || pendingAttachments.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick  = { filePicker.launch("*/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add attachment")
        }

        Spacer(Modifier.height(4.dp))
        Text(
            text  = "Photos, documents, PDFs, voice recordings",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AttachmentRow(name: String, mimeType: String, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        AssistChip(
            onClick     = {},
            label       = { Text(name, maxLines = 1) },
            leadingIcon = {
                Icon(
                    imageVector        = mimeTypeIcon(mimeType),
                    contentDescription = null,
                    modifier           = Modifier.size(AssistChipDefaults.IconSize)
                )
            },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Remove")
        }
    }
}

private fun mimeTypeIcon(mimeType: String): ImageVector = when {
    mimeType.startsWith("image/") -> Icons.Default.Image
    mimeType.startsWith("audio/") -> Icons.Default.Mic
    else                          -> Icons.Default.Description
}
