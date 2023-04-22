package io.castled.notifications.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.castled.notifications.CastledNotifications.onTokenFetch
import io.castled.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.notifications.logger.LogTags

class CastledMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        onTokenFetch(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.debug("From: " + remoteMessage.from)

        // Handle message payload
        PushNotificationManager.handleNotification(this, remoteMessage)
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}