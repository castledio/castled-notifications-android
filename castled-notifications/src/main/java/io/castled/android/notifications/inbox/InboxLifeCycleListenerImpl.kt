package io.castled.android.notifications.inbox

import android.content.Context
import io.castled.android.notifications.commons.CastledClickActionUtils
import io.castled.android.notifications.inbox.model.InboxActionUtils
import io.castled.android.notifications.inbox.model.InboxEventType
import io.castled.android.notifications.inbox.model.InboxResponseConverter.toInboxItem
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.Inbox

internal class InboxLifeCycleListenerImpl(private val context: Context) {

    fun deleteItem(
        inboxItem: Inbox
    ) {
        AppInbox.reportEventWith(
            inboxItem.toInboxItem(), "", InboxEventType.DELETED.toString(), null
        )
    }

    fun registerReadEvents(
        inboxItems: Set<Long>
    ) {
        AppInbox.reportInboxIdsRead(inboxItems)
    }

    fun onClicked(
        inboxItem: Inbox, actionParams: Map<String, Any>
    ) {
        val actionContext = InboxActionUtils.getCastledActionContextFromActionParams(
            actionParams
        )
        val uri = actionContext.actionUri ?: ""
        val keyVals = actionContext.keyVals

        when (actionContext.actionType) {
            CastledClickAction.NONE -> {
                // Do nothing
            }

            CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                if (!CastledSharedStore.configs.skipUrlHandling) {
                    CastledClickActionUtils.handleDeeplinkAction(
                        context, uri, keyVals
                    )
                }
                AppInbox.reportEventWith(
                    inboxItem.toInboxItem(),
                    actionContext.actionLabel ?: "",
                    InboxEventType.CLICKED.toString(),
                    actionContext
                )
            }

            CastledClickAction.DISMISS_NOTIFICATION -> {
                // Do nothing
            }

            CastledClickAction.NAVIGATE_TO_SCREEN -> {
                if (!CastledSharedStore.configs.skipUrlHandling) {
                    CastledClickActionUtils.handleNavigationAction(
                        context, uri, keyVals
                    )
                }
                AppInbox.reportEventWith(
                    inboxItem.toInboxItem(),
                    actionContext.actionLabel ?: "",
                    InboxEventType.CLICKED.toString(),
                    actionContext
                )
            }

            else -> {
                logger.debug(
                    "Unexpected action:${actionContext.actionType} for notification:" + "${inboxItem.messageId}, button:${actionParams["label"]}"
                )
            }
        }
        logger.debug("Inbox with notification id:${inboxItem.messageId} clicked!")
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)
    }
}