package cy.org.rise.obsai.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import cy.org.rise.obsai.utils.TAG

/**
 * Class that provides location data as LiveData. Tracking is only active as long as there is an
 * observer, when it becomes inactive the tracking stops.
 *
 * For getting location, see [documentation](https://developer.android.com/training/location).
 *
 * Based on [this example](https://git.io/JJ0Ev).
 *
 * @param context application context
 */
class LocationLiveData(context: Context) : LiveData<Location>() {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        Log.d(TAG(), "location tracking started")
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
        Log.d(TAG(), "location tracking ended")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let { setLocation(it.lastLocation) }
        }
    }

    private fun setLocation(location: Location) {
        Log.d(TAG(), "location set to [${location.latitude}, ${location.longitude}]")
        value = location
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 // 5s
            fastestInterval = 1000 // 1s
//            smallestDisplacement = 2f // 2m
        }
    }
}
