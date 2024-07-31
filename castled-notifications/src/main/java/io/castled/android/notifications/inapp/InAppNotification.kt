package io.castled.android.notifications.inapp

import android.app.Activity
import android.app.Application
import android.content.Context
import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.commons.extenstions.toCastledActionContext
import io.castled.android.notifications.inapp.models.InAppDisplayState
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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

internal object InAppNotification : CastledSharedStoreListener {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.IN_APP)
    private lateinit var externalScope: CoroutineScope
    private lateinit var inAppController: InAppController
    internal var inAppDisplayState = InAppDisplayState.ACTIVE

    private var enabled = false
    private var fetchJob: Job? = null
    private var inAppNotificationListener: CastledInappNotificationListener? = null

    fun init(application: Application, externalScope: CoroutineScope) {
        InAppNotification.externalScope = externalScope
        inAppController = InAppController(application)
        CastledSharedStore.registerListener(this)
        CastledLifeCycleObserver.registerListener(InAppAppLifeCycleListener(externalScope))
        enabled = true
        logger.debug("InApp module initialized")
    }

    private fun startCampaignJob() {
        if (!enabled) {
            logger.debug("Ignoring app event, In-App disabled")
            return
        }
        if (fetchJob == null || !fetchJob!!.isActive) {
            fetchJob = externalScope.launch(Default) {
                do {
                    if (!CastledSharedStore.isAppInBackground) {
                        inAppController.refreshLiveCampaigns()
                    }
                    delay(TimeUnit.SECONDS.toMillis(CastledSharedStore.configs.inAppFetchIntervalSec))
                } while (true)
            }
        }
    }

    internal suspend fun cancelCampaignJob() {
        if (fetchJob != null && fetchJob!!.isActive) {
            fetchJob!!.cancelAndJoin()
        }
    }

    internal fun logAppEvent(
        context: Context,
        eventName: String,
        eventParams: Map<String, Any?>?
    ) = externalScope.launch(Default) {
        if (!enabled || inAppDisplayState == InAppDisplayState.STOPPED) {
            logger.debug("Ignoring in-app event, In-App disabled/ display state is 'stopped'")
            return@launch
        } else if (CastledSharedStore.getUserId().isNullOrBlank()) {
            logger.debug("Ignoring in-app event, UserId not set yet!")
            return@launch
        }
        inAppController.findAndLaunchInApp(context, eventName, eventParams)
    }

    fun reportInAppEvent(request: CastledInAppEventRequest, actionParams: ClickActionParams?) =
        externalScope.launch(Default) {
            inAppController.reportEvent(request)
            inAppNotificationListener?.let { listener ->
                actionParams?.let {
                    listener.onCastledInappClicked(actionParams.toCastledActionContext())
                }
            }
        }

    fun updateInAppDisplayStats(inApp: Campaign) = externalScope.launch(Default) {
        inAppController.updateInAppDisplayStats(inApp)
    }

    fun dismissInAppDialogsIfAny() = inAppController.dismissDialogIfAny()

    fun checkPendingNotificationsIfAny() = externalScope.launch(Default) {
        inAppController.triggerPendingNotificationsIfAny()
    }

    suspend fun refreshCampaigns() {
        CastledSharedStore.getUserId()?.let {
            inAppController.refreshLiveCampaigns()
        }
    }

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

    internal fun setCurrentActivity(activity: Activity) {
        inAppController.currentActivityReference = WeakReference(activity)
    }

    internal fun clearCurrentActivity(activity: Activity) {
        inAppController.currentActivityReference?.takeIf { it.get() == activity }?.clear()
    }

    internal fun getCurrentActivity(): Activity? {
        return inAppController.currentActivityReference?.get()
    }

    fun subscribeToInappEvents(listener: CastledInappNotificationListener) {
        if (!enabled) {
            logger.debug("Ignoring inapp listener, In-App disabled")
            return
        }
        inAppNotificationListener = listener
    }

    internal fun pauseInApp() {
        if (!enabled) {
            logger.debug("Ignoring inapp display state, In-App disabled")
            return
        }
        inAppDisplayState = InAppDisplayState.PAUSED
        logger.debug(
            "In-app state changed to ‘paused’, no more in-app notifications will be " +
                    "displayed until ‘resumeInApp’ is called."
        )
    }

    internal fun stopInApp() {
        if (!enabled) {
            logger.debug("Ignoring inapp display state, In-App disabled")
            return
        }
        inAppDisplayState = InAppDisplayState.STOPPED
        logger.debug(
            "In-app state changed to ‘stopped’, no more in-app notifications will be " +
                    "evaluated/displayed until ‘resumeInApp’ is called."
        )
    }

    internal fun resumeInApp() {
        if (!enabled) {
            logger.debug("Ignoring inapp display state, In-App disabled")
            return
        }
        inAppDisplayState = InAppDisplayState.ACTIVE
        CastledSharedStore.getUserId()?.let {
            checkPendingNotificationsIfAny()
        }
        logger.debug(
            "In-app state changed to ‘active’."
        )
    }
}