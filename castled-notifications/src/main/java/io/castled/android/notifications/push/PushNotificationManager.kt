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
import io.castled.android.notifications.R
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledNotificationFieldConsts
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.push.models.PushConstants
import io.castled.android.notifications.store.CastledSharedStore

internal object PushNotificationManager {
    private val logger = getInstance(LogTags.PUSH)
    private const val DISPLAYED_NOTIFICATIONS = "castled_displayed_notifications"
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
        if (checkNotificationIsDisplayed(pushPayload.notificationId)) {
            return
        }
        logger.debug("Building castled notification...")
//
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

    // Function to check if a Long item is in the array and manage array length
    private fun checkNotificationIsDisplayed(newItem: Int): Boolean {
        val existingItems = CastledSharedStore.getRecentDisplayedNotifications()
        val isItemPresent = newItem in existingItems
        if (!isItemPresent) {
            existingItems.add(newItem)
            CastledSharedStore.setRecentDisplayedNotifications(existingItems)
        }
        return isItemPresent
    }

}