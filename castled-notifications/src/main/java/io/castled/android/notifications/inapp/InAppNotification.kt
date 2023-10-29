package io.castled.android.notifications.inapp

import android.app.Activity
import android.app.Application
import android.content.Context
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.observer.CastledLifeCycleObserver
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

internal object InAppNotification : CastledSharedStoreListener {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.IN_APP)
    private lateinit var externalScope: CoroutineScope
    private lateinit var inAppController: InAppController

    private var enabled = false
    private var fetchJob: Job? = null

    fun init(application: Application, externalScope: CoroutineScope) {
        InAppNotification.externalScope = externalScope
        inAppController = InAppController(application)
        CastledLifeCycleObserver.registerListener(InAppAppLifeCycleListener(externalScope))
        enabled = true
    }

    private fun startCampaignJob() {
        if (!enabled) {
            logger.debug("Ignoring app event, In-App disabled")
            return
        }
        if (fetchJob == null || !fetchJob!!.isActive) {
            fetchJob = externalScope.launch(Default) {
                do {
                    delay(TimeUnit.SECONDS.toMillis(CastledSharedStore.configs.inAppFetchIntervalSec))
                    if (!CastledSharedStore.isAppInBackground) {
                        inAppController.refreshLiveCampaigns()
                    }
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

    fun dismissInAppDialogsIfAny() = inAppController.dismissDialogIfAny()

    suspend fun refreshCampaigns() = inAppController.refreshLiveCampaigns()

    fun onOrientationChange(activity: Activity) =
        inAppController.updateInAppForOrientationChanges(activity)

    override fun onStoreInitialized(context: Context) {
        CastledSharedStore.getUserId()?.let {
            startCampaignJob()
        }
    }

    override fun onStoreUserIdSet(context: Context) {
        startCampaignJob()
    }

}