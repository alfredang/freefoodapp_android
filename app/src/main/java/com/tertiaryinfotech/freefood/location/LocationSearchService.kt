package com.tertiaryinfotech.freefood.location

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Forward-geocodes a free-text query into place results, mirroring the iOS
 * `LocationSearchService` which wraps MapKit's `MKLocalSearch`. Uses the platform
 * [Geocoder] (no API key required).
 */
class LocationSearchService(context: Context) {

    data class PlaceResult(
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val country: String,
    )

    private val geocoder = Geocoder(context, Locale.getDefault())

    @Suppress("DEPRECATION")
    suspend fun search(query: String): List<PlaceResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return withContext(Dispatchers.IO) {
            runCatching {
                geocoder.getFromLocationName(trimmed, 8).orEmpty().mapNotNull { a ->
                    val name = a.featureName ?: a.thoroughfare ?: a.locality
                    ?: a.getAddressLine(0) ?: return@mapNotNull null
                    val addressParts = listOfNotNull(
                        a.thoroughfare, a.locality, a.adminArea, a.countryName,
                    ).distinct()
                    PlaceResult(
                        name = name,
                        address = addressParts.joinToString(", "),
                        latitude = a.latitude,
                        longitude = a.longitude,
                        country = a.countryCode ?: "",
                    )
                }
            }.getOrDefault(emptyList())
        }
    }
}
