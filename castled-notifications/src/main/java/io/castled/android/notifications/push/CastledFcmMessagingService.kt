package io.castled.android.notifications.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.PushTokenType

class CastledFcmMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CastledNotifications.onTokenFetch(token, PushTokenType.FCM)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.debug("From: " + remoteMessage.from)

        if (CastledNotifications.isCastledPushMessage(remoteMessage)) {
            // Notification initiated from Castled server. Handle message payload
            CastledNotifications.handlePushNotification(this, remoteMessage.toCastledPushMessage())
        } else {
            logger.debug("Push message not from Castled")
        }
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}