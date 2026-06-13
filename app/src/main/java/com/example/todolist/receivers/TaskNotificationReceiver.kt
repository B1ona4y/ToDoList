package com.example.todolist.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.todolist.MainActivity
import com.example.todolist.data.repository.TaskRepository
import com.example.todolist.data.database.entity.TaskEntity
import com.example.todolist.utils.AlarmScheduler
import com.example.todolist.utils.Constants
import com.example.todolist.utils.RecurrenceHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskNotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: TaskRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(Constants.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val task = repository.getById(taskId) ?: return@launch
                postNotification(context, task)
                if (task.isRecurring) {
                    val nextTime = RecurrenceHelper.nextOccurrence(task)
                    if (nextTime > 0L) {
                        val updated = task.copy(notificationTime = nextTime)
                        repository.update(updated)
                        alarmScheduler.schedule(updated)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun postNotification(context: Context, task: TaskEntity) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_TASKS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(task.title)
            .setContentText(task.description.ifBlank { "You have a task reminder." })
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(task.id.toInt(), notification)
    }
}
