package io.castled.android.notifications.inapp

import android.content.Context
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.commons.CastledClickActionUtils
import io.castled.android.notifications.commons.ClickActionParams
import io.castled.android.notifications.inapp.views.InAppViewUtils
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.Campaign

internal class InAppLifeCycleListenerImpl(private val inAppController: InAppController) :
    InAppViewLifecycleListener {

    override fun onDisplayed(inAppMessage: Campaign) {
        InAppNotification.reportInAppEvent(
            InAppEventUtils.getViewedEvent(inAppMessage),
            null
        )
        InAppNotification.updateInAppDisplayStats(inAppMessage)
        logger.debug("In-App with notification id:${inAppMessage.notificationId} displayed!")
    }

    override fun onClicked(
        context: Context,
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign,
        actionParams: ClickActionParams
    ) {
        try {
            when (actionParams.action) {
                CastledClickAction.NONE -> {
                    // Do nothing
                }

                CastledClickAction.CUSTOM -> {
                    // Do nothing
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getClickedEvent(
                            inAppMessage,
                            actionParams
                        ), actionParams
                    )
                    inAppViewBaseDecorator.close(actionParams.action)
                }

                CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                    if (!CastledSharedStore.configs.skipUrlHandling) {
                        CastledClickActionUtils.handleDeeplinkAction(
                            context,
                            actionParams.uri!!,
                            actionParams.keyVals
                        )
                    }
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getClickedEvent(
                            inAppMessage,
                            actionParams
                        ), actionParams
                    )
                    inAppViewBaseDecorator.close(actionParams.action)
                }

                CastledClickAction.DISMISS_NOTIFICATION -> {
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getDismissedEvent(
                            inAppMessage
                        ), actionParams
                    )
                    inAppViewBaseDecorator.close(actionParams.action)
                    logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
                }

                CastledClickAction.NAVIGATE_TO_SCREEN -> {
                    if (!CastledSharedStore.configs.skipUrlHandling) {
                        CastledClickActionUtils.handleNavigationAction(
                            context,
                            actionParams.uri!!,
                            actionParams.keyVals
                        )
                    }
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getClickedEvent(
                            inAppMessage,
                            actionParams
                        ), actionParams
                    )
                    inAppViewBaseDecorator.close(actionParams.action)

                }

                else -> {
                    logger.debug("Unexpected action:${actionParams.action} for notification:${inAppMessage.notificationId}, button:${actionParams.actionLabel}")
                }
            }
        } catch (e: Exception) {
            inAppViewBaseDecorator.close(CastledClickAction.NONE)
            logger.debug("Click action: ${actionParams.action} handling failed. reason: ${e.message}")
        }
        logger.debug("In-App with notification id:${inAppMessage.notificationId} clicked!")
    }

    override fun onButtonClicked(
        context: Context,
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign,
        actionParams: ClickActionParams?
    ) {
        actionParams ?: return
        try {
            when (actionParams.action) {
                CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                    if (!CastledSharedStore.configs.skipUrlHandling) {
                        CastledClickActionUtils.handleDeeplinkAction(
                            context,
                            actionParams.uri!!,
                            actionParams.keyVals
                        )
                    }
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getClickedEvent(
                            inAppMessage,
                            actionParams
                        ), actionParams
                    )
                }

                CastledClickAction.DISMISS_NOTIFICATION -> {
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getDismissedEvent(
                            inAppMessage
                        ), actionParams
                    )
                    logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
                }

                CastledClickAction.NAVIGATE_TO_SCREEN -> {
                    if (!CastledSharedStore.configs.skipUrlHandling) {
                        CastledClickActionUtils.handleNavigationAction(
                            context,
                            actionParams.uri!!,
                            actionParams.keyVals
                        )
                    }
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getClickedEvent(
                            inAppMessage,
                            actionParams
                        ), actionParams
                    )
                }

                CastledClickAction.REQUEST_PUSH_PERMISSION -> {
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getClickedEvent(
                            inAppMessage,
                            actionParams
                        ), actionParams
                    )
                    InAppNotification.getCurrentActivity()?.let {
                        CastledNotifications.requestPushPermission(it)
                    }
                }

                CastledClickAction.CUSTOM -> {
                    InAppNotification.reportInAppEvent(
                        InAppEventUtils.getClickedEvent(
                            inAppMessage,
                            actionParams
                        ), actionParams
                    )

                    // TODO:  Not implemented!
                }

                else -> {
                    logger.debug("Unexpected action:${actionParams.action} for notification:${inAppMessage.notificationId}, button:${actionParams.actionLabel}")
                }
            }
            inAppViewBaseDecorator.close(actionParams.action)

        } catch (e: Exception) {
            inAppViewBaseDecorator.close(CastledClickAction.DISMISS_NOTIFICATION)
            logger.debug("Button click action: ${actionParams.action} handling failed. reason: ${e.message}")
        }
        // Close in-app irrespective of the click action
    }


    override fun onCloseButtonClicked(
        context: Context,
        inAppViewBaseDecorator: InAppViewBaseDecorator,
        inAppMessage: Campaign
    ) {
        inAppViewBaseDecorator.close(CastledClickAction.DISMISS_NOTIFICATION)
        InAppNotification.reportInAppEvent(
            InAppEventUtils.getDismissedEvent(inAppMessage),
            InAppViewUtils.getInAppDismissedActionParams()
        )
        logger.debug("In-App with notification id:${inAppMessage.notificationId} dismissed!")
    }

    override fun onClosed(inAppMessage: Campaign) {
        inAppController.clearCurrentInApp()
        logger.debug("In-App with notification id:${inAppMessage.notificationId} closed!")
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.IN_APP_VIEW_LIFECYCLE)
    }
}