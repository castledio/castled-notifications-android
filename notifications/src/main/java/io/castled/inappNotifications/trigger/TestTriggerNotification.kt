package io.castled.inappNotifications.trigger

import android.content.Context

class TestTriggerNotification private constructor(){
    companion object {
        private lateinit var testTriggerNotification: TestTriggerNotification

        @JvmStatic
        fun getInstance(): TestTriggerNotification{
            return if (this::testTriggerNotification.isInitialized) testTriggerNotification else TestTriggerNotification()
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

    fun fetchAndSaveTriggerNotification(context: Context){
        NotificationTrigger.getInstance().fetchAndSaveTriggerNotification(context)
    }
}