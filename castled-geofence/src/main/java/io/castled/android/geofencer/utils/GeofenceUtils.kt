package io.castled.android.geofencer.utils

import io.castled.android.geofencer.models.GeofenceData

internal object GeofenceUtils {
    fun getGeofenceData(apiResponse: List<Map<String, Any>>): List<GeofenceData> {
        val geofenceList = mutableListOf<GeofenceData>()
        try {
            for (item in apiResponse) {
                val id = item["id"] as? String ?: throw IllegalArgumentException("Invalid ID")
                val latitude =
                    item["lat"] as? Double ?: throw IllegalArgumentException("Invalid Latitude")
                val longitude =
                    item["long"] as? Double ?: throw IllegalArgumentException("Invalid Longitude")
                val radius = (item["radius"] as? Number)?.toFloat()
                    ?: throw IllegalArgumentException("Invalid Radius")

                val geofenceData = GeofenceData(id, latitude, longitude, radius)
                geofenceList.add(geofenceData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error accordingly, perhaps return an empty list or log the error
        }
        return geofenceList
    }
}