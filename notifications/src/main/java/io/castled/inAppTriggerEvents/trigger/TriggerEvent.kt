package io.castled.inAppTriggerEvents.trigger

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.castled.inAppTriggerEvents.database.DatabaseBuilder
import io.castled.inAppTriggerEvents.database.CampaignDatabaseHelperImpl
import io.castled.inAppTriggerEvents.database.DbOperation
import io.castled.inAppTriggerEvents.event.EventNotification
import io.castled.inAppTriggerEvents.eventConsts.TriggerEventConstants
import io.castled.inAppTriggerEvents.models.CampaignModel
import io.castled.inAppTriggerEvents.models.CampaignModelApi
import io.castled.inAppTriggerEvents.requests.ServiceGenerator
import io.castled.notifications.CastledEventListener
import io.castled.notifications.consts.Constants
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.trigger.EventFilterDeserializer
import io.castled.notifications.trigger.TriggerParamsEvaluator
import io.castled.notifications.trigger.models.EventFilter
import io.castled.notifications.trigger.models.NestedEventFilter
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Response
import java.util.*


private const val TAG = "TriggerEvent"

internal class TriggerEvent private constructor(){
    private val castledLogger = CastledLogger.getInstance()

    companion object {
        private lateinit var triggerEvent: TriggerEvent

        @JvmStatic
        internal fun getInstance(): TriggerEvent{
            return if (this::triggerEvent.isInitialized) triggerEvent else TriggerEvent()
        }
    }

    internal fun fetchAndSaveTriggerEvents(context: Context) {

        CoroutineScope(Main).launch {
            val campaigns = requestTriggerEventsFromCloud(context)

            //No error in fetching notifications. This allows notifications to be empty
            if (campaigns != null) {
                val noOfRowDeleted = dbDeleteTriggerEvents(context)
                val rows = dbInsertTriggerEvents(context, campaigns)
                castledLogger.debug("$TAG: inserted into db: ${rows.toList()}")

            } else
                castledLogger.info("$TAG: Notification fetch failed.")
        }

    }

    private suspend fun requestTriggerEventsFromCloud(context: Context): List<CampaignModel>? {
        val inApp = EventNotification.getInstance
        if (!inApp.hasInternet) {
            CastledLogger.getInstance().error("$TAG: Error: No Internet.")
            return null
        }

        if (inApp.userId.isNullOrBlank()) {
            CastledLogger.getInstance().error("$TAG: UserId is null.")
            return null
        }

        return withContext(IO) {
                val eventsResponse = ServiceGenerator.requestApi()
                .makeNotificationQuery(inApp.instanceIdKey, inApp.userId!!)
//            showApiLog(eventsResponse)
            if (eventsResponse.isSuccessful && eventsResponse.body() != null) {
                convertCampaignModelApiListToCampaignModelList(context, eventsResponse.body())
            } else {
                withContext(Main) {
                    CastledLogger.getInstance().debug("$TAG: requestTriggerEventsFromCloud:  Error while getting data.")
                }
                null
            }
        }
    }

    private suspend fun convertCampaignModelApiListToCampaignModelList(context: Context, campaignModelApi: List<CampaignModelApi>?): List<CampaignModel>? {
        if (campaignModelApi == null)
            return null

        return withContext(Default){
            val campaignModelList = mutableListOf<CampaignModel>()
            campaignModelApi.forEachIndexed { index, campaignApi ->
                campaignModelList.add(
                    CampaignModel(
                        (index+1),
                        campaignApi.notificationId,
                        campaignApi.teamId,
                        campaignApi.sourceContext,
                        campaignApi.startTs,
                        campaignApi.endTs,
                        campaignApi.ttl,
                        campaignApi.displayConfig.displayLimit,
                        0,
                        campaignApi.displayConfig.minIntervalBtwDisplays,
                        0,
                        campaignApi.displayConfig.minIntervalBtwDisplaysGlobal,
//                        5,
                        campaignApi.displayConfig.autoDismissInterval,
                        campaignApi.trigger,
                        campaignApi.message,
                    )
                )
            }

            val dbCampaigns = dbFetchCampaigns(context)
            dbCampaigns.forEach { dbCampaign ->
                campaignModelList.map { model ->
                    if (model.notificationId == dbCampaign.notificationId){
                        model.lastDisplayedTime = dbCampaign.lastDisplayedTime
                        model.timesDisplayed = dbCampaign.timesDisplayed
                        model
                    } else model
                }
            }
            campaignModelList
        }
    }

    private suspend fun updateTriggerEventLogToCloudWithCount(eventBody: JsonObject, tryCount: Int): String {
        if (!EventNotification.getInstance.hasInternet) {
            CastledLogger.getInstance().debug("$TAG: Error: No Internet.")
            return "Error: No Internet."
        }
        return withContext(IO) {
            eventBody.addProperty("ts", System.currentTimeMillis())
            eventBody.addProperty("tz", TimeZone.getDefault().displayName)
            val response = ServiceGenerator.requestApi()
                .logEventView(EventNotification.getInstance.instanceIdKey, eventBody)

            /*CastledLogger.getInstance().debug("$TAG: \n\n\n** START ******* ## Log Trigger Event to Cloud(Try: $tryCount) ## *********\n" +
                    "Body(raw):1:: $eventBody\n" +
                    "Response isSuccess:2:: ${response.isSuccessful}\n" +
                    "Response Header:3:: ${response.headers()}\n" +
                    "Response Body:4:: ${response.body()}\n" +
                    "Response Message:5:: ${response.message()}\n" +
                    "Response errorBody:6:: ${response.errorBody()}\n" +
                    "Response raw:7:: ${response.raw()}\n" +
                    "Response Code:8:: ${response.code()}\n" +
                    "*********  *********  *********  ******* END **\n"
            )*/

            if (response.isSuccessful){
                response.raw().toString()
            } else if (!response.isSuccessful && tryCount < 99){
                //TODO: close gitHub-> do retries 100x pausing 100ms every time #13
                delay(100)
                updateTriggerEventLogToCloudWithCount(eventBody, (tryCount + 1))
            } else ""
        }
    }

    private suspend fun updateTriggerEventLogToCloud(eventBody: JsonObject): String {

        return updateTriggerEventLogToCloudWithCount(eventBody, 0)
    }

    private fun initiateTriggerEventLogToCloud(eventBody: JsonObject){
        /*
        * I used Coroutine GlobalScope because it will keep retrying until and unless the app completely closed.
        * */
        GlobalScope.launch {
            updateTriggerEventLogToCloud(eventBody)
        }
    }

    private var notificationObserver: LiveData<List<CampaignModel>>? = null
    fun observeDatabaseNotification(context: Context, viewLifecycleOwner: LifecycleOwner) {
        CoroutineScope(Default).launch {
            if (notificationObserver == null) {
                val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                notificationObserver = db.getLiveDataCampaignsFromDb()
            }

            if (notificationObserver?.hasObservers() != null){
                val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                CastledLogger.getInstance().debug("$TAG: hasActiveObservers: ${db.getLiveDataCampaignsFromDb().hasActiveObservers()}")
                CastledLogger.getInstance().debug("$TAG: hasObservers: ${db.getLiveDataCampaignsFromDb().hasObservers()}")
            }

            if (notificationObserver?.hasObservers() == null || !notificationObserver!!.hasObservers()) {
                withContext(Main) {
                    val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                    db.getLiveDataCampaignsFromDb().observe(viewLifecycleOwner) { it ->
                        CastledLogger.getInstance().debug("$TAG: observeDatabaseNotification: ${it?.size} notifications added/replaced.")
                        it.forEach {
                            CastledLogger.getInstance().debug("$TAG: observeDatabaseNotification: ${it.notificationId}")
                        }
                    }
                }
            }
        }
    }

    private fun startObservingTriggerNotification(context: Context) {
        // TODO remove. This is the wrong way. We need to react to events, not just go through db without events
       CoroutineScope(Main).launch {
           fetchAndSaveTriggerEvents(context)
           findAndLaunchDbTriggerEvent(context)
       }
    }

    //TODO rename this to findAndLaunchUi
    internal fun findAndLaunchEvent(context: Context, eventParamsWithEventName: Map<String, Any?>, callBack: (List<CampaignModel>) -> Unit) {
        CoroutineScope(Main).launch {
            //TODO rename value variable
            val value = evaluateDbTriggerEvent(context, eventParamsWithEventName.toMutableMap())
            //TODO maybe rename this to launchInapp
            launchTriggerEvent(context, value)
            callBack.invoke(value)
        }
    }


    private suspend fun evaluateDbTriggerEvent(
        context: Context,
        eventParam: MutableMap<String, Any?>
    ): List<CampaignModel> =

        withContext(Default) {
            CastledLogger.getInstance().debug("$TAG: **** evaluateDbTriggerEvent:: ****\teventParam:${eventParam.toList()}")
            val showOnScreenEvent = mutableListOf<CampaignModel>()
            var triggerEvent = dbFetchCampaigns(context)
            val triggerParamsEvaluator = TriggerParamsEvaluator()

            //TODO filter the inapps to be looked at here. LATER do this at the db level
            val eventNameFromParams = eventParam["event"]
            triggerEvent = triggerEvent.filter { triggerObj ->
                val eventNameFromTrigger = triggerObj.trigger.asJsonObject.get("eventName").asString

                when (eventNameFromParams){
                    "app_opened" -> eventNameFromTrigger.equals(eventNameFromParams)
                    //TODO update the page_viewed condition to check for the screen name as well
                    "page_viewed" -> eventNameFromTrigger.equals(eventNameFromParams)
                    else -> false
                }
            }

            //TODO rename "triggerEvent" to campaign. Rename related stuff
            val timeRightNow = System.currentTimeMillis()
            var lastCampaignViewTime = 0L
            triggerEvent.maxByOrNull { it.lastDisplayedTime }?.let {
                castledLogger.info("$TAG: max ${it.notificationId}")
                lastCampaignViewTime = it.lastDisplayedTime
            }
            triggerEvent.forEach { triggerEventModel ->
                CastledLogger.getInstance().debug("$TAG: DB trigger JSON: ${triggerEventModel.trigger}")
                if (!triggerEventModel.trigger.asJsonObject.isJsonNull
                    && triggerEventModel.trigger.asJsonObject.has("eventFilter")
                    && !triggerEventModel.trigger.asJsonObject.get("eventFilter").isJsonNull
                ){
                    CastledLogger.getInstance().debug("$TAG: DB trigger JSON(Condition Pass): ${triggerEventModel.trigger}")

                    CastledLogger.getInstance().debug("$TAG: timeRightNow: $timeRightNow, Event endTime: ${triggerEventModel.endTs}, startTime: ${triggerEventModel.startTs}")

                    // TODO: close gitHub-> https://github.com/dheerajbhaskar/castled-notifications-android/issues/54
                    if (triggerEventModel.endTs > timeRightNow
                        && triggerEventModel.displayLimit > triggerEventModel.timesDisplayed
                        && timeRightNow > (triggerEventModel.lastDisplayedTime + triggerEventModel.minIntervalBtwDisplays)
                        && timeRightNow > (lastCampaignViewTime + triggerEventModel.minIntervalBtwDisplaysGlobal)
//                        && triggerEventModel.minIntervalBtwDisplays >= (timeRightNow - triggerEventModel.lastDisplayedTime)
                    ){
                        val gson = GsonBuilder()
                            .registerTypeAdapter(EventFilter::class.java, EventFilterDeserializer())
                            .create()
                        val eventFilter: EventFilter =
                            gson.fromJson(
                                triggerEventModel.trigger.get("eventFilter").asJsonObject,
                                EventFilter::class.java
                            )
                        if (triggerParamsEvaluator.evaluate(eventParam, eventFilter as NestedEventFilter))
                            showOnScreenEvent.add(triggerEventModel)
                    } else {
                        CastledLogger.getInstance().debug("$TAG: ${triggerEventModel.notificationId} expired or crossed the display limit or minIntervalBtwDisplays not crossed")
                    }

                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val timeRightNow = Instant.now()
                        CastledLogger.getInstance().debug("$TAG: timeRightNow: ${timeRightNow.toEpochMilli()}, Event endTime: ${triggerEventModel.endTs}, startTime: ${triggerEventModel.startTs}")
                        if (timeRightNow.isBefore(Instant.ofEpochSecond(triggerEventModel.endTs))){
                            CastledLogger.getInstance().debug("$TAG: BEFORE")
                        } else {
                            CastledLogger.getInstance().debug("$TAG: AFTER")
                        }
                    }*/

                }
            }
            showOnScreenEvent
        }

    internal fun findAndLaunchDbTriggerEvent(context: Context) = CoroutineScope(Default).launch {
        val dbTriggerEvents = dbFetchCampaigns(context)
        CastledLogger.getInstance().debug("$TAG: findAndLaunchTriggerNotification: ${dbTriggerEvents.map { it.notificationId }}")
        launchTriggerEvent(context, dbTriggerEvents)
    }

    private fun launchTriggerEvent(
        context: Context,
        triggerEvents: List<CampaignModel>
    ) {

        if (triggerEvents.isNotEmpty()) {
            val event = triggerEvents.first()
            CoroutineScope(Main).launch {
                CastledLogger.getInstance().debug("$TAG: launchTriggerEvent: ")
                when (TriggerPopupDialog.getTriggerEventType(event)) {
                    TriggerEventConstants.Companion.TriggerEventType.MODAL -> {
                        launchModalInApp(context, event)
                    }
                    TriggerEventConstants.Companion.TriggerEventType.SLIDE_UP -> {
                        launchSlideUpTriggerNotification(context, event)
                    }
                    TriggerEventConstants.Companion.TriggerEventType.FULL_SCREEN -> {
                        launchFullScreenTriggerNotification(context, event)
                    }
                    else -> {}
                }
            }
        }

    }


/**
 * The below code is just to test the different Event Dialog on the screen. eventType is 0 or 1 or 2
 */
internal fun findAndLaunchTriggerEventForTest(context: Context, eventType: Int) =
    CoroutineScope(Main).launch {
        if (eventType == 0 || eventType == 1 || eventType == 2) {

            withContext(Default) {

                val triggerEventType = when (eventType) {
                    0 -> TriggerEventConstants.Companion.TriggerEventType.MODAL
                    1 -> TriggerEventConstants.Companion.TriggerEventType.FULL_SCREEN
                    2 -> TriggerEventConstants.Companion.TriggerEventType.SLIDE_UP
                    else -> TriggerEventConstants.Companion.TriggerEventType.NONE
                }

                val dbTriggerNotifications = dbFetchCampaigns(context)
                CastledLogger.getInstance().debug("$TAG: findAndLaunchTriggerNotification: ${dbTriggerNotifications.map { it.notificationId }}")

                if (dbTriggerNotifications.isNotEmpty()) {
                    val event =
                        dbTriggerNotifications.firstOrNull { TriggerPopupDialog.getTriggerEventType(it) == triggerEventType }
                    withContext(Main) {
                        when (TriggerPopupDialog.getTriggerEventType(event)) {
                            TriggerEventConstants.Companion.TriggerEventType.MODAL -> {
                                launchModalInApp(context, event!!)
                            }
                            TriggerEventConstants.Companion.TriggerEventType.SLIDE_UP -> {
                                launchSlideUpTriggerNotification(context, event!!)
                            }
                            TriggerEventConstants.Companion.TriggerEventType.FULL_SCREEN -> {
                                launchFullScreenTriggerNotification(context, event!!)
                            }
                            TriggerEventConstants.Companion.TriggerEventType.NONE -> {
//                                Toast.makeText(context, "No ${triggerEventType.name} event found in the database.", Toast.LENGTH_SHORT).show()
                                CastledLogger.getInstance().debug("$TAG: findAndLaunchTriggerEventForTest: No ${triggerEventType.name} event found in the database.")
                            }
                            else -> {}
                        }
                    }
                }

            }

        } else {
//            Toast.makeText(context, "Please enter 0 for MODAL, 1 for FULL_SCREEN or 2 for SLIDE_UP.", Toast.LENGTH_SHORT).show()
            CastledLogger.getInstance().debug("$TAG: findAndLaunchTriggerEventForTest: Please enter 0 for MODAL, 1 for FULL_SCREEN or 2 for SLIDE_UP.")
        }
    }

    /**
     *  The below code is just to test the different Event Dialog on the screen. eventType is 0 or 1 or 2
     */
    internal fun findAndLaunchTriggerEventForTest(context: Context, event: JsonObject) =
        CoroutineScope(Default).launch {

            CastledLogger.getInstance().debug("$TAG: selected event: $event")

            val eventLocal = CampaignModel(
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
                when (TriggerPopupDialog.getTriggerEventType(eventLocal)) {
                    TriggerEventConstants.Companion.TriggerEventType.MODAL -> {
                        launchModalInApp(context, eventLocal)
                    }
                    TriggerEventConstants.Companion.TriggerEventType.SLIDE_UP -> {
                        launchSlideUpTriggerNotification(context, eventLocal)
                    }
                    TriggerEventConstants.Companion.TriggerEventType.FULL_SCREEN -> {
                        launchFullScreenTriggerNotification(context, eventLocal)
                    }
                    TriggerEventConstants.Companion.TriggerEventType.NONE -> {}
                    else -> {}
                }
            }
        }

    private fun getDefaultNotification(): CampaignModel{
        val fs = JsonObject()
        fs.addProperty("imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png")
        fs.addProperty("defaultClickAction", "NONE")
        fs.addProperty("screenOverlayColor", "#f8ffbd")
        fs.addProperty("title", "Full screen title text.")
        fs.addProperty("titleFontColor", "#FFFFFF")
        fs.addProperty("titleFontSize", 18)
        fs.addProperty("titleBgColor", "#E74C3C")
        fs.addProperty("body", "Full screen message text.")
        fs.addProperty("bodyFontColor", "#FFFFFF")
        fs.addProperty("bodyFontSize", 12)
        fs.addProperty("bodyBgColor", "#039ADC")

        val modal = JsonObject()
        modal.addProperty("imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png")
        modal.addProperty("defaultClickAction", "NONE")
        modal.addProperty("screenOverlayColor", "#f8ffbd")
        modal.addProperty("title", "Summer sale is Back!")
        modal.addProperty("titleFontColor", "#FFFFFF")
        modal.addProperty("titleFontSize", 18)
        modal.addProperty("titleBgColor", "#E74C3C")
        modal.addProperty("body", "Full Screen \n" +
                "30% offer on Electronics, Cloths, Sports and other categories.")
        modal.addProperty("bodyFontColor", "#FFFFFF")
        modal.addProperty("bodyFontSize", 12)
        modal.addProperty("bodyBgColor", "#039ADC")

        val su = JsonObject()
        su.addProperty("imageUrl", "https://cdn.castled.io/logo/castled_multi_color_logo_only.png")
        su.addProperty("defaultClickAction", "NONE")
        su.addProperty("screenOverlayColor", "#ff99ff")
        su.addProperty("titleBgColor", "#E74C3C")
        su.addProperty("body", "Slide Up \n" +
                "30% offer on Electronics, Cloths, Sports and other categories.")
        su.addProperty("bodyFontColor", "#FFFFFF")
        su.addProperty("bodyFontSize", 12)
        su.addProperty("bodyBgColor", "#039ADC")

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

        fs.add("actionButtons", buttons)
        modal.add("actionButtons", buttons)

        val message = JsonObject()
        message.add("fs", fs)
        message.add("modal", modal)
        message.add("su", su)

        val trigger: JsonObject = JsonObject()

        return  CampaignModel(1, 1, 1L, "", 0L, 0L, 0, 0, 1, 1, 1, 1, 1, trigger, message)
    }

    private fun preparePopupHeader(modal: JsonObject) = PopupHeader(
        if(modal.get("title").isJsonNull) "" else modal.get("title").asString,
        modal.get("titleFontColor").asString,
        modal.get("titleFontSize").asFloat,
        modal.get("titleBgColor").asString
    )

    private fun preparePopupMessage(modal: JsonObject): PopupMessage{
        if (modal.has("bodyFontColor") && modal.has("bodyFontSize") && modal.has("bodyBgColor")){
        return PopupMessage(
            if (modal.get("body").isJsonNull) "" else modal.get("body").asString,
            modal.get("bodyFontColor").asString,
            modal.get("bodyFontSize").asFloat,
            modal.get("bodyBgColor").asString
        )
        } else if (modal.has("bgColor") && modal.has("fontSize") && modal.has("fontColor")){
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

    private fun preparePopupSecondaryButton(secondaryPopupButtonJson: JsonObject) = PopupSecondaryButton(
        secondaryPopupButtonJson.get("label").asString,
        secondaryPopupButtonJson.get("fontColor").asString,
        secondaryPopupButtonJson.get("buttonColor").asString,
        secondaryPopupButtonJson.get("borderColor").asString,
        if (secondaryPopupButtonJson.get("url").isJsonNull) "" else secondaryPopupButtonJson.get("url").asString
    )

    private fun launchModalInApp(context: Context, campaignModel: CampaignModel) {
        val message: JsonObject = campaignModel.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("modal")
        val buttons: JsonArray = modal.getAsJsonArray("actionButtons")

        if (buttons.size() < 2) {
            CastledLogger.getInstance().debug("$TAG: launchModalTriggerNotification: Event is not valid for notificationId ${campaignModel.notificationId}.")
            return
        }

        val buttonPrimary: JsonObject = buttons[0].asJsonObject
        val buttonSecondary: JsonObject = buttons[1].asJsonObject

        val eventClickActionData = JsonObject()
        eventClickActionData.addProperty("teamId", campaignModel.teamId)
        eventClickActionData.addProperty("sourceContext", campaignModel.sourceContext)

        DbOperation.dbUpdateCampaignLastDisplayedAndTimesDisplayed(context, campaignModel)
        initiateTriggerEventLogToCloud(prepareEventViewActionBodyData(campaignModel))

        TriggerPopupDialog.showDialog(
            context,
            campaignModel.autoDismissInterval,
            modal.get("screenOverlayColor").asString,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            if(modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            preparePopupPrimaryButton(buttonPrimary),
            preparePopupSecondaryButton(buttonSecondary),
            object : TriggerEventClickAction{
                override fun onTriggerEventAction(
                    triggerEventConstants: TriggerEventConstants.Companion.EventClickType
                ) {
                    when(triggerEventConstants) {
                        TriggerEventConstants.Companion.EventClickType.CLOSE_EVENT -> {
                            initiateTriggerEventLogToCloud(prepareEventCloseActionBodyData(campaignModel))
                        }
                        TriggerEventConstants.Companion.EventClickType.IMAGE_CLICK -> {

                            //if(modal.get("defaultClickAction").isJsonNull) "" else modal.get("defaultClickAction").asString
                            // NONE, SEND, CLICKED, DISCARDED, RECEIVED, FOREGROUND

                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = ""

                            if(!modal.get("url").isJsonNull)
                                intent.putExtra(Constants.EXTRA_URI, modal.get("url").asString)

                            if (!modal.get("defaultClickAction").isJsonNull)
                                intent.putExtra(Constants.EXTRA_ACTION, modal.get("defaultClickAction").asString)

//                            intent.putExtra(Constants.EXTRA_LABEL, "")
//                            intent.putExtra(Constants.EXTRA_KEY_VAL_PARAMS, "")

                            context.startActivity(intent)

                            initiateTriggerEventLogToCloud(prepareEventImageClickActionBodyData(campaignModel))
                        }
                        TriggerEventConstants.Companion.EventClickType.PRIMARY_BUTTON -> {

                            //                            if(modal.get("defaultClickAction").isJsonNull) "" else modal.get("defaultClickAction").asString
                            // NONE, SEND, CLICKED, DISCARDED, RECEIVED, FOREGROUND

                            CastledLogger.getInstance().info("buttonPrimary: $buttonPrimary")
                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = ""

                            if(!buttonPrimary.get("url").isJsonNull)
                                intent.putExtra(Constants.EXTRA_URI, buttonPrimary.get("url").asString)

                            if (!buttonPrimary.get("clickAction").isJsonNull)
                                intent.putExtra(Constants.EXTRA_ACTION, buttonPrimary.get("clickAction").asString)

                            if (!buttonPrimary.get("label").isJsonNull)
                                intent.putExtra(Constants.EXTRA_LABEL, buttonPrimary.get("label").asString)

                            if (!buttonPrimary.get("keyVals").isJsonNull)
                                intent.putExtra(Constants.EXTRA_KEY_VAL_PARAMS, buttonPrimary.get("keyVals").toString())

                            context.startActivity(intent)


                            initiateTriggerEventLogToCloud(prepareEventButtonClickActionBodyData(eventClickActionData, buttonPrimary))
                        }
                        TriggerEventConstants.Companion.EventClickType.SECONDARY_BUTTON -> {

                            CastledLogger.getInstance().info("buttonSecondary: $buttonSecondary")

                            val intent = Intent(context, CastledEventListener::class.java)
                            intent.action = ""

                            if(!buttonSecondary.get("url").isJsonNull)
                                intent.putExtra(Constants.EXTRA_URI, buttonSecondary.get("url").asString)

                            if (!buttonSecondary.get("clickAction").isJsonNull)
                                intent.putExtra(Constants.EXTRA_ACTION, buttonSecondary.get("clickAction").asString)

                            if (!buttonSecondary.get("label").isJsonNull)
                                intent.putExtra(Constants.EXTRA_LABEL, buttonSecondary.get("label").asString)

                            if (!buttonSecondary.get("keyVals").isJsonNull)
                                intent.putExtra(Constants.EXTRA_KEY_VAL_PARAMS, buttonSecondary.get("keyVals").toString())

                            context.startActivity(intent)


                            initiateTriggerEventLogToCloud(prepareEventButtonClickActionBodyData(eventClickActionData, buttonSecondary))
                        }
                        else -> {}
                    }

                }
            }
        )
    }

    private fun launchFullScreenTriggerNotification(context: Context, eventModel: CampaignModel) {
        val message: JsonObject = eventModel.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("fs")
        val buttons: JsonArray = modal.getAsJsonArray("actionButtons")
        val buttonPrimary: JsonObject = buttons[0].asJsonObject
        val buttonSecondary: JsonObject = buttons[1].asJsonObject

        val eventClickActionData = JsonObject()
        eventClickActionData.addProperty("teamId", eventModel.teamId)
        eventClickActionData.addProperty("sourceContext", eventModel.sourceContext)

        initiateTriggerEventLogToCloud(prepareEventViewActionBodyData(eventModel))

        TriggerPopupDialog.showFullscreenDialog(
            context,
            modal.get("screenOverlayColor").asString,
            preparePopupHeader(modal),
            preparePopupMessage(modal),
            if(modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            if(modal.get("defaultClickAction").isJsonNull) "" else modal.get("defaultClickAction").asString,
            preparePopupPrimaryButton(buttonPrimary),
            preparePopupSecondaryButton(buttonSecondary),
            object : TriggerEventClickAction{
                override fun onTriggerEventAction(
                    triggerEventConstants: TriggerEventConstants.Companion.EventClickType
                ) {
                    when(triggerEventConstants) {
                        TriggerEventConstants.Companion.EventClickType.CLOSE_EVENT -> {
                            eventClickActionData.addProperty("eventType", "DISCARDED")
                            initiateTriggerEventLogToCloud(prepareEventCloseActionBodyData(eventModel))
                        }
                        TriggerEventConstants.Companion.EventClickType.IMAGE_CLICK -> {
                            initiateTriggerEventLogToCloud(prepareEventImageClickActionBodyData(eventModel))
                        }
                        TriggerEventConstants.Companion.EventClickType.PRIMARY_BUTTON -> {
                            initiateTriggerEventLogToCloud(prepareEventButtonClickActionBodyData(eventClickActionData, buttonPrimary))
                        }
                        TriggerEventConstants.Companion.EventClickType.SECONDARY_BUTTON -> {
                            initiateTriggerEventLogToCloud(prepareEventButtonClickActionBodyData(eventClickActionData, buttonSecondary))
                        }
                        else -> {}
                    }
                }
            }
        )
    }

    private fun launchSlideUpTriggerNotification(context: Context, eventModel: CampaignModel) {
        CastledLogger.getInstance().debug("$TAG: notification: $eventModel")
        val message: JsonObject = eventModel.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("slideUp")
        CastledLogger.getInstance().debug("$TAG: slideUp: $modal")

        val eventClickActionData = JsonObject()
        eventClickActionData.addProperty("teamId", eventModel.teamId)
        eventClickActionData.addProperty("sourceContext", eventModel.sourceContext)

        initiateTriggerEventLogToCloud(prepareEventViewActionBodyData(eventModel))

        TriggerPopupDialog.showSlideUpDialog(
            context,
            modal.get("bgColor").asString,
            preparePopupMessage(modal),
            if(modal.get("imageUrl").isJsonNull) "" else modal.get("imageUrl").asString,
            if(modal.get("url").isJsonNull) "" else modal.get("url").asString,
            object : TriggerEventClickAction{
                override fun onTriggerEventAction(
                    triggerEventConstants: TriggerEventConstants.Companion.EventClickType
                ) {
                    when(triggerEventConstants) {
                        TriggerEventConstants.Companion.EventClickType.CLOSE_EVENT -> {
                            initiateTriggerEventLogToCloud(prepareEventCloseActionBodyData(eventModel))
                        }
                        TriggerEventConstants.Companion.EventClickType.IMAGE_CLICK -> {
                            initiateTriggerEventLogToCloud(prepareEventImageClickActionBodyData(eventModel))
                        }
                        TriggerEventConstants.Companion.EventClickType.PRIMARY_BUTTON -> {}
                        TriggerEventConstants.Companion.EventClickType.SECONDARY_BUTTON -> {}
                        else -> {}
                    }
                }
            }
        )
    }

    private fun prepareEventViewActionBodyData(eventModel: CampaignModel): JsonObject {
        val eventViewActionBodyData = JsonObject()
        eventViewActionBodyData.addProperty("teamId", eventModel.teamId)
        eventViewActionBodyData.addProperty("eventType", "VIEWED")
        eventViewActionBodyData.addProperty("sourceContext", eventModel.sourceContext)

        return eventViewActionBodyData
    }

    private fun prepareEventCloseActionBodyData(eventModel: CampaignModel): JsonObject {
        val eventViewActionBodyData = JsonObject()
        eventViewActionBodyData.addProperty("teamId", eventModel.teamId)
        eventViewActionBodyData.addProperty("eventType", "DISCARDED")
        eventViewActionBodyData.addProperty("sourceContext", eventModel.sourceContext)

        return eventViewActionBodyData
    }

    fun prepareEventImageClickActionBodyData(eventModel: CampaignModel): JsonObject{
        val message: JsonObject = eventModel.message.asJsonObject
        val modal: JsonObject =
            if (message.has("modal"))
                message.getAsJsonObject("modal")
            else if (message.has("fs"))
                message.getAsJsonObject("fs")
            else if (message.has("slideUp"))
                message.getAsJsonObject("slideUp")
            else JsonObject()

        val eventClickActionData = JsonObject()
        eventClickActionData.addProperty("teamId", eventModel.teamId)
        eventClickActionData.addProperty("sourceContext", eventModel.sourceContext)
        eventClickActionData.addProperty("eventType", "CLICKED")
        if (modal.has("defaultClickAction")){
            eventClickActionData.addProperty("actionType", if (modal.get("defaultClickAction").isJsonNull) null else modal.get("defaultClickAction").asString)
        } else if (modal.has("clickAction")){
            eventClickActionData.addProperty("actionType", if (modal.get("clickAction").isJsonNull) null else modal.get("clickAction").asString)
        }
        if (modal.has("url")){
            eventClickActionData.addProperty("actionUri", if (modal.get("url").isJsonNull) null else modal.get("url").asString)
        }

       return eventClickActionData
    }

    private fun prepareEventButtonClickActionBodyData(jsonObjectBody: JsonObject, jsonButton: JsonObject):JsonObject {

        jsonObjectBody.addProperty("eventType", "CLICKED")
        jsonObjectBody.addProperty("actionType", jsonButton.get("clickAction").asString)
        jsonObjectBody.addProperty("actionUri", if (jsonButton.get("url").isJsonNull) null else jsonButton.get("url").asString)
        jsonObjectBody.addProperty("btnLabel", jsonButton.get("label").asString)
        CastledLogger.getInstance().debug("$TAG: Event Action API Body: $jsonObjectBody")
        return jsonObjectBody
    }

    internal suspend fun dbFetchCampaigns(context: Context): List<CampaignModel> =
        withContext(Default) {
            val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            db.getCampaignsFromDb()
        }

    private suspend fun dbDeleteTriggerEvents(context: Context): Int =
        withContext(Default) {
            val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            db.deleteDbCampaigns()
        }

    private suspend fun dbInsertTriggerEvents(
        context: Context,
        notifications: List<CampaignModel>
    ) =
        withContext(Default) {
            val db = CampaignDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            db.insertCampaignsIntoDb(notifications)
        }

    private fun showApiLog(cloudEventResponse: Response<List<CampaignModelApi>>) {
        CastledLogger.getInstance().debug("$TAG: ************* fetchCloudEvents FETCHED *************\n")
        CastledLogger.getInstance().debug("$TAG: 1. isSuccessful: ${cloudEventResponse.isSuccessful}")
//        CastledLogger.getInstance().debug("$TAG: 2. Body: ${cloudEventResponse.body()}")
        CastledLogger.getInstance().debug("$TAG: 3. Code: ${cloudEventResponse.code()}")
        CastledLogger.getInstance().debug("$TAG: 4. Message: ${cloudEventResponse.message()}")
//        CastledLogger.getInstance().debug("$TAG: 5. Headers: ${cloudEventResponse.headers()}")
//        CastledLogger.getInstance().debug("$TAG: 6. Raw: ${cloudEventResponse.raw()}")
        CastledLogger.getInstance().debug("$TAG: 7. Body(Size): ${cloudEventResponse.body()?.size} ")
        CastledLogger.getInstance().debug("$TAG: ************* fetchCloudEvents FETCHED DONE *************\n")
    }
}
