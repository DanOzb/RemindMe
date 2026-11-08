package com.example.remindme.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.example.remindme.data.LatLng
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CurrentLocation(context: Context) {

    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    fun hasPermission(): Boolean = appContext.locationAccess() != LocationAccess.Off

    @SuppressLint("MissingPermission") // checked by hasPermission()
    suspend fun get(): LatLng? {
        if (!hasPermission()) return null
        return try {
            suspendCancellableCoroutine { cont ->
                val cancel = CancellationTokenSource()
                cont.invokeOnCancellation { cancel.cancel() }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancel.token)
                    .addOnSuccessListener { location ->
                        if (cont.isActive) cont.resume(location?.let { LatLng(it.latitude, it.longitude) })
                    }
                    .addOnFailureListener { if (cont.isActive) cont.resume(null) }
            }
        } catch (_: SecurityException) {
            null // permission revoked between the check and the call
        }
    }
}
