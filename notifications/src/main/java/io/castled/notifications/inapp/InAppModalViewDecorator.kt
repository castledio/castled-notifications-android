package io.castled.notifications.inapp

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import io.castled.notifications.inapp.views.InAppModalViewLayout
import io.castled.notifications.inapp.views.InAppViewUtils
import io.castled.notifications.store.models.Campaign

// TODO: Convert Campaign to InAppMessage

internal class InAppModalViewDecorator(
    val context: Context,
    private val inAppModalViewLayout: InAppModalViewLayout,
    private val inAppMessage: Campaign,
) : InAppViewDecorator {

    private var dialog = Dialog(context)
    private val inAppViewLifecycleListener = InAppLifeCycleListenerImpl(context)

    init {
        addListenerClickCallbacks()
    }

    private fun addListenerClickCallbacks() {
        val msgBody = InAppMessageUtils.getMessageBody(inAppMessage.message)

        inAppModalViewLayout.viewContainer?.setOnClickListener {
            inAppViewLifecycleListener.onClicked(inAppMessage)
        }

        inAppModalViewLayout.primaryButton?.setOnClickListener {
            inAppViewLifecycleListener.onButtonClicked(
                this,
                inAppMessage,
                InAppViewUtils.getPrimaryButtonViewParams(msgBody)
            )
        }
        inAppModalViewLayout.secondaryButton?.setOnClickListener {
            inAppViewLifecycleListener.onButtonClicked(
                this,
                inAppMessage,
                InAppViewUtils.getSecondaryButtonViewParams(msgBody)
            )
        }
        inAppModalViewLayout.closeButton?.setOnClickListener {
            inAppViewLifecycleListener.onCloseButtonClicked(this, inAppMessage)
        }
    }

    override fun show() {
        dialog.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(inAppModalViewLayout)
            show()
        }
        // TODO: Handle auto dismiss
        inAppViewLifecycleListener.onDisplayed(inAppMessage)
    }

    override fun close() {
        dialog.dismiss()
        inAppViewLifecycleListener.onDismissed(inAppMessage)
    }

}

