package io.castled.inAppTriggerEvents.observer

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.castled.inAppTriggerEvents.trigger.TriggerEvent

private const val TAG = "FragmentLifeCycleObserv"
class ScreenLifeCycleObserver(val screenName: String): LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                Log.d(TAG, "on create: $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_START -> {
                Log.d(TAG, "on start $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_RESUME -> {
                Log.d(TAG, "on resume $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_PAUSE -> {
                Log.d(TAG, "on pause $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_STOP -> {
                Log.d(TAG, "on stop $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_DESTROY -> {
                Log.d(TAG, "on destroy $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_ANY -> {
                Log.d(TAG, "on any $source, ${source.lifecycle.currentState.name}")
            }
        }
    }
}