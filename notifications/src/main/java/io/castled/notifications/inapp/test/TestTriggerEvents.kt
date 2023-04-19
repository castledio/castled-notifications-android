package io.castled.notifications.inapp.test

import android.content.Context
import io.castled.notifications.inapp.InAppController
import io.castled.notifications.inapp.models.consts.InAppConstants
import io.castled.notifications.inapp.service.InAppRepository
import io.castled.notifications.inapp.trigger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class TestTriggerEvents private constructor(context: Context){

    private val inAppController = InAppController(context)
    private val inAppRepository = InAppRepository(context)

    companion object {
        private lateinit var testTriggerEvents: TestTriggerEvents

        @JvmStatic
        fun getInstance(context: Context): TestTriggerEvents {
            if (!this::testTriggerEvents.isInitialized) {
                testTriggerEvents = TestTriggerEvents(context)
            }
            return testTriggerEvents
        }
    }

    fun showDialog(
        context: Context,
        popUpBackgroundColor: String,
        popUpHeader: PopupHeader,
        popupMessage: PopupMessage,
        imageUrl:String,
        urlForOnClickOnImage: String,
        popupPrimaryButton: PopupPrimaryButton,
        popupSecondaryButton: PopupSecondaryButton
    ) {
        InAppPopupDialog.showDialog(
            context,
            10,
            popUpBackgroundColor,
            popUpHeader,
            popupMessage,
            imageUrl,
            popupPrimaryButton,
            popupSecondaryButton,
            object : InAppClickAction {
                override fun onTrigger(
                    clickType: InAppConstants.Companion.EventClickType
                ) { }

            }
        )
    }

    fun showFullscreenDialog(
        context: Context,
        popUpBackgroundColor: String,
        popUpHeader: PopupHeader,
        popupMessage: PopupMessage,
        imageUrl:String,
        urlForOnClickOnImage: String,
        popupPrimaryButton: PopupPrimaryButton,
        popupSecondaryButton: PopupSecondaryButton
    ) {
        InAppPopupDialog.showFullscreenDialog(
            context,
            popUpBackgroundColor,
            popUpHeader,
            popupMessage,
            imageUrl,
            urlForOnClickOnImage,
            popupPrimaryButton,
            popupSecondaryButton,
            object : InAppClickAction {
                override fun onTrigger(
                    clickType: InAppConstants.Companion.EventClickType
                ) {}

            }
        )
    }

    fun showSlideUpDialog(
        context: Context,
        popUpBackgroundColor: String,
        popupMessage: PopupMessage,
        imageUrl:String,
        urlForOnClickOnImage: String
    ) {
        InAppPopupDialog.showSlideUpDialog(context,
            popUpBackgroundColor,
            popupMessage,
            imageUrl,
            urlForOnClickOnImage,
            object : InAppClickAction {
                override fun onTrigger(
                    clickType: InAppConstants.Companion.EventClickType
                ) {}

            }
        )
    }

    fun fetchDbTriggerEvents(context: Context, callback: (JsonArray) -> Unit){
        CoroutineScope(Default).launch {
            val dbData = inAppRepository.getCampaigns()
            val jsonArray = mutableListOf<JsonElement>()
            dbData.forEach {
                val jsonMap = mutableMapOf<String, JsonElement>()
                jsonMap["id"] = JsonPrimitive(it.id)
                jsonMap["sourceContext"] = JsonPrimitive(it.sourceContext)
                jsonMap["notificationId"] = JsonPrimitive(it.notificationId)
                jsonMap["teamId"] = JsonPrimitive(it.teamId)
                jsonMap["endTs"] = JsonPrimitive(it.endTs)
                jsonMap["startTs"] = JsonPrimitive(it.startTs)
                jsonMap["ttl"] = JsonPrimitive(it.ttl)
                jsonMap["displayLimit"] = JsonPrimitive(it.displayConfig.displayLimit)
                jsonMap["timesDisplayed"] = JsonPrimitive(it.timesDisplayed)
                jsonMap["minIntervalBtwDisplays"] = JsonPrimitive(it.displayConfig.minIntervalBtwDisplays)
                jsonMap["lastDisplayedTime"] = JsonPrimitive(it.lastDisplayedTime)
                jsonMap["minIntervalBtwDisplaysGlobal"] = JsonPrimitive(it.displayConfig.minIntervalBtwDisplaysGlobal)
                jsonMap["autoDismissInterval"] = JsonPrimitive(it.displayConfig.autoDismissInterval)
                jsonMap["message"] = it.message
                jsonMap["trigger"] = it.trigger
                jsonArray.add(JsonObject(jsonMap))
            }
            withContext(Main){
                callback.invoke(JsonArray(jsonArray))
            }

        }
    }

    fun showDbTriggerEventDialog(context: Context, eventType: Int){
        inAppController.findAndLaunchInAppForTest(context, eventType)
    }

    fun showDbTriggerEventDialog(context: Context, eventType: JsonObject){
        // inAppController.findAndLaunchInAppForTest(context, eventType)
    }

    fun fetchAndSaveTriggerEvents(context: Context){
        CoroutineScope(Default).launch {
            inAppController.refreshLiveCampaigns()
        }
    }

    fun findAndLaunchTriggerEvent(context: Context){
    }
}