package interfaces

import io.castled.android.geofencer.models.GeofenceConfig

interface GeofenceManager {
    fun init(config: GeofenceConfig)
    fun startMonitoring()
    fun stopMonitoring()
    fun setGeofenceEventListener(listener: GeofenceEventListener)
}