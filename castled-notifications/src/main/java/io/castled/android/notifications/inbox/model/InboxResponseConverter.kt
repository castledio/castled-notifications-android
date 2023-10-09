package io.castled.android.notifications.inbox.model

import io.castled.android.notifications.store.models.Inbox
import kotlinx.serialization.json.JsonObject

internal object InboxResponseConverter {

    fun CastledInboxItem.toInbox(): Inbox {
        return Inbox(
            messageId = this.messageId,
            teamId = this.teamId,
            sourceContext = this.sourceContext,
            startTs = this.startTs,
            expiryTs = this.expiryTs,
            isRead = this.read,
            trigger = this.trigger ?: JsonObject(emptyMap()),
            message = this.message,
            messageType = this.messageType,
            aspectRatio = this.aspectRatio,
            thumbnailUrl = this.thumbnailUrl,
            body = this.body,
            title = this.title,
            dateAdded = this.dateAdded,
            updatedTime = this.updatedTs,
            isPinned = this.pinningEnabled,
            tag = this.tag ?: ""
        )
    }

    fun Inbox.toInboxItem(): CastledInboxItem {

        return CastledInboxItem(
            messageId = this.messageId,
            teamId = this.teamId,
            sourceContext = this.sourceContext,
            startTs = this.startTs,
            expiryTs = this.expiryTs,
            read = this.isRead,
            trigger = this.trigger,
            message = this.message,
            pinningEnabled = this.isPinned,
            tag = this.tag,
            updatedTs = this.updatedTime
        )
    }
}