package io.castled.notifications.inapp.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import io.castled.notifications.R
import io.castled.notifications.inapp.models.InAppMessageType

object InAppViewFactory {

    @SuppressLint("InflateParams")
    fun createView(context: Context, messageType: InAppMessageType) : View {
        return when(messageType) {
            InAppMessageType.MODAL ->
                LayoutInflater.from(context).inflate(R.layout.castled_inapp_modal_default, null)
            else -> {
                TODO("Not implemented")
            }
        }
    }
}