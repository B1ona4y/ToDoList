package com.example.todolist.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.todolist.data.database.entity.TaskEntity
import com.example.todolist.receivers.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = LocationServices.getGeofencingClient(context)

    private fun buildPendingIntent(taskId: Long): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            putExtra(Constants.EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun register(task: TaskEntity) {
        val lat = task.latitude ?: return
        val lng = task.longitude ?: return

        val geofence = Geofence.Builder()
            .setRequestId(task.id.toString())
            .setCircularRegion(lat, lng, Constants.GEOFENCE_RADIUS_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        client.addGeofences(request, buildPendingIntent(task.id))
    }

    fun remove(task: TaskEntity) {
        client.removeGeofences(listOf(task.id.toString()))
    }
}
