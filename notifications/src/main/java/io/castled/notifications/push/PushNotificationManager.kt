package io.castled.notifications.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import io.castled.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.CastledPushMessage
import io.castled.notifications.push.models.NotificationActionContext
import io.castled.notifications.push.models.NotificationEventType
import io.castled.notifications.push.models.CastledNotificationFieldConsts

internal object PushNotificationManager {
    private val logger = getInstance(LogTags.PUSH)

    fun isCastledNotification(remoteMessage: RemoteMessage): Boolean {
        if (remoteMessage.data.containsKey(CastledNotificationFieldConsts.CASTLED_KEY)) {
            return true
        }
        logger.debug("Push message not from Castled!")
        return false
    }

    suspend fun handleNotification(context: Context, pushPayload: CastledPushMessage) {
        // Payload from Castled server
        logger.debug("Building castled notification...")

        if (PushNotification.isAppInForeground) {
            // Not displaying notification if foreground.
            PushNotification.reportPushEvent(
                NotificationActionContext(
                    notificationId = pushPayload.notificationId,
                    teamId = pushPayload.teamId,
                    sourceContext = pushPayload.sourceContext,
                    eventType = NotificationEventType.FOREGROUND.toString(),
                    actionLabel = null,
                    actionType = null,
                    actionUri = null,
                    keyVals = null
                )
            )
        } else {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                logger.debug("Do not have push permission!")
                return
            }
            val notification =
                CastledNotificationBuilder(context).buildNotification(pushPayload)
            NotificationManagerCompat.from(context)
                .notify(pushPayload.notificationId, notification)

            PushNotification.reportPushEvent(
                NotificationActionContext(
                    notificationId = pushPayload.notificationId,
                    teamId = pushPayload.teamId,
                    sourceContext = pushPayload.sourceContext,
                    eventType = NotificationEventType.RECEIVED.toString(),
                    actionLabel = null,
                    actionType = null,
                    actionUri = null,
                    keyVals = null
                )
            )
        }
    }

    fun getOrCreateNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String?,
        channelDesc: String?
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, channelName, importance)
                channel.description = channelDesc
                notificationManager.createNotificationChannel(channel)
            }
        }
        return channelId
    }
}