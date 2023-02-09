package io.castled.inAppTriggerEvents.event

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import io.castled.inAppTriggerEvents.eventConsts.TriggerEventConstants
import io.castled.inAppTriggerEvents.trigger.*
import io.castled.inAppTriggerEvents.trigger.TriggerEvent
import io.castled.inAppTriggerEvents.trigger.TriggerPopupDialog

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

    fun showDbTriggerEventDialog(context: Context, eventType: Int){
        TriggerEvent.getInstance().findAndLaunchTriggerEventForTest(context, eventType)
    }

    fun fetchAndSaveTriggerEvents(context: Context){
        TriggerEvent.getInstance().fetchAndSaveTriggerEvents(context)
    }

    fun findAndLaunchTriggerEvent(context: Context){
        TriggerEvent.getInstance().findAndLaunchTriggerEvent(context)
    }
}