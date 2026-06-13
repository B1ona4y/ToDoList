package com.example.todolist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.todolist.utils.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodoListApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_TASKS,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).also { manager.createNotificationChannel(it) }

        NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_GEOFENCE,
            "Location Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).also { manager.createNotificationChannel(it) }
    }
}
