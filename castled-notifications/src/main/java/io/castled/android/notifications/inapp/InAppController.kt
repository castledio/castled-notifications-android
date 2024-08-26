package io.castled.android.notifications.inapp

import android.app.Activity
import android.content.Context
import io.castled.android.notifications.R
import io.castled.android.notifications.inapp.CampaignResponseConverter.toCampaign
import io.castled.android.notifications.inapp.models.InAppDisplayState
import io.castled.android.notifications.inapp.service.InAppRepository
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.trigger.EventFilterEvaluator
import io.castled.android.notifications.trigger.enums.JoinType
import io.castled.android.notifications.trigger.models.GroupFilter
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicLong

internal class InAppController(context: Context) {

    private val inAppRepository = InAppRepository(context)
    private val logger = CastledLogger.getInstance(LogTags.IN_APP_TRIGGER)
    private var currentInAppBeingDisplayed: Campaign? = null
    private val currentInAppLock = Any()
    private val inAppViewLifecycleListener = InAppLifeCycleListenerImpl(this)
    private var inAppViewDecorator: InAppViewDecorator? = null
    private var pendingInApps = mutableListOf<Campaign>()
    private val pendingInAppMutex = Mutex()
    private var lastDisplayTs = AtomicLong(0L)
    private val lastDisplayTsMutex = Mutex()
    private val excludedActivities: List<String> by lazy {
        try {
            context.getString(R.string.io_castled_inapp_excluded_activities).split(",")
        } catch (e: Exception) {
            emptyList()
        }
    }
    internal var currentActivityReference: WeakReference<Activity>? = null

    suspend fun refreshLiveCampaigns() {
        val liveCampaignResponse = inAppRepository.fetchLiveCampaigns() ?: return
        val liveCampaigns = liveCampaignResponse.map { it.toCampaign() }
        val cachedCampaigns = inAppRepository.getCampaigns()

        val cachedCampaignsMapById = cachedCampaigns.associateBy { it.notificationId }
        val liveCampaignsMapById = liveCampaigns.associateBy { it.notificationId }

        val expiredCampaigns =
            cachedCampaigns.filterNot { liveCampaignsMapById.containsKey(it.notificationId) }
        val newCampaigns =
            liveCampaigns.filterNot { cachedCampaignsMapById.containsKey(it.notificationId) }

        inAppRepository.insertCampaignsIntoDb(newCampaigns)
        inAppRepository.deleteDbCampaigns(expiredCampaigns)
    }

    suspend fun reportEvent(request: CastledInAppEventRequest) =
        inAppRepository.reportEvent(request)

    internal suspend fun findAndLaunchInApp(
        context: Context,
        eventName: String,
        params: Map<String, Any?>?
    ) {
        logger.debug("Looking for in-app with trigger condition '$eventName'")
        val inAppCampaigns = inAppRepository.getCampaigns()
        setLastInAppDisplayTs(
            inAppCampaigns.maxByOrNull { it.lastDisplayedTime }?.lastDisplayedTime ?: 0L
        )
        val triggeredInApps = findTriggeredInApp(inAppCampaigns, eventName, params)
        if (triggeredInApps.isEmpty()) {
            logger.debug("No in-apps found with trigger condition $eventName. Checking for any pending in-apps")
            triggerPendingNotificationsIfAny()
        } else {
            logger.debug("${triggeredInApps.count()} in-apps found. Validating...")
            validateAndDisplayInApp(triggeredInApps)
        }
    }

    private fun isSatisfiedWithGlobalIntervalBtwDisplays(
        campaign: Campaign
    ): Boolean {
        return (campaign.displayConfig.minIntervalBtwDisplaysGlobal == 0L ||
                campaign.displayConfig.minIntervalBtwDisplaysGlobal * 1000 <= System.currentTimeMillis() - lastDisplayTs.get())
    }

    private suspend fun validateAndDisplayInApp(triggeredInApps: List<Campaign>) {
        if (currentActivityReference == null || currentActivityReference!!.get() == null) {
            enqueuePendingItems(triggeredInApps)
            logger.error("currentActivityReference is null!")
            return
        }
        val currentActivity = currentActivityReference!!.get()!!
        val inAppToShow =
            triggeredInApps.firstOrNull {
                isSatisfiedWithGlobalIntervalBtwDisplays(it) &&
                        canShowInActivity(currentActivity.javaClass.simpleName)
            }
        inAppToShow?.let {
            if (checkAndUpdateCurrentInApp(it)) {
                launchInApp(currentActivity, it)
                removeCampaignFromPendingItems(it)
                setLastInAppDisplayTs(System.currentTimeMillis())
                enqueuePendingItems(triggeredInApps.filter { inApp -> it.notificationId != inApp.notificationId })
            } else {
                enqueuePendingItems(triggeredInApps)
                logger.debug("Skipping in-app display. Another currently being shown/ in-app display state is not active")
            }
        } ?: run {
            enqueuePendingItems(triggeredInApps)
            logger.debug("No in-apps to show from the triggered list")
        }
    }

    private suspend fun enqueuePendingItems(items: List<Campaign>) {
        pendingInAppMutex.withLock {
            pendingInApps.addAll(items.filter { newItem ->
                pendingInApps.none { existingItem -> existingItem.notificationId == newItem.notificationId }
            })
        }
    }

    private suspend fun removeCampaignFromPendingItems(triggeredInApp: Campaign) {
        pendingInAppMutex.withLock {
            pendingInApps.removeIf { it.notificationId == triggeredInApp.notificationId }
        }
    }

    private suspend fun setLastInAppDisplayTs(ts: Long) {
        lastDisplayTsMutex.withLock {
            if (ts > lastDisplayTs.get()) {
                lastDisplayTs.set(ts)
            }
        }
    }

    internal fun getPendingListItems(): List<Campaign> {
        return ArrayList(pendingInApps)
    }

    private fun canShowInActivity(currentActivityName: String): Boolean {
        if (excludedActivities.contains(currentActivityName)) {
            logger.debug("Unable to display the in-app as $currentActivityName is excluded")
            return false
        }
        return true
    }

    suspend fun triggerPendingNotificationsIfAny() {
        if (pendingInApps.isNotEmpty()) {
            validateAndDisplayInApp(getPendingListItems())
        } else {
            logger.debug("No pending in-apps found.")
        }
    }

    fun clearCurrentInApp() {
        synchronized(currentInAppLock) {
            currentInAppBeingDisplayed = null
            inAppViewDecorator = null
        }
    }

    private fun checkAndUpdateCurrentInApp(inApp: Campaign): Boolean {
        if (currentInAppBeingDisplayed != null ||
            InAppNotification.inAppDisplayState != InAppDisplayState.ACTIVE
        ) {
            return false
        }
        synchronized(currentInAppLock) {
            if (currentInAppBeingDisplayed == null) {
                currentInAppBeingDisplayed = inApp
                return true
            }
        }
        return false
    }

    private fun getEventFilter(campaign: Campaign): GroupFilter {
        return try {
            return Json.decodeFromJsonElement(campaign.trigger["eventFilter"] as JsonElement)
        } catch (e: Exception) {
            //logger.error("Couldn't deserialize event filter!", e)
            GroupFilter(JoinType.AND, null)
        }
    }

    private fun findTriggeredInApp(
        inAppCampaigns: List<Campaign>, eventName: String, params: Map<String, Any?>?
    ): List<Campaign> {
        val triggeredInApp = inAppCampaigns.filter {
            // Trigger params filter
            ((it.trigger["eventName"] as JsonPrimitive?)?.content == eventName) && EventFilterEvaluator.evaluate(
                getEventFilter(it), params
            )
        }.filter {
            // Display config filter
            it.timesDisplayed < it.displayConfig.displayLimit && (it.displayConfig.minIntervalBtwDisplays == 0L
                    || it.displayConfig.minIntervalBtwDisplays * 1000 <= System.currentTimeMillis() - it.lastDisplayedTime)
        }.filter {
            // Exclude the currentCampaign
            it.notificationId != currentInAppBeingDisplayed?.notificationId
        }.sortedByDescending { it.priority }
        return triggeredInApp
    }

    private suspend fun launchInApp(
        context: Context, inAppSelectedForDisplay: Campaign
    ) = withContext(Main) {
        try {
            inAppViewDecorator =
                InAppViewDecorator(context, inAppSelectedForDisplay, inAppViewLifecycleListener)
            inAppViewDecorator?.show(true)
        } catch (e: Exception) {
            logger.error("In-app display failed!", e)
        }
    }

    suspend fun updateInAppDisplayStats(inApp: Campaign) {
        inAppRepository.updateCampaignDisplayStats(inApp)
    }

    fun updateInAppForOrientationChanges(context: Context) {
        if (inAppViewDecorator != null) {
            return
        }
        currentInAppBeingDisplayed?.let {
            try {
                inAppViewDecorator = InAppViewDecorator(
                    context, it, inAppViewLifecycleListener
                )
                inAppViewDecorator?.show(false)
            } catch (e: Exception) {
                logger.error("In-app display failed after orientation!", e)
                clearCurrentInApp()
            }
        }
    }

    fun dismissDialogIfAny() {
        currentInAppBeingDisplayed?.let {
            try {
                inAppViewDecorator?.dismissDialog()
                inAppViewDecorator = null
            } catch (e: Exception) {
                logger.error("In-app dismiss failed!", e)
            }
        }
    }
}
