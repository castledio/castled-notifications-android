package io.castled.android.notifications.inapp

import android.app.Activity
import android.content.Context
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
    private val currentInAppLock = Any()
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
        params: Map<String, Any>?
    ) {
        if (currentInAppBeingDisplayed != null) {
            return
        }
        val triggeredInApp = findTriggeredInApp(eventName, params) ?: return
        try {
            currentActivityReference?.let {
                it.get()?.let { activityContext ->
                    if (updateCurrentInApp(triggeredInApp)) {
                        launchInApp(activityContext, triggeredInApp)
                    } else {
                        logger.debug("Skipping in-app display. Another currently being shown")
                    }
                }
            }

        } catch (e: Exception) {
            logger.error("In-app launch failed!", e)
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
        eventName: String,
        params: Map<String, Any>?
    ): Campaign? {
        val inAppCampaigns = inAppRepository.getCampaigns()
        val latestCampaignViewTs =
            inAppCampaigns.maxByOrNull { it.lastDisplayedTime }?.lastDisplayedTime
                ?: 0

        val triggeredInApp = inAppCampaigns
            .filter {
                // Trigger params filter
                ((it.trigger["eventName"] as JsonPrimitive?)?.content == eventName) && EventFilterEvaluator.evaluate(
                    getEventFilter(it), params
                )
            }
            .filter {
                // Display config filter
                it.timesDisplayed < it.displayConfig.displayLimit &&
                        (it.displayConfig.minIntervalBtwDisplays == 0L ||
                                it.displayConfig.minIntervalBtwDisplays * 1000 <= System.currentTimeMillis() - it.lastDisplayedTime) &&
                        (it.displayConfig.minIntervalBtwDisplaysGlobal == 0L ||
                                it.displayConfig.minIntervalBtwDisplaysGlobal * 1000 <= System.currentTimeMillis() - latestCampaignViewTs)
            }
            .takeUnless { it.isNullOrEmpty() }
            ?.maxBy { it.priority }
        return triggeredInApp
    }

    private suspend fun launchInApp(
        context: Context, inAppSelectedForDisplay: Campaign
    ) = withContext(Main) {
        try {
            inAppViewDecorator =
                InAppViewDecorator(context, inAppSelectedForDisplay, inAppViewLifecycleListener)
            inAppViewDecorator?.let { inAppViewDecorator!!.show(true) }
        } catch (e: Exception) {
            logger.error("In-app display failed!", e)
        }
    }

    suspend fun updateInAppDisplayStats(inApp: Campaign) {
        inAppRepository.updateCampaignDisplayStats(inApp)
    }

    fun updateInAppForOrientationChanges(context: Context) {
        currentInAppBeingDisplayed?.let {
            try {
                inAppViewDecorator = InAppViewDecorator(
                    context,
                    currentInAppBeingDisplayed!!,
                    inAppViewLifecycleListener
                )
                inAppViewDecorator?.let { inAppViewDecorator!!.show(false) }
            } catch (e: Exception) {
                logger.error("In-app display failed after orientation!", e)
            }
        }
    }

    fun dismissDialogIfAny() {
        currentInAppBeingDisplayed?.let {
            try {
                inAppViewDecorator?.let {
                    inAppViewDecorator!!.dismissDialog()
                    inAppViewDecorator = null
                }

            } catch (e: Exception) {
                logger.error("In-app dismiss failed!", e)
            }
        }
    }
}
