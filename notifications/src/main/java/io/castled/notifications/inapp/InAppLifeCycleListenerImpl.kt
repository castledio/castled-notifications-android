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
        InAppNotification.reportInAppEvent(InAppEventUtils.getViewedEvent(inAppMessage))
        logger.debug("In-App with notification id:${inAppMessage.notificationId} displayed!")
    }

    override fun onClicked(inAppMessage: Campaign) {
        InAppNotification.reportInAppEvent(InAppEventUtils.getClickedEvent(inAppMessage))
        logger.debug("In-App with notification id:${inAppMessage.notificationId} clicked!")
    }

    override fun onButtonClicked(
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign,
        btnParams: ButtonViewParams?
    ) {
        val params = btnParams ?: return
        when (params.action) {
            CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                CastledClickActionUtils.handleDeeplinkAction(context, params.uri!!, params.keyVals)
                InAppNotification.reportInAppEvent(InAppEventUtils.getButtonClickedEvent(inAppMessage, btnParams))
            }
            CastledClickAction.DISMISS_NOTIFICATION -> {
                InAppNotification.reportInAppEvent(InAppEventUtils.getDismissedEvent(inAppMessage))
                logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
            }
            CastledClickAction.NAVIGATE_TO_SCREEN -> {
                CastledClickActionUtils.handleNavigationAction(
                    context,
                    params.uri!!,
                    params.keyVals
                )
                InAppNotification.reportInAppEvent(InAppEventUtils.getButtonClickedEvent(inAppMessage, btnParams))
            }
            CastledClickAction.PUSH_PERMISSION_REQUEST -> {
                TODO("Not implemented!")
            }
            else -> {
                logger.debug("Unexpected action:${params.action} for notification:${inAppMessage.notificationId}, button:${params.buttonText}")
            }
        }
        // Close in-app irrespective of the click action
        inAppViewBaseDecorator.close()
    }

    override fun onCloseButtonClicked(
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign
    ) {
        inAppViewBaseDecorator.close()
        InAppNotification.reportInAppEvent(InAppEventUtils.getDismissedEvent(inAppMessage))
        logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
    }

    override fun onClosed(inAppMessage: Campaign) {
        logger.debug("In-App with notification id:${inAppMessage.notificationId} closed!")
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.IN_APP_VIEW_LIFECYCLE)
    }
}