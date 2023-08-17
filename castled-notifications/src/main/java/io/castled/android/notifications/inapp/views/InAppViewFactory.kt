package io.castled.android.notifications.inapp.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import io.castled.android.notifications.R
import io.castled.android.notifications.inapp.models.InAppMessageType

object InAppViewFactory {

    @SuppressLint("InflateParams")
    fun createView(context: Context, messageType: InAppMessageType): InAppBaseViewLayout {
        return LayoutInflater.from(context)
            .inflate(R.layout.castled_inapp_modal_html, null) as InAppBaseViewLayout

        return when (messageType) {
            InAppMessageType.MODAL ->
                LayoutInflater.from(context)
                    .inflate(R.layout.castled_inapp_modal_default, null) as InAppBaseViewLayout

            InAppMessageType.FULL_SCREEN ->
                LayoutInflater.from(context)
                    .inflate(R.layout.castled_inapp_fullscreen_default, null) as InAppBaseViewLayout

            InAppMessageType.BANNER ->
                LayoutInflater.from(context)
                    .inflate(R.layout.castled_inapp_banner_default, null) as InAppBaseViewLayout
        }
    }
}