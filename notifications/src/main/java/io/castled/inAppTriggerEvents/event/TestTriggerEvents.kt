package io.castled.inAppTriggerEvents.event

import android.content.Context
import io.castled.inAppTriggerEvents.trigger.PopupHeader
import io.castled.inAppTriggerEvents.trigger.PopupMessage
import io.castled.inAppTriggerEvents.trigger.PopupPrimaryButton
import io.castled.inAppTriggerEvents.trigger.PopupSecondaryButton
import io.castled.inAppTriggerEvents.trigger.TriggerEvent
import io.castled.inAppTriggerEvents.trigger.TriggerPopupDialog

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
            popupSecondaryButton
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
            popupSecondaryButton
        )
    }

    fun showSlideUpDialog(
        context: Context,
        popUpBackgroundColor: String,
        popupMessage: PopupMessage,
        imageUrl:String,
        urlForOnClickOnImage: String
    ) {
        showSlideUpDialog(context,
            popUpBackgroundColor,
            popupMessage,
            imageUrl,
            urlForOnClickOnImage
        )
    }

    fun fetchAndSaveTriggerEvents(context: Context){
        TriggerEvent.getInstance().fetchAndSaveTriggerEvents(context)
    }

    fun findAndLaunchTriggerEvent(context: Context){
        TriggerEvent.getInstance().findAndLaunchTriggerEvent(context)
    }
}