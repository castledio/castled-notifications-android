package io.castled.android.notifications.inapp.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import io.castled.android.notifications.R
import io.castled.android.notifications.inapp.InAppMessageUtils
import io.castled.android.notifications.inapp.models.InAppMessageTemplateType
import io.castled.android.notifications.inapp.models.InAppMessageType
import io.castled.android.notifications.store.models.Campaign
import kotlinx.serialization.json.jsonPrimitive

object InAppViewFactory {

    @SuppressLint("InflateParams")
    fun createView(context: Context, inAppMessage: Campaign): InAppBaseViewLayout? {
        val messageType: InAppMessageType = InAppMessageUtils.getMessageType(inAppMessage.message)
        val msgBody = InAppMessageUtils.getMessageBody(inAppMessage.message)
        val messageTemplateType: InAppMessageTemplateType =
            InAppMessageUtils.getMessageTemplateType(
                (msgBody["type"]?.jsonPrimitive?.content ?: "")
            )
        return LayoutInflater.from(context)
            .inflate(
                R.layout.castled_inapp_modal_text_buttons,
                null
            ) as InAppBaseViewLayout
        return when (messageType) {
            InAppMessageType.MODAL ->
                when (messageTemplateType) {
                    InAppMessageTemplateType.DEFAULT ->
                        LayoutInflater.from(context)
                            .inflate(
                                R.layout.castled_inapp_modal_default,
                                null
                            ) as InAppBaseViewLayout


                    InAppMessageTemplateType.CUSTOM_HTML ->
                        LayoutInflater.from(context)
                            .inflate(R.layout.castled_inapp_modal_html, null) as InAppBaseViewLayout

                    else ->
                        null
                }

            InAppMessageType.FULL_SCREEN ->
                when (messageTemplateType) {
                    InAppMessageTemplateType.DEFAULT ->
                        LayoutInflater.from(context)
                            .inflate(
                                R.layout.castled_inapp_fullscreen_default,
                                null
                            ) as InAppBaseViewLayout

                    InAppMessageTemplateType.IMG_AND_BUTTONS ->
                        LayoutInflater.from(context)
                            .inflate(
                                R.layout.castled_inapp_fullscreen_image_buttons,
                                null
                            ) as InAppBaseViewLayout

                    InAppMessageTemplateType.CUSTOM_HTML ->
                        LayoutInflater.from(context)
                            .inflate(R.layout.castled_inapp_fs_html, null) as InAppBaseViewLayout

                    else ->
                        null

                }

            InAppMessageType.BANNER ->
                when (messageTemplateType) {
                    InAppMessageTemplateType.DEFAULT ->
                        LayoutInflater.from(context)
                            .inflate(
                                R.layout.castled_inapp_banner_default,
                                null
                            ) as InAppBaseViewLayout

                    else ->
                        null

                }

        }
    }
}