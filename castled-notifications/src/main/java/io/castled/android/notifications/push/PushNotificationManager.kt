package io.castled.android.notifications.push

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.push.models.CastledNotificationFieldConsts

internal object PushNotificationManager {
    private val logger = getInstance(LogTags.PUSH)

    fun isCastledNotification(remoteMessage: RemoteMessage): Boolean {
        if (remoteMessage.data.containsKey(CastledNotificationFieldConsts.CASTLED_KEY)) {
            return true
        }
        logger.debug("Push message not from Castled!")
        return false
    }

    @SuppressLint("MissingPermission")
    suspend fun handleNotification(context: Context, pushPayload: CastledPushMessage?) {
        // Payload from Castled server
        pushPayload ?: run {
            logger.debug("Castled push notification empty! skipping notification handling...")
            return
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logger.debug("Do not have push permission!")
            return
        }
        logger.debug("Building castled notification...")

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