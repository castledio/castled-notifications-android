package io.castled.android.geofencer.models

import com.google.android.gms.location.LocationRequest

data class GeofenceConfig(
    val locationAccuracy: Int = LocationRequest.PRIORITY_HIGH_ACCURACY,
    val fetchMode: Int = LocationRequest.PRIORITY_HIGH_ACCURACY,
    val updateInterval: Long = 10000L, // in milliseconds
    val displacement: Float = 100f // in meters
) {
    class Builder {
        private var locationAccuracy: Int = LocationRequest.PRIORITY_HIGH_ACCURACY
        private var fetchMode: Int = LocationRequest.PRIORITY_HIGH_ACCURACY
        private var updateInterval: Long = 10000L
        private var displacement: Float = 100f

        fun setLocationAccuracy(accuracy: Int) = apply {
            this.locationAccuracy = accuracy
        }

        fun setFetchMode(fetchMode: Int) = apply {
            this.fetchMode = fetchMode
        }

        fun setUpdateInterval(interval: Long) = apply {
            this.updateInterval = interval
        }

        fun setDisplacement(displacement: Float) = apply {
            this.displacement = displacement
        }

        fun build(): GeofenceConfig {
            return GeofenceConfig(
                locationAccuracy = locationAccuracy,
                fetchMode = fetchMode,
                updateInterval = updateInterval,
                displacement = displacement
            )
        }
    }
}