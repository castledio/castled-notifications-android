package io.castled.android.notifications.inapp

import android.app.Activity
import android.app.Application
import android.content.Context
import io.castled.android.notifications.inapp.models.consts.AppEvents
import io.castled.android.notifications.inapp.observer.AppActivityLifecycleObserver
import io.castled.android.notifications.inapp.observer.AppEventCallbacks
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

internal object InAppNotification {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.IN_APP)
    private lateinit var externalScope: CoroutineScope
    private lateinit var inAppController: InAppController

    private var enabled = false
    private var fetchJob: Job? = null

    private val appEventCallbacks = object : AppEventCallbacks {
        override fun onAppMovedToForeground(activity: Activity) {
            logAppEvent(activity, AppEvents.APP_OPENED, null)
            CastledSharedStore.isAppInBackground = true
        }

        override fun onActivityStarted(activity: Activity) {
            logAppEvent(
                activity,
                AppEvents.APP_PAGE_VIEWED,
                mapOf("name" to activity.componentName.shortClassName.drop(1))
            )
        }

        override fun onAppMovedToBackground(activity: Activity) {
            CastledSharedStore.isAppInBackground = false
        }
    }

    fun init(application: Application, externalScope: CoroutineScope) {
        InAppNotification.externalScope = externalScope
        inAppController = InAppController(application)
        application.registerActivityLifecycleCallbacks(
            AppActivityLifecycleObserver(
                appEventCallbacks
            )
        )
        // observeAppLifecycle(application)
        enabled = true
        CastledSharedStore.getUserId()?.let {
            startCampaignJob()
        }

    }

    fun startCampaignJob() {
        if (!enabled) {
            logger.debug("Ignoring app event, In-App disabled")
            return
        }
        if (fetchJob == null || !fetchJob!!.isActive) {
            fetchJob = externalScope.launch(Default) {
                do {
                    logger.verbose("Syncing in-apps...")
                    inAppController.refreshLiveCampaigns()
                    delay(TimeUnit.SECONDS.toMillis(CastledSharedStore.configs.inAppFetchIntervalSec))
                } while (true)
            }
        }
    }

    internal fun logAppEvent(
        context: Context,
        eventName: String,
        eventParams: Map<String, Any>?
    ) = externalScope.launch(Default) {
        if (!enabled) {
            logger.debug("Ignoring app event, In-App disabled")
            return@launch
        }
        inAppController.findAndLaunchInApp(context, eventName, eventParams)
    }

    fun reportInAppEvent(request: CastledInAppEventRequest) = externalScope.launch(Default) {
        inAppController.reportEvent(request)
    }

    fun updateInAppDisplayStats(inApp: Campaign) = externalScope.launch(Default) {
        inAppController.updateInAppDisplayStats(inApp)
    }
}