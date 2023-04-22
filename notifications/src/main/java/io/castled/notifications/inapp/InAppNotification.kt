package io.castled.notifications.inapp

import android.app.Application
import android.content.Context
import io.castled.notifications.inapp.observer.AppActivityLifecycleObserver
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.store.CastledSharedStore
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import java.util.concurrent.TimeUnit

internal object InAppNotification {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.IN_APP)
    private lateinit var externalScope: CoroutineScope
    private lateinit var inAppController: InAppController
    private var enabled = false
    private var fetchJob: Job? = null

    internal fun init(application: Application, externalScope: CoroutineScope) {
        this.externalScope = externalScope
        this.inAppController = InAppController(application)
        application.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
        // observeAppLifecycle(application)
        this.enabled = true
    }

    internal fun startCampaignJob() {
        if (fetchJob == null || !fetchJob!!.isActive) {
            externalScope.launch(Default) {
                do {
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
}