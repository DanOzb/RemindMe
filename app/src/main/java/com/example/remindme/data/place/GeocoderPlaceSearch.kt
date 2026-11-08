package com.example.remindme.data.place

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.remindme.data.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GeocoderPlaceSearch(context: Context) : PlaceSearch {

    private val geocoder = Geocoder(context.applicationContext)

    override suspend fun search(query: String, near: LatLng?): SearchOutcome {
        if (query.isBlank()) return SearchOutcome.Found(emptyList())
        if (!Geocoder.isPresent()) return SearchOutcome.NotAvailable
        return try {
            val local = near?.let { find(query, boundingBox(it, BIAS_RADIUS_METERS)) }.orEmpty()
            val addresses = local.ifEmpty { find(query, null) }
            SearchOutcome.Found(addresses.mapNotNull { it.toPlaceResult() }.distinctBy { it.latLng })
        } catch (_: IOException) {
            SearchOutcome.Failed
        } catch (_: IllegalArgumentException) {
            SearchOutcome.Failed
        }
    }

    private suspend fun find(query: String, box: LatLngBounds?): List<Address> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) findAsync(query, box)
        else withContext(Dispatchers.IO) { findBlocking(query, box) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun findAsync(query: String, box: LatLngBounds?): List<Address> =
        suspendCancellableCoroutine { cont ->
            val listener = object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    if (cont.isActive) cont.resume(addresses)
                }

                override fun onError(errorMessage: String?) {
                    if (cont.isActive) cont.resumeWithException(IOException(errorMessage))
                }
            }
            if (box == null) {
                geocoder.getFromLocationName(query, MAX_RESULTS, listener)
            } else {
                geocoder.getFromLocationName(
                    query, MAX_RESULTS,
                    box.southWest.latitude, box.southWest.longitude,
                    box.northEast.latitude, box.northEast.longitude,
                    listener,
                )
            }
        }

    @Suppress("DEPRECATION")
    private fun findBlocking(query: String, box: LatLngBounds?): List<Address> =
        if (box == null) {
            geocoder.getFromLocationName(query, MAX_RESULTS)
        } else {
            geocoder.getFromLocationName(
                query, MAX_RESULTS,
                box.southWest.latitude, box.southWest.longitude,
                box.northEast.latitude, box.northEast.longitude,
            )
        }.orEmpty()

    private fun Address.toPlaceResult(): PlaceResult? {
        if (!hasLatitude() || !hasLongitude()) return null
        val line = (if (maxAddressLineIndex >= 0) getAddressLine(0) else null)
            ?: listOfNotNull(thoroughfare, subThoroughfare, locality).joinToString(" ").ifBlank { return null }
        return PlaceResult(
            label = placeLabel(featureName, thoroughfare, subThoroughfare, line),
            address = line,
            latLng = LatLng(latitude, longitude),
        )
    }

    private companion object {
        const val MAX_RESULTS = 5
        const val BIAS_RADIUS_METERS = 40_000.0
    }
}
