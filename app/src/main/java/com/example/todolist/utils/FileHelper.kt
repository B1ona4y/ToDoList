package com.example.todolist.utils

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val attachmentsDir: File
        get() = File(context.filesDir, "attachments").apply { mkdirs() }

    fun copyToInternalStorage(uri: Uri, fileName: String): String {
        val dest = File(attachmentsDir, "${System.currentTimeMillis()}_$fileName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        return dest.absolutePath
    }

    fun delete(filePath: String) {
        File(filePath).takeIf { it.exists() }?.delete()
    }
}
