package com.example.remindme.data.place

import com.example.remindme.data.LatLng
import kotlin.math.cos

data class PlaceResult(val label: String, val address: String, val latLng: LatLng)

sealed interface SearchOutcome {
    data class Found(val places: List<PlaceResult>) : SearchOutcome
    data object NotAvailable : SearchOutcome
    data object Failed : SearchOutcome
}

fun interface PlaceSearch {
    /** [near] biases results towards that point when given. */
    suspend fun search(query: String, near: LatLng?): SearchOutcome
}

data class LatLngBounds(val southWest: LatLng, val northEast: LatLng)

fun boundingBox(center: LatLng, radiusMeters: Double): LatLngBounds {
    val dLat = radiusMeters / 111_320.0
    // Degrees of longitude shrink towards the poles
    val dLng = dLat / cos(Math.toRadians(center.latitude)).coerceAtLeast(0.01)
    return LatLngBounds(
        southWest = LatLng((center.latitude - dLat).coerceAtLeast(-90.0), (center.longitude - dLng).coerceAtLeast(-180.0)),
        northEast = LatLng((center.latitude + dLat).coerceAtMost(90.0), (center.longitude + dLng).coerceAtMost(180.0)),
    )
}

fun placeLabel(featureName: String?, thoroughfare: String?, subThoroughfare: String?, addressLine: String): String {
    val feature = featureName?.trim()?.takeIf {
        it.isNotEmpty() && !it.all(Char::isDigit) && it != thoroughfare && it != subThoroughfare
    }
    return feature ?: addressLine.substringBefore(",").trim().ifEmpty { addressLine }
}
