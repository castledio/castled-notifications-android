package io.castled.android.notifications.inapp

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Base64
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import com.example.javascriptsample.JavaScriptInterface
import io.castled.android.notifications.inapp.models.InAppMessageType
import io.castled.android.notifications.inapp.views.InAppBaseViewLayout
import io.castled.android.notifications.inapp.views.InAppViewFactory
import io.castled.android.notifications.inapp.views.InAppViewUtils
import io.castled.android.notifications.inapp.views.InAppWebViewLayout
import io.castled.android.notifications.inapp.views.toActionParams
import io.castled.android.notifications.store.models.Campaign
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

// TODO: Convert Campaign to InAppMessage

internal class InAppViewDecorator(
    val context: Context,
    private val inAppMessage: Campaign,
) : InAppViewBaseDecorator {

    private var dialog = Dialog(context)
    private val inAppViewLayout: InAppBaseViewLayout? =
        InAppViewFactory.createView(context, inAppMessage)
    private val inAppViewLifecycleListener = InAppLifeCycleListenerImpl(context)

    init {
        if (inAppViewLayout != null) {
            inAppViewLayout?.updateViewParams(inAppMessage)
            if (inAppViewLayout?.webView == null) {
                addListenerClickCallbacks()
            } else {
                loadJSInterface()
            }
        }


    }

    private fun addListenerClickCallbacks() {
        val msgBody = InAppMessageUtils.getMessageBody(inAppMessage.message)

        inAppViewLayout?.viewContainer?.setOnClickListener {
            inAppViewLifecycleListener.onClicked(
                this,
                inAppMessage,
                InAppViewUtils.getInAppRootActionParams(msgBody)
            )
        }

        inAppViewLayout?.primaryButton?.setOnClickListener {
            inAppViewLifecycleListener.onButtonClicked(
                this,
                inAppMessage,
                InAppViewUtils.getPrimaryButtonViewParams(msgBody)?.toActionParams()
            )
        }
        inAppViewLayout?.secondaryButton?.setOnClickListener {
            inAppViewLifecycleListener.onButtonClicked(
                this,
                inAppMessage,
                InAppViewUtils.getSecondaryButtonViewParams(msgBody)?.toActionParams()
            )
        }
        inAppViewLayout?.closeButton?.setOnClickListener {
            inAppViewLifecycleListener.onCloseButtonClicked(this, inAppMessage)
        }
    }

    private fun loadJSInterface() {
        val message = inAppMessage.message
        val htmlString =
            (message["modal"]?.jsonObject ?: message["fs"]?.jsonObject)?.get("html")?.toString()
                ?: run {
                    InAppWebViewLayout.logger.debug("Modal/ FS object not present in in-app message!")
                    return
                }
        val decodedHtmlString: String = try {
            val decodedBytes = Base64.decode(htmlString, Base64.DEFAULT)
            String(decodedBytes, Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            htmlString // Use original encoded string if decoding fails
        }
        val jsinterface = JavaScriptInterface(this, inAppViewLayout?.webView!!)
        inAppViewLayout?.webView?.loadDataWithBaseURL(
            null, decodedHtmlString, "text/html",
            "utf-8", null
        )
        jsinterface.setupWebViewClient()

        jsinterface.setEventListener { eventData ->
            // Handle the event data here in ClassB
            println("Event occurred with data in ClassB: $eventData")
            inAppViewLifecycleListener.onButtonClicked(
                this,
                inAppMessage, eventData
            )
        }

        inAppViewLayout?.closeButton?.setOnClickListener {
            inAppViewLifecycleListener.onCloseButtonClicked(this, inAppMessage)
        }
    }

    override fun show() {
        if (inAppViewLayout == null) {
            return
        }
        when (InAppMessageUtils.getMessageType(inAppMessage.message)) {
            InAppMessageType.MODAL -> dialog.apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setGravity(Gravity.CENTER)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(inAppViewLayout!!)
                show()
            }

            InAppMessageType.FULL_SCREEN -> dialog.apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setGravity(Gravity.CENTER)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(inAppViewLayout!!)

                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                show()
            }

            InAppMessageType.BANNER -> dialog.apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setGravity(Gravity.BOTTOM)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(inAppViewLayout!!)
                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                show()
            }
        }
        // TODO: Handle auto dismiss
         inAppViewLifecycleListener.onDisplayed(inAppMessage)
    }

    override fun close() {
        dialog.dismiss()
        inAppViewLifecycleListener.onClosed(inAppMessage)
    }

}

