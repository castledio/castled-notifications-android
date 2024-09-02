package io.castled.android.geofencer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

class LocationUpdateBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val locationResult = LocationResult.extractResult(intent)
        locationResult?.let {
            val location = it.lastLocation
            location?.let {
                Log.d(
                    "LocationReceiver",
                    "Location updated: ${location.latitude}, ${location.longitude}"
                )

            }
            // Handle location update here
        }
    }
}