package com.example.yeogiottae.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
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
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFinePermission && !hasCoarsePermission) {
            trySend(LocationResult.PermissionDenied)
            close()
            awaitClose {}
            return@callbackFlow
        }

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
