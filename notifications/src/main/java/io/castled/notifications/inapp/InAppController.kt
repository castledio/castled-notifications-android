package io.castled.notifications.inapp

import android.content.Context
import com.google.gson.JsonSyntaxException
import io.castled.notifications.inapp.CampaignResponseConverter.toCampaign
import io.castled.notifications.store.models.Campaign
import io.castled.notifications.inapp.service.InAppRepository
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.trigger.EventFilterDeserializer
import io.castled.notifications.trigger.EventFilterEvaluator
import io.castled.notifications.trigger.enums.JoinType
import io.castled.notifications.trigger.models.EventFilter
import io.castled.notifications.trigger.models.GroupFilter
import io.castled.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule

internal class InAppController(context: Context) {

    private val inAppRepository = InAppRepository(context)
    private val logger = CastledLogger.getInstance(LogTags.IN_APP_TRIGGER)

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
        val triggeredInApps = findTriggeredInApp(eventName, params)
        try {
            launchInApp(context, triggeredInApps)
        } catch (e: Exception) {
            logger.error("In-app launch failed!", e)
        }
    }

    private fun getEventFilter(campaign: Campaign): GroupFilter {
        return try {
            val json = Json {
                ignoreUnknownKeys = true
                serializersModule = SerializersModule {
                    contextual(EventFilter::class, EventFilterDeserializer)
                }
            }
            return json.decodeFromJsonElement(campaign.trigger["eventFilter"] as JsonElement)
        } catch (e: JsonSyntaxException) {
            logger.error("Couldn't deserialize event filter!", e)
            GroupFilter(JoinType.AND, null)
        }
    }

    private suspend fun findTriggeredInApp(
        eventName: String,
        params: Map<String, Any>?
    ): List<Campaign> {
        val inAppCampaigns = inAppRepository.getCampaigns()
        val latestCampaignViewTs =
            inAppCampaigns.maxByOrNull { it.lastDisplayedTime }?.lastDisplayedTime
                ?: 0

        val triggeredInApps = inAppCampaigns
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
        return triggeredInApps
    }

    private suspend fun launchInApp(
        context: Context, triggeredInApps: List<Campaign>
    ) {
        if (triggeredInApps.isNotEmpty()) {
            val inAppSelectedForDisplay = triggeredInApps.maxBy { it.priority }
            withContext(Main) {
                try {
                    InAppViewDecorator(context, inAppSelectedForDisplay).show()
                } catch (e: Exception) {
                    logger.error("In-app display failed!", e)
                }
            }
        }
    }

}
