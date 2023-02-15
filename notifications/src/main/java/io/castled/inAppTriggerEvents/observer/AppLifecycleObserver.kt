package io.castled.inAppTriggerEvents.observer

import android.content.Context
import android.util.Log
import android.util.Log.d
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.castled.inAppTriggerEvents.event.EventNotification
import io.castled.inAppTriggerEvents.trigger.TriggerEvent

private const val TAG = "AppLifecycleObserver"
internal class AppLifecycleObserver(val context: Context, val screenName: String): LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                d(TAG, "on create: $source, ${source.lifecycle.currentState}" )
            }
            Lifecycle.Event.ON_START -> {
                d(TAG, "on start $source, ${source.lifecycle.currentState}" )
            }
            Lifecycle.Event.ON_RESUME -> {
                d(TAG, "on resume $source, ${source.lifecycle.currentState}" )

                EventNotification.getInstance().logAppOpenEvent(context)
                EventNotification.getInstance().logPageViewedEvent(context, screenName)
                // The below line is to just test Db events.
//                TriggerEvent.getInstance().findAndLaunchDbTriggerEvent(context)

                //The below line need to enable if event need to trigger on Activity.
//                TriggerEvent.getInstance().findAndLaunchEvent(context, screenName) { events ->
//                    Log.d(TAG, "=>>(Size: ${events.size})")
//                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                d(TAG, "on pause $source, ${source.lifecycle.currentState}" )
            }
            Lifecycle.Event.ON_STOP -> {
                d(TAG, "on stop $source, ${source.lifecycle.currentState}" )
            }
            Lifecycle.Event.ON_DESTROY -> {
                d(TAG, "on destroy $source, ${source.lifecycle.currentState}" )
            }
            Lifecycle.Event.ON_ANY -> {
                d(TAG, "on any $source, ${source.lifecycle.currentState}" )
            }
        }
    }
}