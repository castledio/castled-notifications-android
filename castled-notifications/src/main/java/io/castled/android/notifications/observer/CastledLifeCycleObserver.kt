package io.castled.android.notifications.observer

import android.app.Application
import android.content.Context
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener

internal object CastledLifeCycleObserver : CastledSharedStoreListener {

    private val lifeCycleListeners = mutableListOf<CastledAppLifeCycleListener>()
    private lateinit var application: Application
    private var registered = false

    private val logger = CastledLogger.getInstance(
        LogTags.ALC_OBS
    )

    fun init(application: Application) {
        this.application = application
    }

    @Synchronized
    private fun startLifeCycleListeners() {
        if (!registered && (CastledSharedStore.configs.enableAppInbox || CastledSharedStore.configs.enableInApp)) {
            logger.debug("Starting lifecycle listeners...")
            application.registerActivityLifecycleCallbacks(
                CastledActivityLifeCycleCallbacksImpl(
                    lifeCycleListeners
                )
            )
        }
        registered = true
    }

    fun registerListener(listener: CastledAppLifeCycleListener) {
        this.lifeCycleListeners.add(listener)
    }

    override fun onStoreInitialized(context: Context) {
        CastledSharedStore.getUserId()?.let { startLifeCycleListeners() }
    }

    override fun onStoreUserIdSet(context: Context) {
        startLifeCycleListeners()
    }

}