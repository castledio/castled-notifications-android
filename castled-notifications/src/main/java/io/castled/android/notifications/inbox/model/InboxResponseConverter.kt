package io.castled.android.notifications.inbox.model
import io.castled.android.notifications.store.models.AppInbox

internal object InboxResponseConverter {

    fun InboxResponse.toInbox() : AppInbox {
        return AppInbox(
            messageId = this.messageId,
            teamId = this.teamId,
            sourceContext = this.sourceContext,
            startTs = this.startTs,
            expiryTs = this.expiryTs,
            isRead = this.read,
            trigger = this.trigger,
            message = this.message,
            messageType = this.messageType,
            aspectRatio =  this.aspectRatio,
            thumbnailUrl = this.thumbnailUrl,
            body = this.body,
            title = this.title,
            dateAdded = this.date_added
        )
    }
}