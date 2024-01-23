package io.castled.android.notifications.observer

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

}