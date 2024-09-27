package io.castled.android.notifications.observer

import android.app.Activity
import android.app.Application
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags

internal object CastledLifeCycleObserver {

    private val lifeCycleListeners = mutableListOf<CastledAppLifeCycleListener>()
    private var started = false

    private val logger = CastledLogger.getInstance(
        LogTags.ALC_OBS
    )

    @Synchronized
    fun start(application: Application) {
        if (started) {
            return
        }
        logger.debug("Starting lifecycle listeners...")
        application.registerActivityLifecycleCallbacks(
            CastledActivityLifeCycleCallbacksImpl(
                lifeCycleListeners
            )
        )
        started = true
    }

    @Synchronized
    fun registerListener(listener: CastledAppLifeCycleListener) {
        if (listener !in lifeCycleListeners) {
            this.lifeCycleListeners.add(listener)
        }
    }

    fun onNonNativeAppForegrounded(activity: Activity) {
        lifeCycleListeners.forEach {
            it.onActivityStarted(
                activity,
                false
            )
        }
        lifeCycleListeners.forEach { it.onAppMovedToForeground(activity) }
    }


}