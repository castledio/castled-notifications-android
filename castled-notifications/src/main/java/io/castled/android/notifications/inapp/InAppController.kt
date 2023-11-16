package io.castled.android.notifications.inapp

import android.app.Activity
import android.content.Context
import io.castled.android.notifications.commons.CastledManifestInfo
import io.castled.android.notifications.inapp.CampaignResponseConverter.toCampaign
import io.castled.android.notifications.inapp.service.InAppRepository
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.tracking.events.service.TrackEventApi
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
    private var pendingInapps = mutableListOf<Campaign>()
    private val currentInAppLock = Any()
    private val excludedActivities by lazy { CastledManifestInfo(context).getExcludedActivities() }
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
        val triggeredInApps = findTriggeredInApp(eventName, params) ?: return
        validateInappsBeforeDisplay(triggeredInApps)
     }

    private fun isSatisfiedWithGlobalIntervalBtwDisplays(campaign: Campaign, inAppCampaigns: List<Campaign>) : Boolean{
          val latestCampaignViewTs =
            inAppCampaigns.maxByOrNull { it.lastDisplayedTime }?.lastDisplayedTime
                ?: 0
        val result = (campaign.displayConfig.minIntervalBtwDisplaysGlobal == 0L ||
                campaign.displayConfig.minIntervalBtwDisplaysGlobal * 1000 <= System.currentTimeMillis() - latestCampaignViewTs)
        println("inapps validation  isSatisfiedWithGlobalIntervalBtwDisplays result $result ${campaign.notificationId} global time $latestCampaignViewTs cam time ${campaign.displayConfig.minIntervalBtwDisplaysGlobal}")

        return result
     }

    private suspend fun validateInappsBeforeDisplay(inApps:List<Campaign>){
        val inAppCampaigns = inAppRepository.getCampaigns()
        val triggeredInapps =  inApps.toMutableList()
        println("inapps validation  validateInappsBeforeDisplay ${triggeredInapps.map { it.notificationId }}")

        triggeredInapps.indexOfFirst { item ->
            isSatisfiedWithGlobalIntervalBtwDisplays(item,inAppCampaigns) && canShowInActivity()
        }.takeIf { satisfiedIndex -> satisfiedIndex != -1 }?.let { satisfiedIndex ->
            val  triggeredInApp = triggeredInapps[satisfiedIndex]
             try {
                 currentActivityReference?.let {
                     it.get()?.let { activityContext ->
                         if (updateCurrentInApp(triggeredInApp)) {
                             launchInApp(activityContext, triggeredInApp)
                             pendingInapps.removeIf {pendingItem -> pendingItem.notificationId == triggeredInApp.notificationId }

                             println("inapps validation before display ${triggeredInapps.map { it.notificationId }}")
                             triggeredInapps.removeAt(satisfiedIndex)
                             println("$satisfiedIndex inapps validation after delete ${triggeredInapps.map { it.notificationId }}")

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

    private fun enqueuePendingItems(items:List<Campaign>){
        println("inapps validation enqueuePendingItems before ${pendingInapps.map { it.notificationId }}")

        pendingInapps.addAll(items)
        println("inapps validation enqueuePendingItems after appending ${pendingInapps.map { it.notificationId }}")

        pendingInapps = pendingInapps.distinct().toMutableList()
        println("inapps validation enqueuePendingItems after duplicates ${pendingInapps.map { it.notificationId }}")

    }

    private fun canShowInActivity():Boolean{
        currentActivityReference?.let {
         val activityName =    it.get()?.componentName?.shortClassName?.drop(1)
            activityName?.let {
                return !excludedActivities.contains(activityName)
            }
        }
        return false
    }

    suspend fun triggerPendingNotificationsIfAny(){
        if (pendingInapps.isNotEmpty()) {
            validateInappsBeforeDisplay(pendingInapps)
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
    ): List<Campaign>? {
        val inAppCampaigns = inAppRepository.getCampaigns()
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
                                it.displayConfig.minIntervalBtwDisplays * 1000 <= System.currentTimeMillis() - it.lastDisplayedTime)
            }
            .takeUnless { it.isEmpty() }
            ?.sortedBy { it.priority }

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
                    context,
                    it,
                    inAppViewLifecycleListener
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
