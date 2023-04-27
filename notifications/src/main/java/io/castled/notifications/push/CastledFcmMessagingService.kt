package io.castled.notifications.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.castled.notifications.CastledNotifications
import io.castled.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.CastledPushMessage
import io.castled.notifications.push.models.PushTokenType

class CastledFcmMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CastledNotifications.onTokenFetch(token, PushTokenType.FCM)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.debug("From: " + remoteMessage.from)

        if (CastledNotifications.isCastledPushMessage(remoteMessage)) {
            // Return if failed to extract payload
            val pushMessage = CastledPushMessage.extractCastledPushMessage(remoteMessage.data)
                ?: return
            // Handle message payload
            CastledNotifications.handlePushNotification(this, pushMessage)
        } else {
            logger.debug("Push message not from Castled")
        }
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}