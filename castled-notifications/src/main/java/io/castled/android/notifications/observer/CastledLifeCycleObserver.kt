package io.castled.android.notifications.observer

import android.app.Application
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore

internal object CastledLifeCycleObserver {

    private val lifeCycleListeners = mutableListOf<CastledAppLifeCycleListener>()
    private var started = false

    private val logger = CastledLogger.getInstance(
        LogTags.ALC_OBS
    )

    fun start(application: Application) {
        if (!started && (CastledSharedStore.configs.enableAppInbox || CastledSharedStore.configs.enableInApp)) {
            logger.debug("Starting lifecycle listeners...")
            application.registerActivityLifecycleCallbacks(
                CastledActivityLifeCycleCallbacksImpl(
                    lifeCycleListeners
                )
            )
        }
        started = true
    }

    fun registerListener(listener: CastledAppLifeCycleListener) {
        this.lifeCycleListeners.add(listener)
    }

}