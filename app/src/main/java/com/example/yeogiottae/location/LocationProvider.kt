package com.example.yeogiottae.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

sealed interface LocationResult {
    data class Success(val coordinate: LocationCoordinate) : LocationResult
    data object PermissionDenied : LocationResult
    data object Unknown : LocationResult
}

data class LocationCoordinate(
    val latitude: Double,
    val longitude: Double
)

interface LocationProvider {
    fun observeLocation(): Flow<LocationResult>
}

class FusedLocationProvider(private val context: Context) : LocationProvider {

    @SuppressLint("MissingPermission")
    override fun observeLocation(): Flow<LocationResult> = callbackFlow {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val locationTask = client.lastLocation
        locationTask.addOnSuccessListener { location ->
            if (location != null) {
                trySend(
                    LocationResult.Success(
                        LocationCoordinate(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                )
            } else {
                trySend(LocationResult.Unknown)
            }
            close()
        }
        locationTask.addOnFailureListener {
            trySend(LocationResult.Unknown)
            close()
        }
        awaitClose {}
    }
}
