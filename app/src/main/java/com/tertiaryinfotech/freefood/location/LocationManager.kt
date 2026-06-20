package com.tertiaryinfotech.freefood.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

/** Current-location provider, mirroring the iOS `LocationManager` (CoreLocation). */
class LocationManager(private val context: Context) {

    data class LatLng(val latitude: Double, val longitude: Double)

    private val client = LocationServices.getFusedLocationProviderClient(context)
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Refresh the cached current location. No-op (keeps last value) without permission. */
    suspend fun requestLocation() {
        if (!hasPermission()) return
        runCatching {
            @Suppress("MissingPermission")
            val last = client.lastLocation.await()
            if (last != null) {
                _currentLocation.value = LatLng(last.latitude, last.longitude)
            } else {
                @Suppress("MissingPermission")
                val fresh = client.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token,
                ).await()
                if (fresh != null) _currentLocation.value = LatLng(fresh.latitude, fresh.longitude)
            }
        }
    }
}
