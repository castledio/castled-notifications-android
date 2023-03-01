package io.castled.inAppTriggerEvents.observer

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.castled.notifications.logger.CastledLogger

private const val TAG = "FragmentLifeCycleObserv"
class ScreenLifeCycleObserver(val screenName: String): LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                CastledLogger.getInstance().debug("$TAG: on create: $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_START -> {
                CastledLogger.getInstance().debug("$TAG: on start $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_RESUME -> {
                CastledLogger.getInstance().debug("$TAG: on resume $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_PAUSE -> {
                CastledLogger.getInstance().debug("$TAG: on pause $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_STOP -> {
                CastledLogger.getInstance().debug("$TAG: on stop $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_DESTROY -> {
                CastledLogger.getInstance().debug("$TAG: on destroy $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_ANY -> {
                CastledLogger.getInstance().debug("$TAG: on any $source, ${source.lifecycle.currentState.name}")
            }
        }
    }
}