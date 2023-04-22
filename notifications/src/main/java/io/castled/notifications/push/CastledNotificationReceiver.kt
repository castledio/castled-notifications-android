package io.castled.notifications.push

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.castled.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.NotificationActionContext
import io.castled.notifications.push.models.PushConstants
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CastledNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        handleIntent(context, intent)
    }

    private fun handleIntent(context: Context, intent: Intent) {
        try {
            val contextJson = intent.extras?.getString(PushConstants.CASTLED_EXTRA_NOTIF_CONTEXT) ?: return
            val notificationContext : NotificationActionContext = Json.decodeFromString(contextJson)

            // Cancel the notification
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationContext.notificationId)

            logger.debug("Reporting push notification event: ${notificationContext.eventType}")
            PushNotification.reportPushEvent(notificationContext)

        } catch (e: Exception) {
            logger.error("Push notification receiver failed", e)
        }
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH_RECEIVER)
    }
}