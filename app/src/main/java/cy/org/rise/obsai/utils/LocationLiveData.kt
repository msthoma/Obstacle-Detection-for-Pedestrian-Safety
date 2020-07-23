package cy.org.rise.obsai.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationLiveData(context: Context) : LiveData<Location>() {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient.apply {
            // first get last known location
            lastLocation.addOnSuccessListener { location ->
                location?.let { setLocation(it) }
            }

            // also subscribe to location updates
            requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let { setLocation(it.lastLocation) }
        }
    }

    private fun setLocation(location: Location) {
        value = location
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 20000 // 20s
            fastestInterval = 10000 // 10s
            smallestDisplacement = 2f // 2m
        }
    }
}
