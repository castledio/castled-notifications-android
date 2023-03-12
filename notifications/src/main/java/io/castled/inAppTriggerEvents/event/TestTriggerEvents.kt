package io.castled.inAppTriggerEvents.event

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.castled.inAppTriggerEvents.eventConsts.TriggerEventConstants
import io.castled.inAppTriggerEvents.trigger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TestTriggerEvents"
class TestTriggerEvents private constructor(){
    companion object {
        private lateinit var testTriggerEvents: TestTriggerEvents

        @JvmStatic
        fun getInstance(): TestTriggerEvents {
            return if (this::testTriggerEvents.isInitialized) testTriggerEvents else TestTriggerEvents()
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
        TriggerPopupDialog.showDialog(
            context,
            10,
            popUpBackgroundColor,
            popUpHeader,
            popupMessage,
            imageUrl,
            popupPrimaryButton,
            popupSecondaryButton,
            object : TriggerEventClickAction {
                override fun onTriggerEventAction(
                    triggerEventConstants: TriggerEventConstants.Companion.EventClickType
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
        TriggerPopupDialog.showFullscreenDialog(
            context,
            popUpBackgroundColor,
            popUpHeader,
            popupMessage,
            imageUrl,
            urlForOnClickOnImage,
            popupPrimaryButton,
            popupSecondaryButton,
            object : TriggerEventClickAction {
                override fun onTriggerEventAction(
                    triggerEventConstants: TriggerEventConstants.Companion.EventClickType
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
        TriggerPopupDialog.showSlideUpDialog(context,
            popUpBackgroundColor,
            popupMessage,
            imageUrl,
            urlForOnClickOnImage,
            object : TriggerEventClickAction {
                override fun onTriggerEventAction(
                    triggerEventConstants: TriggerEventConstants.Companion.EventClickType
                ) {}

            }
        )
    }

    fun fetchDbTriggerEvents(context: Context, callback: (JsonArray) -> Unit){
        CoroutineScope(Dispatchers.Default).launch {
            val dbData = TriggerEvent.getInstance().dbFetchCampaigns(context)
            val jsonArray = JsonArray()
            dbData.forEach {
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", it.id)
                jsonObject.addProperty("sourceContext", it.sourceContext)
                jsonObject.addProperty("notificationId", it.notificationId)
                jsonObject.addProperty("teamId", it.teamId)
                jsonObject.addProperty("endTs", it.endTs)
                jsonObject.addProperty("startTs", it.startTs)
                jsonObject.addProperty("ttl", it.ttl)
                jsonObject.addProperty("displayLimit", it.displayLimit)
                jsonObject.addProperty("timesDisplayed", it.timesDisplayed)
                jsonObject.addProperty("minIntervalBtwDisplays", it.minIntervalBtwDisplays)
                jsonObject.addProperty("lastDisplayedTime", it.lastDisplayedTime)
                jsonObject.addProperty("minIntervalBtwDisplaysGlobal", it.minIntervalBtwDisplaysGlobal)
                jsonObject.addProperty("autoDismissInterval", it.autoDismissInterval)
                jsonObject.add("message", it.message.asJsonObject)
                jsonObject.add("trigger", it.trigger.asJsonObject)

                jsonArray.add(jsonObject)
            }
            withContext(Main){
                callback.invoke(jsonArray)
            }

        }
    }

    fun showDbTriggerEventDialog(context: Context, eventType: Int){
        TriggerEvent.getInstance().findAndLaunchTriggerEventForTest(context, eventType)
    }

    fun showDbTriggerEventDialog(context: Context, eventType: JsonObject){
        TriggerEvent.getInstance().findAndLaunchTriggerEventForTest(context, eventType)
    }

    fun fetchAndSaveTriggerEvents(context: Context){
        TriggerEvent.getInstance().fetchAndSaveTriggerEvents(context)
    }

    fun findAndLaunchTriggerEvent(context: Context){
        TriggerEvent.getInstance().findAndLaunchDbTriggerEvent(context)
    }
}