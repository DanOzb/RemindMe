package com.example.remindme.triggers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.remindme.data.location.LocationAccess
import com.example.remindme.data.location.locationAccess
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class Geofences(context: Context) {

    private val appContext = context.applicationContext
    private val client = LocationServices.getGeofencingClient(appContext)

    // Shared by all geofences; the event says which ones triggered. Must be mutable: Play services fills it in.
    private val pendingIntent: PendingIntent by lazy {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        PendingIntent.getBroadcast(
            appContext, 0,
            Intent(appContext, TriggerReceiver::class.java).setAction(TriggerReceiver.ACTION_GEOFENCE),
            flags,
        )
    }

    fun canUse(): Boolean = appContext.locationAccess() == LocationAccess.Always

    @SuppressLint("MissingPermission") // checked by canUse()
    suspend fun register(taskId: Int, plan: GeofencePlan): Boolean {
        if (!canUse()) {
            remove(taskId)
            return false
        }
        val geofence = Geofence.Builder()
            .setRequestId(taskId.toString())
            .setCircularRegion(plan.latitude, plan.longitude, plan.radiusMeters.toFloat())
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(if (plan.exit) Geofence.GEOFENCE_TRANSITION_EXIT else Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val initialTrigger = when {
            !plan.initialTrigger -> 0
            plan.exit -> GeofencingRequest.INITIAL_TRIGGER_EXIT
            else -> GeofencingRequest.INITIAL_TRIGGER_ENTER
        }
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(initialTrigger)
            .addGeofence(geofence)
            .build()
        return try {
            client.addGeofences(request, pendingIntent).awaitSuccess()
        } catch (_: SecurityException) {
            false
        }
    }

    suspend fun remove(taskId: Int) {
        client.removeGeofences(listOf(taskId.toString())).awaitSuccess()
    }
}

private suspend fun Task<*>.awaitSuccess(): Boolean = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { if (cont.isActive) cont.resume(it.isSuccessful) }
}
