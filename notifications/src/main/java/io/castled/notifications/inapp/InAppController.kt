package io.castled.notifications.inapp

import android.content.Context
import android.content.Intent
import com.google.gson.JsonSyntaxException
import io.castled.notifications.inapp.CampaignResponseConverter.toCampaign
import io.castled.notifications.inapp.models.consts.InAppConstants
import io.castled.notifications.store.models.Campaign
import io.castled.notifications.inapp.service.InAppRepository
import io.castled.notifications.inapp.trigger.*
import io.castled.notifications.inapp.trigger.InAppClickAction
import io.castled.notifications.inapp.trigger.InAppPopupDialog
import io.castled.notifications.push.CastledEventListener
import io.castled.notifications.push.models.Constants
import io.castled.notifications.push.models.NotificationEventType
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.trigger.EventFilterDeserializer
import io.castled.notifications.trigger.EventFilterEvaluator
import io.castled.notifications.trigger.enums.JoinType
import io.castled.notifications.trigger.models.EventFilter
import io.castled.notifications.trigger.models.GroupFilter
import io.castled.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import java.util.*

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

    private fun reportEvent(request: CastledInAppEventRequest) =
        CoroutineScope(Default).launch {
            inAppRepository.reportEvent(request)
        }

    internal suspend fun findAndLaunchInApp(
        context: Context,
        eventName: String,
        params: Map<String, Any>?
    ) {
        val triggeredInApps = findTriggeredInApp(eventName, params)
        launchInApp(context, triggeredInApps)
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
        val latestCampaignViewTs = inAppCampaigns.maxByOrNull { it.lastDisplayedTime }?.lastDisplayedTime
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

    private fun launchInApp(
        context: Context, triggeredInApps: List<Campaign>
    ) {
        if (triggeredInApps.isNotEmpty()) {
            val inAppSelectedForDisplay = triggeredInApps.maxBy { it.priority }
            CoroutineScope(Main).launch {
                logger.debug("launchTriggerEvent: ")
                when (InAppPopupDialog.getTriggerEventType(inAppSelectedForDisplay)) {
                    InAppConstants.Companion.InAppTemplateType.MODAL -> {
                        launchModalInApp(context, inAppSelectedForDisplay)
                    }
                    InAppConstants.Companion.InAppTemplateType.SLIDE_UP -> {
                        launchSlideUpInApp(context, inAppSelectedForDisplay)
                    }
                    InAppConstants.Companion.InAppTemplateType.FULL_SCREEN -> {
                        launchFullScreenInApp(context, inAppSelectedForDisplay)
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * The below code is just to test the different Event Dialog on the screen. eventType is 0 or 1 or 2
     */
    internal fun findAndLaunchInAppForTest(context: Context, eventType: Int) =
        CoroutineScope(Main).launch {
            if (eventType == 0 || eventType == 1 || eventType == 2) {

                withContext(Default) {

                    val inAppTemplateType = when (eventType) {
                        0 -> InAppConstants.Companion.InAppTemplateType.MODAL
                        1 -> InAppConstants.Companion.InAppTemplateType.FULL_SCREEN
                        2 -> InAppConstants.Companion.InAppTemplateType.SLIDE_UP
                        else -> InAppConstants.Companion.InAppTemplateType.NONE
                    }

                    val inAppCampaigns = inAppRepository.getCampaigns()
                    logger.debug("findAndLaunchTriggerNotification: ${inAppCampaigns.map { it.notificationId }}")

                    if (inAppCampaigns.isNotEmpty()) {
                        val event = inAppCampaigns.firstOrNull {
                            InAppPopupDialog.getTriggerEventType(
                                it
                            ) == inAppTemplateType
                        }
                        withContext(Main) {
                            when (InAppPopupDialog.getTriggerEventType(event)) {
                                InAppConstants.Companion.InAppTemplateType.MODAL -> {
                                    launchModalInApp(context, event!!)
                                }
                                InAppConstants.Companion.InAppTemplateType.SLIDE_UP -> {
                                    launchSlideUpInApp(context, event!!)
                                }
                                InAppConstants.Companion.InAppTemplateType.FULL_SCREEN -> {
                                    launchFullScreenInApp(context, event!!)
                                }
                                InAppConstants.Companion.InAppTemplateType.NONE -> {
//                                Toast.makeText(context, "No ${triggerEventType.name} event found in the database.", Toast.LENGTH_SHORT).show()
                                    logger.debug("findAndLaunchTriggerEventForTest: No ${inAppTemplateType.name} event found in the database.")
                                }
                            }
                        }
                    }
                }

            } else {
                logger.debug("findAndLaunchTriggerEventForTest: Please enter 0 for MODAL, 1 for FULL_SCREEN or 2 for SLIDE_UP.")
            }
        }

    private fun preparePopupHeader(modal: JsonObject) = PopupHeader(
        (modal["title"] as JsonPrimitive?)?.content ?: "",
        (modal["titleFontColor"] as JsonPrimitive?)?.content ?: "",
        (modal["titleFontSize"] as JsonPrimitive?)?.float ?: 0F,
        (modal["titleBgColor"] as JsonPrimitive?)?.content ?: ""
    )

    // val actionType = (jsonButton["clickAction"] as JsonPrimitive?) ?.let { it.content }


    private fun preparePopupMessage(modal: JsonObject): PopupMessage {
        if (modal["bodyFontColor"] != null && modal["bodyFontSize"] != null && modal["bodyBgColor"] != null) {
            return PopupMessage(
                (modal["body"] as JsonPrimitive).content,
                (modal["bodyFontColor"] as JsonPrimitive).content,
                (modal["bodyFontSize"] as JsonPrimitive).float,
                (modal["bodyBgColor"] as JsonPrimitive).content
            )
        } else if (modal["bgColor"] != null && modal["fontSize"] != null && modal["fontColor"] != null) {
            return PopupMessage(
                (modal["body"] as JsonPrimitive).content,
                (modal["fontColor"] as JsonPrimitive).content,
                (modal["fontSize"] as JsonPrimitive).float,
                (modal["bgColor"] as JsonPrimitive).content
            )
        } else return PopupMessage(
            (modal["body"] as JsonPrimitive?)?.content ?: "",
            "#000000",
            18F,
            "#FFFFFF"
        )
    }

    private fun preparePopupPrimaryButton(primaryPopupButtonJson: JsonObject) = PopupPrimaryButton(
        primaryPopupButtonJson["label"]?.jsonPrimitive?.content!!,
        primaryPopupButtonJson["fontColor"]?.jsonPrimitive?.content!!,
        primaryPopupButtonJson["buttonColor"]?.jsonPrimitive?.content!!,
        primaryPopupButtonJson["borderColor"]?.jsonPrimitive?.content!!,
        primaryPopupButtonJson["url"]?.jsonPrimitive?.content ?: ""
    )

    private fun preparePopupSecondaryButton(secondaryPopupButtonJson: JsonObject) =
        PopupSecondaryButton(
            secondaryPopupButtonJson["label"]?.jsonPrimitive?.content!!,
            secondaryPopupButtonJson["fontColor"]?.jsonPrimitive?.content!!,
            secondaryPopupButtonJson["buttonColor"]?.jsonPrimitive?.content!!,
            secondaryPopupButtonJson["borderColor"]?.jsonPrimitive?.content!!,
            secondaryPopupButtonJson["url"]?.jsonPrimitive?.content ?: ""
        )

    private suspend fun launchModalInApp(context: Context, campaign: Campaign) {
        val message: JsonObject = campaign.message
        val modal: JsonObject = message["modal"] as JsonObject
        val buttons: JsonArray = modal["actionButtons"] as JsonArray

        if (buttons.size < 2) {
            logger.debug("launchModalTriggerNotification: Event is not valid for notificationId ${campaign.notificationId}.")
            return
        }

        val buttonPrimary: JsonObject = buttons[0] as JsonObject
        val buttonSecondary: JsonObject = buttons[1] as JsonObject

        inAppRepository.updateCampaignDisplayStats(campaign)
        reportEvent(prepareEventViewActionBodyData(campaign))

        InAppPopupDialog.showDialog(context,
            campaign.displayConfig.autoDismissInterval,
            modal["screenOverlayColor"] as String?,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            modal["imageUrl"] as String? ?: "",
            preparePopupPrimaryButton(buttonPrimary),
            preparePopupSecondaryButton(buttonSecondary),

            object : InAppClickAction {
                override fun onTrigger(
                    clickType: InAppConstants.Companion.EventClickType
                ) {

                    when (clickType) {

                        InAppConstants.Companion.EventClickType.CLOSE_EVENT -> {
                            reportEvent(
                                prepareEventCloseActionBodyData(campaign)
                            )
                        }
                        InAppConstants.Companion.EventClickType.IMAGE_CLICK -> {
                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = NotificationEventType.CLICKED.toString()
                            modal["url"]?.jsonPrimitive?.content?.let {
                                intent.putExtra(Constants.EXTRA_URI, it)
                            }
                            modal["defaultClickAction"]?.jsonPrimitive?.content.let {
                                intent.putExtra(Constants.EXTRA_ACTION, it)
                            }
                            context.startActivity(intent)
                            reportEvent(prepareEventImageClickActionBodyData(campaign))
                        }
                        InAppConstants.Companion.EventClickType.PRIMARY_BUTTON -> {

                            logger.info("buttonPrimary: $buttonPrimary")
                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = NotificationEventType.CLICKED.toString()

                            (buttonPrimary["url"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_URI, it)
                            }
                            (buttonPrimary["clickAction"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_ACTION, it)
                            }
                            (buttonPrimary["label"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_LABEL, it)
                            }
                            (buttonPrimary["keyVals"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_KEY_VAL_PARAMS, it)
                            }
                            context.startActivity(intent)
                            reportEvent(
                                prepareEventButtonClickActionBodyData(
                                    campaign,
                                    buttonPrimary
                                )
                            )
                        }
                        InAppConstants.Companion.EventClickType.SECONDARY_BUTTON -> {

                            logger.info("buttonSecondary: $buttonSecondary")

                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = NotificationEventType.CLICKED.toString()

                            (buttonSecondary["url"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_URI, it)
                            }
                            (buttonSecondary["clickAction"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_ACTION, it)
                            }
                            (buttonSecondary["label"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_LABEL, it)
                            }
                            (buttonSecondary["keyVals"] as String?)?.let {
                                intent.putExtra(Constants.EXTRA_KEY_VAL_PARAMS, it)
                            }
                            context.startActivity(intent)
                            reportEvent(
                                prepareEventButtonClickActionBodyData(
                                    campaign, buttonSecondary
                                )
                            )
                        }
                    }

                }
            })
    }

    private fun launchFullScreenInApp(context: Context, campaign: Campaign) {
        val message: JsonObject = campaign.message
        val modal: JsonObject = message["fs"] as JsonObject
        val buttons: JsonArray = modal["actionButtons"] as JsonArray
        val buttonPrimary: JsonObject = buttons[0] as JsonObject
        val buttonSecondary: JsonObject = buttons[1] as JsonObject

        reportEvent(prepareEventViewActionBodyData(campaign))

        InAppPopupDialog.showFullscreenDialog(context,
            modal["screenOverlayColor"]?.jsonPrimitive?.content!!,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            modal["imageUrl"]?.jsonPrimitive?.content ?: "",
            modal["defaultClickAction"] as String? ?: "",
            preparePopupPrimaryButton(buttonPrimary),
            preparePopupSecondaryButton(buttonSecondary),
            object : InAppClickAction {
                override fun onTrigger(
                    clickType: InAppConstants.Companion.EventClickType
                ) {
                    when (clickType) {
                        InAppConstants.Companion.EventClickType.CLOSE_EVENT -> {
                            reportEvent(
                                prepareEventCloseActionBodyData(campaign)
                            )
                        }
                        InAppConstants.Companion.EventClickType.IMAGE_CLICK -> {
                            reportEvent(
                                prepareEventImageClickActionBodyData(campaign)
                            )
                        }
                        InAppConstants.Companion.EventClickType.PRIMARY_BUTTON -> {
                            reportEvent(
                                prepareEventButtonClickActionBodyData(
                                    campaign, buttonPrimary
                                )
                            )
                        }
                        InAppConstants.Companion.EventClickType.SECONDARY_BUTTON -> {
                            reportEvent(
                                prepareEventButtonClickActionBodyData(
                                    campaign, buttonSecondary
                                )
                            )
                        }
                    }
                }
            })
    }

    private fun launchSlideUpInApp(context: Context, eventModel: Campaign) {
        logger.debug("notification: $eventModel")
        val message: JsonObject = eventModel.message
        val modal: JsonObject = message["slideUp"] as JsonObject
        logger.debug("slideUp: $modal")

        reportEvent(prepareEventViewActionBodyData(eventModel))
        InAppPopupDialog.showSlideUpDialog(context,
            modal["bgColor"]?.jsonPrimitive?.content!!,
            preparePopupMessage(modal),
            (modal["imageUrl"] as String?) ?: "",
            (modal["url"] as String?) ?: "",
            object : InAppClickAction {
                override fun onTrigger(
                    clickType: InAppConstants.Companion.EventClickType
                ) {
                    when (clickType) {
                        InAppConstants.Companion.EventClickType.CLOSE_EVENT -> {
                            reportEvent(
                                prepareEventCloseActionBodyData(eventModel)
                            )
                        }
                        InAppConstants.Companion.EventClickType.IMAGE_CLICK -> {
                            reportEvent(
                                prepareEventImageClickActionBodyData(eventModel)
                            )
                        }
                        InAppConstants.Companion.EventClickType.PRIMARY_BUTTON -> {}
                        InAppConstants.Companion.EventClickType.SECONDARY_BUTTON -> {}
                    }
                }
            })
    }

    private fun prepareEventViewActionBodyData(campaign: Campaign): CastledInAppEventRequest {
        return CastledInAppEventRequest(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "VIEWED",
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
    }

    private fun prepareEventCloseActionBodyData(campaign: Campaign): CastledInAppEventRequest {
        return CastledInAppEventRequest(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "DISCARDED",
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
    }

    fun prepareEventImageClickActionBodyData(campaign: Campaign): CastledInAppEventRequest {
        val message: JsonObject = campaign.message
        val msgBody: JsonObject = message["modal"] as JsonObject?
            ?: message["fs"] as JsonObject?
            ?: message["slideUp"] as JsonObject

        val actionType = (msgBody["defaultClickAction"] as JsonPrimitive?)?.content
        val actionUri = (msgBody["url"] as JsonPrimitive?)?.content

        return CastledInAppEventRequest(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "CLICKED",
            actionType = actionType,
            actionUri = actionUri,
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
    }

    private fun prepareEventButtonClickActionBodyData(
        campaign: Campaign, jsonButton: JsonObject
    ): CastledInAppEventRequest {

        val actionType = (jsonButton["clickAction"] as JsonPrimitive?)?.content
        val actionUri = (jsonButton["url"] as JsonPrimitive?)?.content
        val label = (jsonButton["label"] as JsonPrimitive?)?.content

        return CastledInAppEventRequest(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "CLICKED",
            btnLabel = label,
            actionType = actionType,
            actionUri = actionUri,
            ts = System.currentTimeMillis() / 1000,
            tz = TimeZone.getDefault().displayName
        )
    }

}
