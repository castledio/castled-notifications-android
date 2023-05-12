package io.castled.android.notifications.inapp.observer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags

class FragmentLifeCycleObserver(val context: Context, val screenName: String) :
    LifecycleEventObserver {

    private val logger = CastledLogger.getInstance(
        LogTags.FLC_OBS)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                logger.debug("on create: $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_START -> {
                logger.debug("on start $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_RESUME -> {
                logger.debug("on resume $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_PAUSE -> {
                logger.debug("on pause $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_STOP -> {
                logger.debug("on stop $source, ${source.lifecycle.currentState}")
            }
            Lifecycle.Event.ON_DESTROY -> {
                logger.debug("on destroy $source, ${source.lifecycle.currentState.name}")
            }
            Lifecycle.Event.ON_ANY -> {
                logger.debug("on any $source, ${source.lifecycle.currentState.name}")
            }
        }
    }
}