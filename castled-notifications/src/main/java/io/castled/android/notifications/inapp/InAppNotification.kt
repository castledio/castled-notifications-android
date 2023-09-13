package io.castled.android.notifications.inapp

import android.app.Application
import android.content.Context
import io.castled.android.notifications.inapp.observer.AppActivityLifecycleObserver
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import java.util.concurrent.TimeUnit

internal object InAppNotification {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.IN_APP)
    private lateinit var externalScope: CoroutineScope
    private lateinit var inAppController: InAppController

    private var enabled = false
    private var fetchJob: Job? = null

    fun init(application: Application, externalScope: CoroutineScope) {
        InAppNotification.externalScope = externalScope
        inAppController = InAppController(application)
        application.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
        // observeAppLifecycle(application)
        enabled = true
    }

    fun startCampaignJob() {
        if (fetchJob == null || !fetchJob!!.isActive) {
            externalScope.launch(Default) {
                do {
                    logger.verbose("Syncing in-apps...")
                    inAppController.refreshLiveCampaigns()
                    delay(TimeUnit.SECONDS.toMillis(CastledSharedStore.configs.inAppFetchIntervalSec))
                } while (true)
            }
        }
    }

    internal suspend fun logAppEvent(
        context: Context,
        eventName: String,
        eventParams: Map<String, Any>?
    ) {
        if (!enabled) {
            logger.debug("Ignoring app event, In-App disabled")
        }
        inAppController.findAndLaunchInApp(context, eventName, eventParams)
    }

    fun reportInAppEvent(request: CastledInAppEventRequest) = externalScope.launch(Default) {
        inAppController.reportEvent(request)
    }
}