package io.castled.android.notifications.push

import android.annotation.SuppressLint
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
import kotlinx.coroutines.CoroutineScope

internal object PushNotificationManager {
    private val logger = getInstance(LogTags.PUSH)
    var displayedNotificaions: HashMap<Int, PushBaseBuilder> = HashMap()

    fun isCastledNotification(remoteMessage: RemoteMessage): Boolean {
        if (remoteMessage.data.containsKey(CastledNotificationFieldConsts.CASTLED_KEY)) {
            return true
        }
        logger.debug("Push message not from Castled!")
        return false
    }

    @SuppressLint("MissingPermission")
    suspend fun displayNotification(
        context: Context,
        pushMessage: CastledPushMessage,
        externalScope: CoroutineScope
    ) {
        logger.debug("Building castled notification...")

        val pushBuilder = PushBuilderFactory.createPushBuilder(context, pushMessage, externalScope)
        pushBuilder?.let {
            displayedNotificaions[pushMessage.notificationId] = it
            it.build()
            it.display()
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

    internal fun cancelTimerIfAny(notiifcationId: Int) {
        try {
            val pushBuilder = displayedNotificaions[notiifcationId]
            pushBuilder?.let {
                it.close()
                displayedNotificaions.remove(notiifcationId)
            }
        } catch (e: Exception) {

        }
    }
}