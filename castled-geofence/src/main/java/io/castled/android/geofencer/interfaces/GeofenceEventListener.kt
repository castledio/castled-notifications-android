package interfaces

import android.location.Location
import io.castled.android.geofencer.models.GeofenceData

interface GeofenceEventListener {
    fun onGeofenceEnter(geofenceData: GeofenceData)
    fun onGeofenceExit(geofenceData: GeofenceData)
    fun onLocationUpdate(location: Location)
    fun onGeofenceReEnter(geofenceData: GeofenceData) {
        // Optional: Handle re-entry logic here if needed
    }
}