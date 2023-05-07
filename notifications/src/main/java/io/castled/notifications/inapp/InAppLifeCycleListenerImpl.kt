package io.castled.notifications.inapp

import android.content.Context
import io.castled.notifications.commons.CastledClickActionUtils
import io.castled.notifications.inapp.views.ButtonViewParams
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.CastledClickAction
import io.castled.notifications.store.models.Campaign

class InAppLifeCycleListenerImpl(private val context: Context) : InAppViewLifecycleListener {

    override fun onDisplayed(inAppMessage: Campaign) {
        InAppNotification.reportInAppEvent(InAppSystemEventUtils.getViewedEvent(inAppMessage))
        logger.debug("In-App with notification id:${inAppMessage.notificationId} displayed!")
    }

    override fun onClicked(inAppMessage: Campaign) {
        InAppNotification.reportInAppEvent(InAppSystemEventUtils.getClickedEvent(inAppMessage))
        logger.debug("In-App with notification id:${inAppMessage.notificationId} clicked!")
    }

    override fun onButtonClicked(
        inAppViewDecorator: InAppViewDecorator,
        inAppMessage: Campaign,
        btnParams: ButtonViewParams?
    ) {
        val params = btnParams ?: return
        when (params.action) {
            CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                CastledClickActionUtils.handleDeeplinkAction(context, params.uri!!, params.keyVals)
            }
            CastledClickAction.DISMISS_NOTIFICATION -> {
                // Noting to do
            }
            CastledClickAction.NAVIGATE_TO_SCREEN -> {
                CastledClickActionUtils.handleNavigationAction(
                    context,
                    params.uri!!,
                    params.keyVals
                )
            }
            CastledClickAction.PUSH_PERMISSION_REQUEST -> {
                TODO("Not implemented!")
            }
            else -> {
                logger.debug("Unexpected action:${params.action} for notification:${inAppMessage.notificationId}, button:${params.buttonText}")
            }
        }
        InAppNotification.reportInAppEvent(InAppSystemEventUtils.getButtonClickedEvent(inAppMessage, btnParams))
    }

    override fun onCloseButtonClicked(
        inAppViewDecorator: InAppViewDecorator,
        inAppMessage: Campaign
    ) {
        inAppViewDecorator.close()
    }

    override fun onDismissed(inAppMessage: Campaign) {
        InAppNotification.reportInAppEvent(InAppSystemEventUtils.getDismissedEvent(inAppMessage))
        logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.IN_APP_VIEW_LIFECYCLE)
    }
}