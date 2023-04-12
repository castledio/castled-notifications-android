package io.castled.notifications.inapp.test

import android.content.Context
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.castled.notifications.inapp.InAppController
import io.castled.notifications.inapp.models.consts.InAppConstants
import io.castled.notifications.inapp.service.InAppRepository
import io.castled.notifications.inapp.trigger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    inAppConstants: InAppConstants.Companion.EventClickType
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
                    inAppConstants: InAppConstants.Companion.EventClickType
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
                    inAppConstants: InAppConstants.Companion.EventClickType
                ) {}

            }
        )
    }

    fun fetchDbTriggerEvents(context: Context, callback: (JsonArray) -> Unit){
        CoroutineScope(Dispatchers.Default).launch {
            val dbData = inAppRepository.getCampaigns()
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
        inAppController.findAndLaunchInAppForTest(context, eventType)
    }

    fun showDbTriggerEventDialog(context: Context, eventType: JsonObject){
        inAppController.findAndLaunchInAppForTest(context, eventType)
    }

    fun fetchAndSaveTriggerEvents(context: Context){
        CoroutineScope(Default).launch {
            inAppController.fetchAndSaveLiveCampaigns()
        }
    }

    fun findAndLaunchTriggerEvent(context: Context){
    }
}