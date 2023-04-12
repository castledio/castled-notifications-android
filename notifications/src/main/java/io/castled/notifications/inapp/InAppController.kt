package io.castled.notifications.inapp

import android.content.Context
import android.content.Intent
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.castled.notifications.inapp.models.consts.InAppConstants
import io.castled.notifications.store.models.Campaign
import io.castled.notifications.inapp.models.CampaignResponse
import io.castled.notifications.inapp.service.InAppRepository
import io.castled.notifications.inapp.trigger.*
import io.castled.notifications.inapp.trigger.InAppClickAction
import io.castled.notifications.inapp.trigger.InAppPopupDialog
import io.castled.notifications.push.CastledEventListener
import io.castled.notifications.push.models.ClickAction
import io.castled.notifications.push.models.Constants
import io.castled.notifications.push.models.NotificationEventType
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.trigger.EventFilterDeserializer
import io.castled.notifications.trigger.TriggerParamsEvaluator
import io.castled.notifications.trigger.models.EventFilter
import io.castled.notifications.trigger.models.NestedEventFilter
import io.castled.notifications.workmanager.models.CastledInAppEventRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main

internal class InAppController(context: Context) {

    private val inAppRepository = InAppRepository(context)
    private val logger = CastledLogger.getInstance(LogTags.IN_APP_TRIGGER)

    suspend fun fetchAndSaveLiveCampaigns() {
        val campaigns = requestInAppFromCloud()
        if (campaigns != null && campaigns.isNotEmpty()) {
            // TODO: shouldn't be deleting, update instead
            inAppRepository.deleteDbCampaigns()
            val rows = inAppRepository.insertCampaignsIntoDb(campaigns)
            logger.debug("inserted into db: ${rows.toList()}")
        } else logger.info("Notification fetch failed.")
    }

    private suspend fun requestInAppFromCloud(): List<Campaign>? {
        val liveCampaigns = inAppRepository.fetchLiveCampaigns()
        return convertCampaignModelApiListToCampaignModelList(liveCampaigns)
    }

    private suspend fun convertCampaignModelApiListToCampaignModelList(campaignResponse: List<CampaignResponse>?): List<Campaign>? {
        if (campaignResponse == null) return null

        val campaignList = mutableListOf<Campaign>()
        campaignResponse.forEachIndexed { index, response ->
            campaignList.add(
                Campaign(
                    (index + 1),
                    response.notificationId,
                    response.teamId,
                    response.sourceContext,
                    response.startTs,
                    response.endTs,
                    response.ttl,
                    response.displayConfig.displayLimit,
                    0,
                    response.displayConfig.minIntervalBtwDisplays,
                    0,
                    response.displayConfig.minIntervalBtwDisplaysGlobal,
                    response.displayConfig.autoDismissInterval,
                    response.trigger,
                    response.message,
                )
            )
        }

        val dbCampaigns = inAppRepository.getCampaigns()
        dbCampaigns.forEach { dbCampaign ->
            campaignList.map { model ->
                if (model.notificationId == dbCampaign.notificationId) {
                    model.lastDisplayedTime = dbCampaign.lastDisplayedTime
                    model.timesDisplayed = dbCampaign.timesDisplayed
                    model
                } else model
            }
        }
        return campaignList
    }

    private fun reportEvent(request: CastledInAppEventRequest) =
        CoroutineScope(Default).launch {
            inAppRepository.reportEvent(request)
        }

    internal suspend fun findAndLaunchInApp(
        context: Context,
        eventParamsWithEventName: Map<String, Any?>
    ) {
        val value = evaluateDbCampaignTrigger(eventParamsWithEventName.toMutableMap())
        launchTriggerEvent(context, value)
    }

    private suspend fun evaluateDbCampaignTrigger(eventParam: MutableMap<String, Any?>): List<Campaign> {
        val showOnScreenEvent = mutableListOf<Campaign>()
        var inAppCampaigns = inAppRepository.getCampaigns()
        val triggerParamsEvaluator =
            TriggerParamsEvaluator()

        val eventNameFromParams = eventParam["event"]
        inAppCampaigns = inAppCampaigns.filter { triggerObj ->
            val eventNameFromTrigger = triggerObj.trigger.asJsonObject.get("eventName").asString

            when (eventNameFromParams) {
                "app_opened" -> eventNameFromTrigger.equals(eventNameFromParams)
                "page_viewed" -> eventNameFromTrigger.equals(eventNameFromParams)
                else -> false
            }
        }

        //TODO rename "triggerEvent" to campaign. Rename related stuff
        val timeRightNow = System.currentTimeMillis()

        //TODO Check if there's benefit. find last campaignViewTime via sql for performance reasons
        var lastCampaignViewTime = 0L
        inAppCampaigns.maxByOrNull { it.lastDisplayedTime }?.let {
            logger.info("max ${it.notificationId}")
            lastCampaignViewTime = it.lastDisplayedTime
        }
        inAppCampaigns.forEach { triggerEventModel ->
            logger.debug("DB trigger JSON: ${triggerEventModel.trigger}")
            if (!triggerEventModel.trigger.asJsonObject.isJsonNull && triggerEventModel.trigger.asJsonObject.has(
                    "eventFilter"
                ) && !triggerEventModel.trigger.asJsonObject.get("eventFilter").isJsonNull
            ) {
                logger.debug("DB trigger JSON(Condition Pass): ${triggerEventModel.trigger}")

                logger.debug("timeRightNow: $timeRightNow, Event endTime: ${triggerEventModel.endTs}, startTime: ${triggerEventModel.startTs}")

                //TODO convert these checks into functional style with filters.
                // TODO convert this and above if to SQL for performance? Check if beneficial
                // TODO: close gitHub-> https://github.com/dheerajbhaskar/castled-notifications-android/issues/54
                if (triggerEventModel.endTs > timeRightNow && triggerEventModel.displayLimit > triggerEventModel.timesDisplayed && timeRightNow > (triggerEventModel.lastDisplayedTime + triggerEventModel.minIntervalBtwDisplays) && timeRightNow > (lastCampaignViewTime + triggerEventModel.minIntervalBtwDisplaysGlobal)
//                        && triggerEventModel.minIntervalBtwDisplays >= (timeRightNow - triggerEventModel.lastDisplayedTime)
                ) {
                    val gson = GsonBuilder().registerTypeAdapter(
                        EventFilter::class.java,
                        EventFilterDeserializer()
                    ).create()
                    val eventFilter: EventFilter = gson.fromJson(
                        triggerEventModel.trigger.get("eventFilter").asJsonObject,
                        EventFilter::class.java
                    )
                    if (triggerParamsEvaluator.evaluate(
                            eventParam, eventFilter as NestedEventFilter
                        )
                    ) showOnScreenEvent.add(triggerEventModel)
                } else {
                    logger.debug("${triggerEventModel.notificationId} expired or crossed the display limit or minIntervalBtwDisplays not crossed")
                }
            }
        }
        return showOnScreenEvent
    }

    private fun launchTriggerEvent(
        context: Context, triggerEvents: List<Campaign>
    ) {
        if (triggerEvents.isNotEmpty()) {
            val event = triggerEvents.first()
            CoroutineScope(Main).launch {
                logger.debug("launchTriggerEvent: ")
                when (InAppPopupDialog.getTriggerEventType(event)) {
                    InAppConstants.Companion.InAppTemplateType.MODAL -> {
                        launchModalInApp(context, event)
                    }
                    InAppConstants.Companion.InAppTemplateType.SLIDE_UP -> {
                        launchSlideUpInApp(context, event)
                    }
                    InAppConstants.Companion.InAppTemplateType.FULL_SCREEN -> {
                        launchFullScreenInApp(context, event)
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

    /**
     *  The below code is just to test the different Event Dialog on the screen. eventType is 0 or 1 or 2
     */
    internal fun findAndLaunchInAppForTest(context: Context, event: JsonObject) =
        CoroutineScope(Default).launch {

            logger.debug("selected event: $event")

            val eventLocal = Campaign(
                event.get("id").asInt,
                event.get("notificationId").asInt,
                event.get("teamId").asLong,
                event.get("sourceContext").asString,
                event.get("startTs").asLong,
                event.get("endTs").asLong,
                event.get("ttl").asInt,
                event.get("displayLimit").asLong,
                event.get("timesDisplayed").asLong,
                event.get("minIntervalBtwDisplays").asLong,
                event.get("lastDisplayedTime").asLong,
                event.get("minIntervalBtwDisplaysGlobal").asLong,
                event.get("autoDismissInterval").asLong,
                event.get("trigger").asJsonObject,
                event.get("message").asJsonObject
            )

            withContext(Main) {
                when (InAppPopupDialog.getTriggerEventType(eventLocal)) {
                    InAppConstants.Companion.InAppTemplateType.MODAL -> {
                        launchModalInApp(context, eventLocal)
                    }
                    InAppConstants.Companion.InAppTemplateType.SLIDE_UP -> {
                        launchSlideUpInApp(context, eventLocal)
                    }
                    InAppConstants.Companion.InAppTemplateType.FULL_SCREEN -> {
                        launchFullScreenInApp(context, eventLocal)
                    }
                    InAppConstants.Companion.InAppTemplateType.NONE -> {}
                }
            }
        }

    private fun getDefaultNotification(): Campaign {
        val fullscreen = JsonObject()
        fullscreen.addProperty(
            "imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png"
        )
        fullscreen.addProperty("defaultClickAction", "NONE")
        fullscreen.addProperty("screenOverlayColor", "#f8ffbd")
        fullscreen.addProperty("title", "Full screen title text.")
        fullscreen.addProperty("titleFontColor", "#FFFFFF")
        fullscreen.addProperty("titleFontSize", 18)
        fullscreen.addProperty("titleBgColor", "#E74C3C")
        fullscreen.addProperty("body", "Full screen message text.")
        fullscreen.addProperty("bodyFontColor", "#FFFFFF")
        fullscreen.addProperty("bodyFontSize", 12)
        fullscreen.addProperty("bodyBgColor", "#039ADC")

        val modal = JsonObject()
        modal.addProperty(
            "imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png"
        )
        modal.addProperty("defaultClickAction", "NONE")
        modal.addProperty("screenOverlayColor", "#f8ffbd")
        modal.addProperty("title", "Summer sale is Back!")
        modal.addProperty("titleFontColor", "#FFFFFF")
        modal.addProperty("titleFontSize", 18)
        modal.addProperty("titleBgColor", "#E74C3C")
        modal.addProperty(
            "body",
            "Full Screen \n" + "30% offer on Electronics, Cloths, Sports and other categories."
        )
        modal.addProperty("bodyFontColor", "#FFFFFF")
        modal.addProperty("bodyFontSize", 12)
        modal.addProperty("bodyBgColor", "#039ADC")

        val slideup = JsonObject()
        slideup.addProperty(
            "imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png"
        )
        slideup.addProperty("defaultClickAction", "NONE")
        slideup.addProperty("screenOverlayColor", "#ff99ff")
        slideup.addProperty("titleBgColor", "#E74C3C")
        slideup.addProperty(
            "body", "Slide Up \n" + "30% offer on Electronics, Cloths, Sports and other categories."
        )
        slideup.addProperty("bodyFontColor", "#FFFFFF")
        slideup.addProperty("bodyFontSize", 12)
        slideup.addProperty("bodyBgColor", "#039ADC")

        val jsonPrimaryButton = JsonObject()
        jsonPrimaryButton.addProperty("label", "Skip Now")
        jsonPrimaryButton.addProperty("url", "app://a.b")
        jsonPrimaryButton.addProperty("clickAction", "DEEP_LINKING")
        jsonPrimaryButton.addProperty("buttonColor", "#ffffff")
        jsonPrimaryButton.addProperty("fontColor", "#000000")
        jsonPrimaryButton.addProperty("borderColor", "#000000")

        val jsonSecondaryButton = JsonObject()
        jsonSecondaryButton.addProperty("label", "Start Shopping")
        jsonSecondaryButton.addProperty("url", "app://a.b")
        jsonSecondaryButton.addProperty("clickAction", "DISMISS_NOTIFICATION")
        jsonSecondaryButton.addProperty("buttonColor", "#FF6D07")
        jsonSecondaryButton.addProperty("fontColor", "#ffe0da")
        jsonSecondaryButton.addProperty("borderColor", "#5cdb5c")

        val buttons = JsonArray()
        buttons.add(jsonPrimaryButton as JsonElement)
        buttons.add(jsonSecondaryButton as JsonElement)

        fullscreen.add("actionButtons", buttons)
        modal.add("actionButtons", buttons)

        val message = JsonObject()
        message.add("fs", fullscreen)
        message.add("modal", modal)
        message.add("su", slideup)

        val trigger = JsonObject()

        return Campaign(1, 1, 1L, "", 0L, 0L, 0, 0, 1, 1, 1, 1, 1, trigger, message)
    }

    private fun preparePopupHeader(modal: JsonObject) = PopupHeader(
        if (modal.get("title").isJsonNull) "" else modal.get("title").asString,
        modal.get("titleFontColor").asString,
        modal.get("titleFontSize").asFloat,
        modal.get("titleBgColor").asString
    )

    private fun preparePopupMessage(modal: JsonObject): PopupMessage {
        if (modal.has("bodyFontColor") && modal.has("bodyFontSize") && modal.has("bodyBgColor")) {
            return PopupMessage(
                if (modal.get("body").isJsonNull) "" else modal.get("body").asString,
                modal.get("bodyFontColor").asString,
                modal.get("bodyFontSize").asFloat,
                modal.get("bodyBgColor").asString
            )
        } else if (modal.has("bgColor") && modal.has("fontSize") && modal.has("fontColor")) {
            return PopupMessage(
                if (modal.get("body").isJsonNull) "" else modal.get("body").asString,
                modal.get("fontColor").asString,
                modal.get("fontSize").asFloat,
                modal.get("bgColor").asString
            )
        } else return PopupMessage(
            if (modal.get("body").isJsonNull) "" else modal.get("body").asString,
            "#000000",
            18F,
            "#FFFFFF"
        )
    }

    private fun preparePopupPrimaryButton(primaryPopupButtonJson: JsonObject) = PopupPrimaryButton(
        primaryPopupButtonJson.get("label").asString,
        primaryPopupButtonJson.get("fontColor").asString,
        primaryPopupButtonJson.get("buttonColor").asString,
        primaryPopupButtonJson.get("borderColor").asString,
        if (primaryPopupButtonJson.get("url").isJsonNull) "" else primaryPopupButtonJson.get("url").asString
    )

    private fun preparePopupSecondaryButton(secondaryPopupButtonJson: JsonObject) =
        PopupSecondaryButton(
            secondaryPopupButtonJson.get("label").asString,
            secondaryPopupButtonJson.get("fontColor").asString,
            secondaryPopupButtonJson.get("buttonColor").asString,
            secondaryPopupButtonJson.get("borderColor").asString,
            if (secondaryPopupButtonJson.get("url").isJsonNull) "" else secondaryPopupButtonJson.get(
                "url"
            ).asString
        )

    private suspend fun launchModalInApp(context: Context, campaign: Campaign) {
        val message: JsonObject = campaign.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("modal")
        val buttons: JsonArray = modal.getAsJsonArray("actionButtons")

        if (buttons.size() < 2) {
            logger.debug("launchModalTriggerNotification: Event is not valid for notificationId ${campaign.notificationId}.")
            return
        }

        val buttonPrimary: JsonObject = buttons[0].asJsonObject
        val buttonSecondary: JsonObject = buttons[1].asJsonObject

        inAppRepository.updateCampaignDisplayStats(campaign)
        reportEvent(prepareEventViewActionBodyData(campaign))

        InAppPopupDialog.showDialog(context,
            campaign.autoDismissInterval,
            modal.get("screenOverlayColor").asString,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            if (modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
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
                            if (!modal.get("url").isJsonNull) intent.putExtra(
                                Constants.EXTRA_URI,
                                modal.get("url").asString
                            )

                            //FIXME covering for a backend bug where defaultClickAction is null but url is non-null
                            when {
                                modal.get("defaultClickAction").isJsonNull && !modal.get("url").isJsonNull -> intent.putExtra(
                                    Constants.EXTRA_ACTION, ClickAction.DEFAULT.name
                                )
                                !modal.get("defaultClickAction").isJsonNull -> intent.putExtra(
                                    Constants.EXTRA_ACTION, modal.get("defaultClickAction").asString
                                )
                            }

                            context.startActivity(intent)

                            reportEvent(
                                prepareEventImageClickActionBodyData(campaign)
                            )
                        }
                        InAppConstants.Companion.EventClickType.PRIMARY_BUTTON -> {

                            logger.info("buttonPrimary: $buttonPrimary")
                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = NotificationEventType.CLICKED.toString()

                            if (!buttonPrimary.get("url").isJsonNull) intent.putExtra(
                                Constants.EXTRA_URI, buttonPrimary.get("url").asString
                            )

                            if (!buttonPrimary.get("clickAction").isJsonNull) intent.putExtra(
                                Constants.EXTRA_ACTION, buttonPrimary.get("clickAction").asString
                            )

                            if (!buttonPrimary.get("label").isJsonNull) intent.putExtra(
                                Constants.EXTRA_LABEL, buttonPrimary.get("label").asString
                            )

                            if (!buttonPrimary.get("keyVals").isJsonNull) intent.putExtra(
                                Constants.EXTRA_KEY_VAL_PARAMS,
                                buttonPrimary.get("keyVals").toString()
                            )

                            context.startActivity(intent)

                            reportEvent(
                                prepareEventButtonClickActionBodyData(
                                    campaign, buttonPrimary
                                )
                            )
                        }
                        InAppConstants.Companion.EventClickType.SECONDARY_BUTTON -> {

                            logger.info("buttonSecondary: $buttonSecondary")

                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = NotificationEventType.CLICKED.toString()

                            if (!buttonSecondary.get("url").isJsonNull) intent.putExtra(
                                Constants.EXTRA_URI, buttonSecondary.get("url").asString
                            )

                            if (!buttonSecondary.get("clickAction").isJsonNull) intent.putExtra(
                                Constants.EXTRA_ACTION, buttonSecondary.get("clickAction").asString
                            )

                            if (!buttonSecondary.get("label").isJsonNull) intent.putExtra(
                                Constants.EXTRA_LABEL, buttonSecondary.get("label").asString
                            )

                            if (!buttonSecondary.get("keyVals").isJsonNull) intent.putExtra(
                                Constants.EXTRA_KEY_VAL_PARAMS,
                                buttonSecondary.get("keyVals").toString()
                            )

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
        val message: JsonObject = campaign.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("fs")
        val buttons: JsonArray = modal.getAsJsonArray("actionButtons")
        val buttonPrimary: JsonObject = buttons[0].asJsonObject
        val buttonSecondary: JsonObject = buttons[1].asJsonObject

        reportEvent(prepareEventViewActionBodyData(campaign))

        InAppPopupDialog.showFullscreenDialog(context,
            modal.get("screenOverlayColor").asString,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            if (modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            if (modal.get("defaultClickAction").isJsonNull) "" else modal.get("defaultClickAction").asString,
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
        val message: JsonObject = eventModel.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("slideUp")
        logger.debug("slideUp: $modal")

        val eventClickActionData = JsonObject()
        eventClickActionData.addProperty("teamId", eventModel.teamId)
        eventClickActionData.addProperty("sourceContext", eventModel.sourceContext)

        reportEvent(prepareEventViewActionBodyData(eventModel))

        InAppPopupDialog.showSlideUpDialog(context,
            modal.get("bgColor").asString,
            preparePopupMessage(modal),
            if (modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            if (modal.get("url").isJsonNull) "" else modal.get("url").asString,
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
            tz = "EST"
        )
    }

    private fun prepareEventCloseActionBodyData(campaign: Campaign): CastledInAppEventRequest {
        return CastledInAppEventRequest(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "DISCARDED",
            ts = System.currentTimeMillis() / 1000,
            tz = "EST"
        )
    }

    fun prepareEventImageClickActionBodyData(campaign: Campaign): CastledInAppEventRequest {
        val message: JsonObject = campaign.message.asJsonObject
        val msgBody: JsonObject = if (message.has("modal")) message.getAsJsonObject("modal")
        else if (message.has("fs")) message.getAsJsonObject("fs")
        else if (message.has("slideUp")) message.getAsJsonObject("slideUp")
        else JsonObject()

        val eventClickActionData = JsonObject()
        eventClickActionData.addProperty("teamId", campaign.teamId)
        eventClickActionData.addProperty("sourceContext", campaign.sourceContext)
        eventClickActionData.addProperty("eventType", "CLICKED")

        val actionType = msgBody.get("defaultClickAction")?.asString
        val actionUri = msgBody.get("url")?.asString

        return CastledInAppEventRequest(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "CLICKED",
            actionType = actionType,
            actionUri = actionUri,
            ts = System.currentTimeMillis() / 1000,
            tz = "EST"
        )
    }

    private fun prepareEventButtonClickActionBodyData(
        campaign: Campaign, jsonButton: JsonObject
    ): CastledInAppEventRequest {

        val actionType = jsonButton.get("clickAction")?.asString
        val actionUri = jsonButton.get("url")?.asString
        val label = jsonButton.get("label")?.asString

        return CastledInAppEventRequest(
            teamId = campaign.teamId.toString(),
            sourceContext = campaign.sourceContext,
            eventType = "CLICKED",
            btnLabel = label,
            actionType = actionType,
            actionUri = actionUri,
            ts = System.currentTimeMillis() / 1000,
            tz = "EST"
        )
    }

}
