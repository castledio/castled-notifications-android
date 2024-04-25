package io.castled.android.notifications.inapp

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.CastledUtils
import io.castled.android.notifications.inapp.js.JavaScriptInterface
import io.castled.android.notifications.inapp.models.InAppMessageType
import io.castled.android.notifications.inapp.views.InAppBaseViewLayout
import io.castled.android.notifications.inapp.views.InAppViewFactory
import io.castled.android.notifications.inapp.views.InAppViewUtils
import io.castled.android.notifications.inapp.views.InAppWebViewLayout
import io.castled.android.notifications.inapp.views.toActionParams
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.store.models.Campaign
import kotlinx.serialization.json.jsonObject

// TODO: Convert Campaign to InAppMessage

internal class InAppViewDecorator(
    val context: Context,
    private val inAppMessage: Campaign,
    private val inAppViewLifecycleListener: InAppViewLifecycleListener
) : InAppViewBaseDecorator {

    private var dialog = Dialog(context)
    private val inAppViewLayout: InAppBaseViewLayout? =
        InAppViewFactory.createView(context, inAppMessage)
    private val autoDismissalHandler = Handler(Looper.getMainLooper())

    init {
        if (inAppViewLayout != null) {
            inAppViewLayout.updateViewParams(inAppMessage)
            if (inAppViewLayout.webView == null) {
                addListenerClickCallbacks()
            } else {
                inAppViewLayout.webView?.let { loadJSInterface() }
            }
        }

    }

    private fun addListenerClickCallbacks() {
        val msgBody = InAppMessageUtils.getMessageBody(inAppMessage.message)
        inAppViewLayout?.viewContainer?.setOnClickListener {
            inAppViewLifecycleListener.onClicked(
                it.context,
                this,
                inAppMessage,
                InAppViewUtils.getInAppRootActionParams(msgBody)
            )
        }

        inAppViewLayout?.primaryButton?.setOnClickListener {
            inAppViewLifecycleListener.onButtonClicked(
                it.context,
                this,
                inAppMessage,
                InAppViewUtils.getPrimaryButtonViewParams(msgBody)?.toActionParams()
            )
        }
        inAppViewLayout?.secondaryButton?.setOnClickListener {
            inAppViewLifecycleListener.onButtonClicked(
                it.context,
                this,
                inAppMessage,
                InAppViewUtils.getSecondaryButtonViewParams(msgBody)?.toActionParams()
            )
        }
        inAppViewLayout?.closeButton?.setOnClickListener {
            inAppViewLifecycleListener.onCloseButtonClicked(it.context, this, inAppMessage)
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
        val jsInterface = JavaScriptInterface(this, inAppViewLayout?.webView!!)
        inAppViewLayout.webView?.loadDataWithBaseURL(
            null, decodedHtmlString, "text/html",
            "utf-8", null
        )
        jsInterface.setupWebViewClient()

        jsInterface.setEventListener { eventData ->
            // Handle the event data here in ClassB
            println("Event occurred with data in ClassB: $eventData")
            inAppViewLifecycleListener.onButtonClicked(
                context, this,
                inAppMessage, eventData
            )
        }

        inAppViewLayout.closeButton?.setOnClickListener {
            inAppViewLifecycleListener.onCloseButtonClicked(it.context, this, inAppMessage)
        }
    }

    override fun show(withApiCall: Boolean) {
        if (inAppViewLayout == null) {
            return
        }
        when (InAppMessageUtils.getMessageType(inAppMessage.message)) {
            InAppMessageType.MODAL -> dialog.apply {

                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(inAppViewLayout)
                window?.setGravity(Gravity.CENTER)
                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                setupModalContainer()
                show()
            }

            InAppMessageType.FULL_SCREEN -> dialog.apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setGravity(Gravity.CENTER)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setContentView(inAppViewLayout)

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
                setContentView(inAppViewLayout)
                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                window?.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                )
                show()
            }
        }
        handleAutoDismissal(withApiCall)
    }

    override fun close(action: CastledClickAction) {
        dismissDialog()
        inAppViewLifecycleListener.onClosed(inAppMessage)
        if (action != CastledClickAction.NAVIGATE_TO_SCREEN){
          InAppNotification.checkPendingNotificationsIfAny()
        }
    }

    internal fun dismissDialog() {
        try {
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            removeAutoDismissalHandler()

        } catch (_: Exception) {
        }

    }

    private fun parseColor(colorStr: String, defaultColor: Int): Int {
        return try {
            Color.parseColor(colorStr)
        } catch (e: IllegalArgumentException) {
            defaultColor
        }
    }

    private fun setupModalContainer() {

        val modalParams = inAppMessage.message["modal"]?.jsonObject
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
        modalParams?.let {
            val headerViewParams = InAppViewUtils.getHeaderViewParams(modalParams)
            headerViewParams.let {
                try {
                    dialog.window?.setBackgroundDrawable(
                        ColorDrawable(
                            parseColor(
                                headerViewParams.screenOverlayColor,
                                Color.TRANSPARENT
                            )
                        )
                    )
                } catch (e: Exception) {

                }

            }
            val dialogueSize =
                context.resources.getString(R.string.castled_inapp_dialouge_size).toFloat()
            val screenSize = CastledUtils.getScreenSize(context)
            val layoutParams = inAppViewLayout!!.layoutParams
            layoutParams.width = (screenSize.x * dialogueSize).toInt()
            layoutParams.height = (screenSize.y * dialogueSize).toInt()
            inAppViewLayout.layoutParams = layoutParams
            inAppViewLayout.x = (screenSize.x / 2 - layoutParams.width / 2).toFloat()
            inAppViewLayout.y =
                (screenSize.y / 2 - layoutParams.height / 2 - CastledUtils.getStatusBarHeight(
                    context
                ) / 2).toFloat()
        }
    }

    private fun handleAutoDismissal(withApiCall: Boolean) {
        removeAutoDismissalHandler()
        var inappAutoDismissalTime = 0L
        if (withApiCall) { // this is for oriention handling,this will be false for orientation change
            inAppViewLifecycleListener.onDisplayed(inAppMessage)

            if (inAppMessage.displayConfig.autoDismissInterval > 0) {
                inappAutoDismissalTime =
                    inAppMessage.displayConfig.autoDismissInterval * 1000
            }
        } else if (inAppMessage.displayConfig.autoDismissInterval > 0) {
            inappAutoDismissalTime =
                ((inAppMessage.lastDisplayedTime + inAppMessage.displayConfig.autoDismissInterval * 1000) -
                        (System.currentTimeMillis()))
        }

        if (inappAutoDismissalTime > 0) {
            val task = Runnable {
                close(CastledClickAction.NONE)
            }
            autoDismissalHandler.postDelayed(task, inappAutoDismissalTime)
        }
    }

    private fun removeAutoDismissalHandler() {
        autoDismissalHandler.removeCallbacksAndMessages(null)
    }
}

