package io.castled.inAppTriggerEvents.trigger

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.castled.inAppTriggerEvents.database.DatabaseBuilder
import io.castled.inAppTriggerEvents.database.TriggerEventDatabaseHelperImpl
import io.castled.inAppTriggerEvents.eventConsts.TriggerEventConstants
import io.castled.inAppTriggerEvents.models.TriggerEventModel
import io.castled.inAppTriggerEvents.requests.ServiceGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.*


private const val TAG = "TriggerEvent"

internal class TriggerEvent private constructor(){

    companion object {
        private lateinit var triggerEvent: TriggerEvent

        @JvmStatic
        internal fun getInstance(): TriggerEvent{
            return if (this::triggerEvent.isInitialized) triggerEvent else TriggerEvent()
        }
    }


    internal fun fetchAndSaveTriggerEvents(context: Context) {

        CoroutineScope(Main).launch {
            val notifications = requestTriggerEventsFromCloud(context)
            if (notifications.isNotEmpty()) {
                Log.d(TAG, "${notifications.size} notifications fetched from server.[${notifications.map { it.notificationId }}]")

                val noOfRowDeleted = dbDeleteTriggerEvents(context)
                Log.d(TAG, "$noOfRowDeleted notifications deleted from database.")

                val rows = dbInsertTriggerEvents(context, notifications)
                Log.d(TAG, "inserted into db: ${rows.toList()}")

            } else Toast.makeText(context, "Notification fetch failed.", Toast.LENGTH_SHORT).show()
        }

    }


    private suspend fun requestTriggerEventsFromCloud(context: Context?): List<TriggerEventModel> {

        /*return withContext(IO) {
            val notificationResponse = ServiceGenerator.requestApi().makeNotificationQuery("<api-key>", "support-1@castled.io")
            (if (notificationResponse.isSuccessful && notificationResponse.body() != null)
                notificationResponse.body()
            else
                emptyList<NotificationModel>())!!
        }*/

        return withContext(IO) {
            val eventsResponse = ServiceGenerator.requestApi()
                .makeNotificationQuery("<api-key>", "support-1@castled.io")
//            showApiLog(notificationResponse)
            if (eventsResponse.isSuccessful && eventsResponse.body() != null) {
                eventsResponse.body()
            } else {
                context?.let {
                    withContext(Main) {
                        Toast.makeText(
                            context,
                            "Error while getting data.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                emptyList()
            }
        } as List<TriggerEventModel>
    }

    private suspend fun updateTriggerEventLogToCloud(eventBody: JsonObject): String {

        return withContext(IO) {
            val eventsResponse = ServiceGenerator.requestApi()
                .logEventView("test-3b229735-04ae-455f-a5d4-20a89c092927", eventBody)

            /*Log.d(TAG, "1. ->: ${eventsResponse.body()}")
            Log.d(TAG, "2. ->: ${eventsResponse.isSuccessful}")
            Log.d(TAG, "3. ->: ${eventsResponse.message()}")
            Log.d(TAG, "4. ->: ${eventsResponse.code()}")
            Log.d(TAG, "5. ->: ${eventsResponse.raw()}")
            Log.d(TAG, "6. ->: ${eventsResponse.headers()}")
            Log.d(TAG, "7. ->: $eventBody")

            (if (eventsResponse.isSuccessful && eventsResponse.body() != null){
                "eventsResponse.body()"
                eventsResponse.body()
            } else {
                ""
            }).toString()*/

            eventsResponse.raw().toString()
        }
    }

    private fun initiateTriggerEventLogToCloud(eventBody: JsonObject){

        CoroutineScope(Default).launch {
//            val d = TimeZone.getDefault()
//            Log.d(TAG, "1->>: ${Calendar.getInstance().timeZone.getDisplayName(false, TimeZone.SHORT)}")
//            Log.d(TAG, "2: ${Calendar.getInstance()}")
//            Log.d(TAG, "3: ${context.getResources().getConfiguration().locale.getCountry()}")

            eventBody.addProperty("ts", System.currentTimeMillis())
            eventBody.addProperty("tz", TimeZone.getDefault().displayName)

            val response = updateTriggerEventLogToCloud(eventBody)
            Log.d(TAG, "********* ## Log Trigger Event to Cloud ## *********")
            Log.d(TAG, "Body(raw): $eventBody\nResponse: $response")
            Log.d(TAG, "*********  *********  *********  *********")
        }
    }

    private var notificationObserver: LiveData<List<TriggerEventModel>>? = null
    fun observeDatabaseNotification(context: Context, viewLifecycleOwner: LifecycleOwner) {
        CoroutineScope(Default).launch {
            if (notificationObserver == null) {
                val db = TriggerEventDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                notificationObserver = db.getLiveDataTriggerEventsFromDb()
            }

            if (notificationObserver?.hasObservers() != null){
                val db = TriggerEventDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                Log.d(TAG, "hasActiveObservers: ${db.getLiveDataTriggerEventsFromDb().hasActiveObservers()}")
                Log.d(TAG, "hasObservers: ${db.getLiveDataTriggerEventsFromDb().hasObservers()}")
            }

            if (notificationObserver?.hasObservers() == null || !notificationObserver!!.hasObservers()) {
                withContext(Main) {
                    val db = TriggerEventDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
                    db.getLiveDataTriggerEventsFromDb().observe(viewLifecycleOwner) { it ->
                        Log.d(TAG, "observeDatabaseNotification: ${it?.size} notifications added/replaced.")
                        it.forEach {
                            Log.d(TAG, "observeDatabaseNotification: ${it.notificationId}")
                        }
                    }
                }
            }
        }
    }

    private fun startObservingTriggerNotification(context: Context) {
       CoroutineScope(Main).launch {
           fetchAndSaveTriggerEvents(context)
           findAndLaunchTriggerEvent(context)
       }
    }

    internal fun findAndLaunchTriggerEvent(context: Context) = CoroutineScope(Main).launch {
        withContext(Default) {

            val dbTriggerNotifications = dbFetchTriggerEvents(context)
            Log.d(TAG, "findAndLaunchTriggerNotification: ${dbTriggerNotifications.map { it.notificationId }}")

            if (dbTriggerNotifications.isNotEmpty()) {
                val event = dbTriggerNotifications.first()
                withContext(Main){
                    when (TriggerPopupDialog.getTriggerEventType(event)) {
                        TriggerEventConstants.Companion.TriggerEventType.MODAL -> {
                            launchModalTriggerNotification(context, event)
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
    }


//The below code is just to test the different Event Dialog on the screen. eventType is 0 or 1 or 2
    internal fun findAndLaunchTriggerEventForTest(context: Context, eventType: Int) = CoroutineScope(Main).launch {
        withContext(Default) {

            val dbTriggerNotifications = dbFetchTriggerEvents(context)
            Log.d(TAG, "findAndLaunchTriggerNotification: ${dbTriggerNotifications.map { it.notificationId }}")

            if (dbTriggerNotifications.isNotEmpty()) {
                val event = dbTriggerNotifications[eventType]
                withContext(Main){
                    when (TriggerPopupDialog.getTriggerEventType(event)) {
                        TriggerEventConstants.Companion.TriggerEventType.MODAL -> {
                            launchModalTriggerNotification(context, event)
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
    }

    private fun getDefaultNotification(): TriggerEventModel{
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

        return  TriggerEventModel(1L, 1, 1L, "",0L, 0L, 0, trigger, message)
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

    private fun launchModalTriggerNotification(context: Context, eventModel: TriggerEventModel) {
        val message: JsonObject = eventModel.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("modal")
        val buttons: JsonArray = modal.getAsJsonArray("actionButtons")
        val buttonPrimary: JsonObject = buttons[0].asJsonObject
        val buttonSecondary: JsonObject = buttons[1].asJsonObject

        val eventClickActionData = JsonObject()
        eventClickActionData.addProperty("teamId", eventModel.teamId)
        eventClickActionData.addProperty("sourceContext", eventModel.sourceContext)

        initiateTriggerEventLogToCloud(prepareEventViewActionBodyData(eventModel))

        TriggerPopupDialog.showDialog(
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
                            initiateTriggerEventLogToCloud(prepareEventCloseActionBodyData(eventModel))
                        }
                        TriggerEventConstants.Companion.EventClickType.IMAGE_CLICK -> {
                            initiateTriggerEventLogToCloud(prepareEventCloseActionBodyData(eventModel))
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



    private fun launchFullScreenTriggerNotification(context: Context, eventModel: TriggerEventModel) {
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
//                            Log.d(TAG, "Event Action API Body: $jsonObjectBody")
                            initiateTriggerEventLogToCloud(eventClickActionData)
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

    private fun launchSlideUpTriggerNotification(context: Context, eventModel: TriggerEventModel) {
        Log.d(TAG, "notification: $eventModel")
        val message: JsonObject = eventModel.message.asJsonObject
        val modal: JsonObject = message.getAsJsonObject("slideUp")
        Log.d(TAG, "slideUp: $modal")

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

    private fun prepareEventViewActionBodyData(eventModel: TriggerEventModel): JsonObject {
        val eventViewActionBodyData = JsonObject()
        eventViewActionBodyData.addProperty("teamId", eventModel.teamId)
        eventViewActionBodyData.addProperty("eventType", "VIEWED")
        eventViewActionBodyData.addProperty("sourceContext", eventModel.sourceContext)

        return eventViewActionBodyData
    }

    private fun prepareEventCloseActionBodyData(eventModel: TriggerEventModel): JsonObject {
        val eventViewActionBodyData = JsonObject()
        eventViewActionBodyData.addProperty("teamId", eventModel.teamId)
        eventViewActionBodyData.addProperty("eventType", "DISCARDED")
        eventViewActionBodyData.addProperty("sourceContext", eventModel.sourceContext)

        return eventViewActionBodyData
    }

    fun prepareEventImageClickActionBodyData(eventModel: TriggerEventModel): JsonObject{
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
        Log.d(TAG, "Event Action API Body: $jsonObjectBody")
        return jsonObjectBody
    }

    private suspend fun dbFetchTriggerEvents(context: Context): List<TriggerEventModel> =
        withContext(Default) {
            val db = TriggerEventDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            db.getTriggerEventsFromDb()
        }

    private suspend fun dbDeleteTriggerEvents(context: Context): Int =
        withContext(Default) {
            val db = TriggerEventDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            db.deleteDbTriggerEvents()
        }

    private suspend fun dbInsertTriggerEvents(
        context: Context,
        notifications: List<TriggerEventModel>
    ) =
        withContext(Default) {
            val db = TriggerEventDatabaseHelperImpl(DatabaseBuilder.getInstance(context))
            db.insertTriggerEventsIntoDb(notifications)
        }

    private fun showApiLog(notificationResponse: Response<List<TriggerEventModel>>) {
        Log.d(TAG, "************* fetchNotification FETCHED *************\n")
        Log.d(TAG, "1. isSuccessful: ${notificationResponse.isSuccessful}")
        Log.d(TAG, "2. Body: ${notificationResponse.body()}")
        Log.d(TAG, "3. Code: ${notificationResponse.code()}")
        Log.d(TAG, "4. Message: ${notificationResponse.message()}")
        Log.d(TAG, "5. Headers: ${notificationResponse.headers()}")
        Log.d(TAG, "6. Raw: ${notificationResponse.raw()}")
        Log.d(TAG, "7. ${notificationResponse.body()?.size} ")
        Log.d(TAG, "************* fetchNotification FETCHED DONE *************\n")
    }
}
