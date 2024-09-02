package io.castled.android.geofencer

import GeofenceBroadcastReceiver
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import interfaces.GeofenceEventListener
import interfaces.GeofenceManager
import io.castled.android.geofencer.models.GeofenceConfig
import io.castled.android.geofencer.models.GeofenceData
import io.castled.android.geofencer.utils.GeofenceUtils
import io.castled.android.geofencer.utils.PermissionValidator

class GeofenceManagerImpl private constructor(
    private val context: Context
) : GeofenceManager {

    private var config: GeofenceConfig? = null
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE
        )
    }

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, LocationUpdateBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Add FLAG_IMMUTABLE
        )
    }
    private val geofenceMap = mutableMapOf<String, Geofence>()
    private var eventListener: GeofenceEventListener? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: GeofenceManagerImpl? = null

        fun getInstance(context: Context): GeofenceManagerImpl {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GeofenceManagerImpl(context).also { INSTANCE = it }
            }
        }
    }

    override fun init(config: GeofenceConfig) {
        this.config = config
    }

    override fun setGeofenceEventListener(listener: GeofenceEventListener) {
        this.eventListener = listener
    }

    @SuppressLint("MissingPermission")
    private fun startMonitoringGeofences(geofenceDataList: List<GeofenceData>) {


        // Clear existing geofences
        stopMonitoring()

        // Add new geofences
        geofenceDataList.forEach { addGeofence(it) }

        if (geofenceMap.isEmpty()) {
            Log.e("GeofenceManager", "No geofences to monitor.")
            return
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceMap.values.toList())
            .build()

        // Add geofences if location permission is granted
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GeofenceManager", "Geofences added successfully.")
                    startLocationUpdates() // Start location updates only after geofences are successfully added
                } else {
                    Log.e("GeofenceManager", "Failed to add geofences.", task.exception)
                }
            }
    }

    fun addGeofence(geofenceData: GeofenceData) {
        val geofence = Geofence.Builder()
            .setRequestId(geofenceData.id)
            .setCircularRegion(geofenceData.latitude, geofenceData.longitude, geofenceData.radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        geofenceMap[geofenceData.id] = geofence
    }

    override fun startMonitoring() {
        if (!PermissionValidator.hasLocationPermission(context)) {
            Log.e("GeofenceManager", "Location permissions are not granted.")
            PermissionValidator.requestLocationPermission(context as Activity)
            return
        }

        val apiResponse: List<Map<String, Any>> = listOf(
            mapOf(
                "id" to "chrysalis",
                "lat" to 12.970323244842623,
                "long" to 77.74890921843459,
                "radius" to 1000
            ),
            mapOf(
                "id" to "kavalam",
                "lat" to 9.477801601203003,
                "long" to 76.46768721497936,
                "radius" to 1000
            )
            // Add more entries here
        )

        val geofenceDataList = GeofenceUtils.getGeofenceData(apiResponse)
        startMonitoringGeofences(geofenceDataList)
    }

    override fun stopMonitoring() {
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GeofenceManager", "Geofences removed successfully.")
                } else {
                    Log.e("GeofenceManager", "Failed to remove geofences.", task.exception)
                }
            }
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!PermissionValidator.hasLocationPermission(context)) {
            Log.e("GeofenceManager", "Location permissions are not granted.")
            return
        }

        val locationRequest = LocationRequest.Builder(
            config?.locationAccuracy ?: Priority.PRIORITY_HIGH_ACCURACY,
            config?.updateInterval ?: 10000L
        )
            .setMinUpdateIntervalMillis(5000L)
            .setMinUpdateDistanceMeters(config?.displacement ?: 10F)
            .build()

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationUpdatePendingIntent
        ).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("GeofenceManager", "Location updates started successfully.")
            } else {
                Log.e("GeofenceManager", "Failed to start location updates.", task.exception)
            }
        }
    }

    private fun stopLocationUpdates() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationProviderClient.removeLocationUpdates(geofencePendingIntent)
    }

}