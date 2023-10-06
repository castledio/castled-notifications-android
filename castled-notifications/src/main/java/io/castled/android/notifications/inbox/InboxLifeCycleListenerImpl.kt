package io.castled.android.notifications.inbox

import android.content.Context
import io.castled.android.notifications.commons.CastledClickActionUtils
import io.castled.android.notifications.commons.toMapString
import io.castled.android.notifications.inbox.model.InboxResponseConverter.toInboxItem
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.store.models.Inbox
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class InboxLifeCycleListenerImpl(private val context: Context) {

    private fun getClickAction(action: String): CastledClickAction {
        return try {
            action.let { CastledClickAction.valueOf(it) }
        } catch (e: Exception) {
            CastledClickAction.NONE
        }
    }

    fun registerReadEvents(
        inboxItems: Set<Inbox>
    ) {
        AppInbox.reportReadEventsWithObjects(inboxItems)
    }

    fun onClicked(
        inboxItem: Inbox, actionParams: Map<String, Any>
    ) {
        val clickAction = getClickAction(
            (actionParams["clickAction"] as? JsonPrimitive?)?.content
                ?: (actionParams["clickAction"] as? String) ?: "NONE"
        )
        val uri =
            (actionParams["url"] as? JsonPrimitive?)?.content ?: (actionParams["url"] as? String)
            ?: ""
        val keyVals = ((actionParams["keyVals"] as? JsonObject)?.toMapString())
        AppInbox.reportEventWith(
            inboxItem.toInboxItem(), (actionParams["label"] as? String) ?: "", "CLICKED"
        )
        when (clickAction) {
            CastledClickAction.NONE -> {
                // Do nothing
            }

            CastledClickAction.DEEP_LINKING, CastledClickAction.RICH_LANDING -> {
                CastledClickActionUtils.handleDeeplinkAction(
                    context, uri, keyVals
                )
            }

            CastledClickAction.DISMISS_NOTIFICATION -> {
                logger.debug("Inbox with notification id:${inboxItem.messageId} dismissed!")
            }

            CastledClickAction.NAVIGATE_TO_SCREEN -> {
                CastledClickActionUtils.handleNavigationAction(
                    context, uri, keyVals
                )
            }

            else -> {
                logger.debug(
                    "Unexpected action:${clickAction} for notification:" + "${inboxItem.messageId}, button:${actionParams["label"]}"
                )
            }
        }
        logger.debug("Inbox with notification id:${inboxItem.messageId} clicked!")
    }

    companion object {
        val logger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)
    }
}