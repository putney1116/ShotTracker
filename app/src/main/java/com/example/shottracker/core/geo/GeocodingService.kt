package com.example.shottracker.core.geo

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.shottracker.domain.model.GpsLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Converts a zip code or place name (e.g. "60515" or "Naperville, IL") into coordinates using
 * Android's built-in [Geocoder]. Returns null if the device geocoder is unavailable, there's no
 * network, or nothing matched. Never throws.
 */
@Singleton
class GeocodingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun geocode(query: String): GpsLocation? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        if (!Geocoder.isPresent()) {
            Log.w("GeocodingService", "Geocoder not present on device")
            return null
        }
        val geocoder = Geocoder(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocodeAsync(geocoder, trimmed)
        } else {
            geocodeBlocking(geocoder, trimmed)
        }
    }

    private suspend fun geocodeAsync(geocoder: Geocoder, query: String): GpsLocation? =
        suspendCancellableCoroutine { cont ->
            try {
                geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        cont.resume(addresses.firstOrNull()?.toGps())
                    }

                    override fun onError(errorMessage: String?) {
                        Log.w("GeocodingService", "geocode error: $errorMessage")
                        cont.resume(null)
                    }
                })
            } catch (e: Exception) {
                Log.w("GeocodingService", "geocodeAsync failed", e)
                cont.resume(null)
            }
        }

    @Suppress("DEPRECATION")
    private suspend fun geocodeBlocking(geocoder: Geocoder, query: String): GpsLocation? =
        withContext(Dispatchers.IO) {
            try {
                geocoder.getFromLocationName(query, 1)?.firstOrNull()?.toGps()
            } catch (e: Exception) {
                Log.w("GeocodingService", "geocodeBlocking failed", e)
                null
            }
        }

    private fun Address.toGps() = GpsLocation(latitude = latitude, longitude = longitude)
}
