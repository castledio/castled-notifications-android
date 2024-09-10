import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.castled.android.geofencer.GeofenceManagerImpl

class GeofenceBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val geofenceManager = GeofenceManagerImpl.getInstance(context)
            geofenceManager.startMonitoring()
        }
    }
}