package io.castled.android.notifications.inapp

import android.content.Context
import io.castled.android.notifications.commons.CastledClickActionUtils
import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.store.models.Campaign

class InAppLifeCycleListenerImpl(private val context: Context) : InAppViewLifecycleListener {

    override fun onDisplayed(inAppMessage: Campaign) {
        InAppNotification.reportInAppEvent(InAppEventUtils.getViewedEvent(inAppMessage))
        logger.debug("In-App with notification id:${inAppMessage.notificationId} displayed!")
    }

    override fun onClicked(
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign,
        actionParams: ClickActionParams
    ) {
        when (actionParams.action) {
            CastledClickAction.NONE -> {
                // Do nothing
            }
            CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                CastledClickActionUtils.handleDeeplinkAction(
                    context,
                    actionParams.uri!!,
                    actionParams.keyVals
                )
                InAppNotification.reportInAppEvent(
                    InAppEventUtils.getClickedEvent(
                        inAppMessage,
                        actionParams
                    )
                )
                inAppViewBaseDecorator.close()
            }
            CastledClickAction.DISMISS_NOTIFICATION -> {
                InAppNotification.reportInAppEvent(InAppEventUtils.getDismissedEvent(inAppMessage))
                inAppViewBaseDecorator.close()
                logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
            }
            CastledClickAction.NAVIGATE_TO_SCREEN -> {
                CastledClickActionUtils.handleNavigationAction(
                    context,
                    actionParams.uri!!,
                    actionParams.keyVals
                )
                InAppNotification.reportInAppEvent(
                    InAppEventUtils.getClickedEvent(
                        inAppMessage,
                        actionParams
                    )
                )
                inAppViewBaseDecorator.close()
            }
            else -> {
                logger.debug("Unexpected action:${actionParams.action} for notification:${inAppMessage.notificationId}, button:${actionParams.actionLabel}")
            }
        }
        logger.debug("In-App with notification id:${inAppMessage.notificationId} clicked!")
    }

    override fun onButtonClicked(
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign,
        actionParams: ClickActionParams?
    ) {
        actionParams ?: return
        when (actionParams.action) {
            CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                CastledClickActionUtils.handleDeeplinkAction(
                    context,
                    actionParams.uri!!,
                    actionParams.keyVals
                )
                InAppNotification.reportInAppEvent(
                    InAppEventUtils.getClickedEvent(
                        inAppMessage,
                        actionParams
                    )
                )
            }
            CastledClickAction.DISMISS_NOTIFICATION -> {
                InAppNotification.reportInAppEvent(InAppEventUtils.getDismissedEvent(inAppMessage))
                logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
            }
            CastledClickAction.NAVIGATE_TO_SCREEN -> {
                CastledClickActionUtils.handleNavigationAction(
                    context,
                    actionParams.uri!!,
                    actionParams.keyVals
                )
                InAppNotification.reportInAppEvent(
                    InAppEventUtils.getClickedEvent(
                        inAppMessage,
                        actionParams
                    )
                )
            }

            CastledClickAction.REQUEST_PUSH_PERMISSION -> {
                InAppNotification.reportInAppEvent(
                    InAppEventUtils.getClickedEvent(
                        inAppMessage,
                        actionParams
                    )
                )

                // TODO:  Not implemented!
            }

            CastledClickAction.CUSTOM -> {
                InAppNotification.reportInAppEvent(
                    InAppEventUtils.getClickedEvent(
                        inAppMessage,
                        actionParams
                    )
                )

                // TODO:  Not implemented!
            }

            else -> {
                logger.debug("Unexpected action:${actionParams.action} for notification:${inAppMessage.notificationId}, button:${actionParams.actionLabel}")
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