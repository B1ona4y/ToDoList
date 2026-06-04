package com.example.todolist.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId    = intent.getLongExtra("task_id", -1)
        val taskTitle = intent.getStringExtra("task_title") ?: "Task reminder"
        // TODO: show notification
    }
}