package io.castled.android.notifications.inapp

import android.app.Activity
import android.content.Context
import io.castled.android.notifications.R
import io.castled.android.notifications.inapp.CampaignResponseConverter.toCampaign
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


internal class InAppController(context: Context) {

    private val inAppRepository = InAppRepository(context)
    private val logger = CastledLogger.getInstance(LogTags.IN_APP_TRIGGER)
    private var currentInAppBeingDisplayed: Campaign? = null
    private val inAppViewLifecycleListener = InAppLifeCycleListenerImpl(this)
    private var inAppViewDecorator: InAppViewDecorator? = null
    private var pendingInapps = mutableListOf<Campaign>()
    private val inappMutex = Mutex()
    private val currentInAppLock = Any()
    internal var currentActivityReference: WeakReference<Activity>? = null
    private val excludedActivities: List<String> by lazy {
        try {
            context.getString(R.string.io_castled_inapp_excluded_activities)!!.split(",")
        } catch (e: Exception) {
            emptyList()
        }
    }

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
        if (currentInAppBeingDisplayed != null) {
            return
        }
        findTriggeredInApp(eventName, params)?.let { validateInappsBeforeDisplay(it) }
            ?: run { triggerPendingNotificationsIfAny() }
    }

    private fun isSatisfiedWithGlobalIntervalBtwDisplays(
        campaign: Campaign, inAppCampaigns: List<Campaign>
    ): Boolean {
        val latestCampaignViewTs =
            inAppCampaigns.maxByOrNull { it.lastDisplayedTime }?.lastDisplayedTime ?: 0
        return (campaign.displayConfig.minIntervalBtwDisplaysGlobal == 0L || campaign.displayConfig.minIntervalBtwDisplaysGlobal * 1000 <= System.currentTimeMillis() - latestCampaignViewTs)
    }

    private suspend fun validateInappsBeforeDisplay(inApps: List<Campaign>) {
        val inAppCampaigns = inAppRepository.getCampaigns()
        val triggeredInapps = inApps.toMutableList()
        triggeredInapps.indexOfFirst { item ->
            isSatisfiedWithGlobalIntervalBtwDisplays(item, inAppCampaigns) && canShowInActivity()
        }.takeIf { satisfiedIndex -> satisfiedIndex != -1 }?.let { satisfiedIndex ->
            val triggeredInApp = triggeredInapps[satisfiedIndex]
            try {
                currentActivityReference?.let {
                    it.get()?.let { activityContext ->
                        if (updateCurrentInApp(triggeredInApp)) {
                            launchInApp(activityContext, triggeredInApp)
                            removeCampaignFromPendingItems(triggeredInApp)
                            triggeredInapps.removeAt(satisfiedIndex)

                        } else {
                            logger.debug("Skipping in-app display. Another currently being shown")
                        }
                    }
                }

            } catch (e: Exception) {
                logger.error("In-app launch failed!", e)
            }

        }
        enqueuePendingItems(triggeredInapps)
    }

    private suspend fun enqueuePendingItems(items: List<Campaign>) {
        inappMutex.withLock {
            pendingInapps.addAll(items.filter { newItem ->
                pendingInapps.none { existingItem -> existingItem.notificationId == newItem.notificationId }
            })
        }
    }

    private suspend fun removeCampaignFromPendingItems(triggeredInApp: Campaign) {
        inappMutex.withLock {
            pendingInapps.removeIf { it.notificationId == triggeredInApp.notificationId }
        }
    }

    fun getPendingListItems(): List<Campaign> {
        return ArrayList(pendingInapps)
    }

    private fun canShowInActivity(): Boolean {
        currentActivityReference?.let {
            val activityName = it.get()?.componentName?.shortClassName?.drop(1)
            activityName?.let {
                return !excludedActivities.contains(activityName)
            }
        }
        return false
    }

    suspend fun triggerPendingNotificationsIfAny() {
        if (pendingInapps.isNotEmpty()) {
            validateInappsBeforeDisplay(getPendingListItems())
        }
    }

    fun clearCurrentInApp() {
        synchronized(currentInAppLock) {
            currentInAppBeingDisplayed = null
            inAppViewDecorator = null
        }
    }

    private fun updateCurrentInApp(inApp: Campaign): Boolean {
        if (currentInAppBeingDisplayed != null) {
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

    private suspend fun findTriggeredInApp(
        eventName: String, params: Map<String, Any?>?
    ): List<Campaign>? {
        val inAppCampaigns = inAppRepository.getCampaigns()
        val triggeredInApp = inAppCampaigns.filter {
            // Trigger params filter
            ((it.trigger["eventName"] as JsonPrimitive?)?.content == eventName) && EventFilterEvaluator.evaluate(
                getEventFilter(it), params
            )
        }.filter {
            // Display config filter
            it.timesDisplayed < it.displayConfig.displayLimit && (it.displayConfig.minIntervalBtwDisplays == 0L || it.displayConfig.minIntervalBtwDisplays * 1000 <= System.currentTimeMillis() - it.lastDisplayedTime)
        }.takeUnless { it.isEmpty() }?.sortedByDescending { it.priority }

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
