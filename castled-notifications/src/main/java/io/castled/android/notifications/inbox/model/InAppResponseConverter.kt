package io.castled.android.notifications.inbox.model
import io.castled.android.notifications.store.models.AppInbox

internal object InAppResponseConverter {

    fun InboxResponse.toInbox() : AppInbox {
        return AppInbox(
            messageId = this.messageId,
            teamId = this.teamId,
            sourceContext = this.sourceContext,
            startTs = this.startTs,
            expiryTs = this.expiryTs,
            is_read = this.read,
            trigger = this.trigger,
            message = this.message,
            message_type = this.messageType,
            aspectRatio =  this.aspectRatio,
            thumbnailUrl = this.thumbnailUrl
        )
    }
}