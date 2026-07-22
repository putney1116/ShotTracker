package com.example.shottracker.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.shottracker.domain.model.GpsLocation
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    companion object {
        private const val UPDATE_INTERVAL_MS = 5000L
        private const val FASTEST_UPDATE_INTERVAL_MS = 2000L
        private const val MAX_ACCURACY_METERS = 20f
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): GpsLocation? {
        if (!hasLocationPermission()) {
            Log.d("LocationService", "No location permission")
            return null
        }

        return try {
            // Always request a fresh location
            Log.d("LocationService", "Requesting fresh location")
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(10_000)
                .build()
            val freshLoc = fusedLocationClient.getCurrentLocation(
                request, null
            ).await()
            if (freshLoc != null) {
                Log.d("LocationService", "Fresh location: ${freshLoc.latitude}, ${freshLoc.longitude}")
                GpsLocation(
                    latitude = freshLoc.latitude,
                    longitude = freshLoc.longitude,
                    accuracy = freshLoc.accuracy
                )
            } else {
                Log.w("LocationService", "getCurrentLocation returned null")
                null
            }
        } catch (e: SecurityException) {
            Log.e("LocationService", "SecurityException getting location", e)
            null
        } catch (e: Exception) {
            Log.e("LocationService", "Exception getting location", e)
            null
        }
    }

    fun getLocationUpdates(): Flow<GpsLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (location.accuracy <= MAX_ACCURACY_METERS) {
                        trySend(
                            GpsLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy
                            )
                        )
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
            return@callbackFlow
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
