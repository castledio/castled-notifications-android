package io.castled.android.notifications.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.R
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledNotificationFieldConsts
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationActionContext
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.push.models.PushConstants
import io.castled.android.notifications.push.views.PushBaseBuilder
import io.castled.android.notifications.push.views.PushBuilderFactory

internal object PushNotificationManager {
    private val logger = getInstance(LogTags.PUSH)
    private var displayedNotifications: HashMap<Int, PushBaseBuilder> = HashMap()

    fun isCastledNotification(remoteMessage: RemoteMessage): Boolean {
        if (remoteMessage.data.containsKey(CastledNotificationFieldConsts.CASTLED_KEY)) {
            return true
        }
        logger.debug("Push message not from Castled!")
        return false
    }

    fun displayNotification(context: Context, pushMessage: CastledPushMessage) {
        logger.debug("Building castled notification...")
        try {
            val pushBuilder =
                PushBuilderFactory.createPushBuilder(context, pushMessage)
            pushBuilder.let {
                displayedNotifications[pushMessage.notificationId] = it
                it.build()
            }
            PushNotification.reportPushEvent(
                NotificationActionContext(
                    notificationId = pushMessage.notificationId,
                    sourceContext = pushMessage.sourceContext,
                    eventType = NotificationEventType.RECEIVED.toString(),
                    actionLabel = null,
                    actionType = null,
                    actionUri = null,
                    keyVals = null
                )
            )
        } catch (_: Exception) {

        }

    }

    fun getOrCreateNotificationChannel(
        context: Context,
        channelId: String,
        channelDesc: String?
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance =
                context.resources.getInteger(R.integer.io_castled_push_default_channel_importance)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val defaultChannelId = PushConstants.CASTLED_DEFAULT_CHANNEL_ID
                val channel = NotificationChannel(
                    defaultChannelId,
                    context.getString(R.string.io_castled_push_default_channel_name), importance
                )
                channel.description = channelDesc
                notificationManager.createNotificationChannel(channel)
                return defaultChannelId
            }
        }
        return channelId
    }

    internal fun cancelTimerIfAny(notificationId: Int) {
        try {
            val pushBuilder = displayedNotifications[notificationId]
            pushBuilder?.let {
                displayedNotifications.remove(notificationId)
                it.close()
            }
        } catch (_: Exception) {

        }
    }

}