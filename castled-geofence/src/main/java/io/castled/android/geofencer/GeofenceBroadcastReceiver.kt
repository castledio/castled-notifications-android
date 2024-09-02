import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            geofencingEvent?.let { event ->
                handleGeofenceEvent(geofencingEvent, context)

            } ?: run {
                Log.e("GeofenceReceiver", "GeofencingEvent is null")
            }
        }
    }

    fun handleGeofenceEvent(geofencingEvent: GeofencingEvent?, context: Context) {
        // val geofenceManager = GeofenceManagerImpl.getInstance(context)
        geofencingEvent?.let { event ->
            if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
            ) {

                when (val transitionType = event.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        // Handle geofence enter transition
                        Log.d("GeofenceReceiver", "Geofence entered")
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        // Handle geofence exit transition
                        Log.d("GeofenceReceiver", "Geofence exited")
                    }

                    else -> {
                        Log.e(
                            "GeofenceReceiver",
                            "Unknown geofence transition: $transitionType"
                        )
                    }
                }
            } else {
                Log.e(
                    "GeofenceReceiver",
                    "Invalid geofence transition type: ${event.geofenceTransition}"
                )
            }
        }
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT =
            "com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_RECEIVED"
    }
}