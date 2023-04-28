package io.castled.notifications.push

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import io.castled.notifications.R
import io.castled.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.*
import io.castled.notifications.commons.CastledIdUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URL

internal class CastledNotificationBuilder(private val context: Context) {

    suspend fun buildNotification(pushMessage: CastledPushMessage): Notification {
        val notificationBuilder = NotificationCompat.Builder(
            context, getChannelId(pushMessage)
        )

        notificationBuilder.setContentTitle(pushMessage.title)

        // Priority
        setPriority(notificationBuilder, pushMessage)

        // Small Icon
        setSmallIcon(notificationBuilder, pushMessage)

        // Large icon
        setLargeIcon(notificationBuilder, pushMessage)

        setSummaryAndBody(notificationBuilder, pushMessage)

        // Image
        setImage(notificationBuilder, pushMessage)

        // Channel
        setChannel(notificationBuilder, pushMessage)

        setTimeout(notificationBuilder, pushMessage)

        // Notification click action
        addNotificationAction(notificationBuilder, pushMessage)

        // Action buttons
        addActionButtons(notificationBuilder, pushMessage)

        // Dismiss action
        addDiscardAction(notificationBuilder, pushMessage)
        notificationBuilder.setAutoCancel(true)
        return notificationBuilder.build()
    }

    private fun setSummaryAndBody(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        if (!payload.summary.isNullOrBlank()) {
            notificationBuilder.setSubText(payload.summary)
        }
        notificationBuilder.setContentText(payload.body)
    }

    private fun setPriority(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        notificationBuilder.priority = when (payload.priority) {
            CastledPushPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private suspend fun setSmallIcon(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) = withContext(Dispatchers.IO) {
        val resources = context.resources
        val smallIcon = payload.smallIconResourceId
        var resourceId = 0
        if (!smallIcon.isNullOrBlank()) {
            resourceId = resources.getIdentifier(smallIcon, "drawable", context.packageName)
        }

        when {
            resourceId > 0 -> {
                notificationBuilder.setSmallIcon(resourceId)
            }
            else -> {
                notificationBuilder.setSmallIcon(
                    IconCompat.createWithBitmap(
                        getBitmapFromDrawable(
                            context.applicationInfo.loadIcon(
                                context.packageManager
                            )
                        )
                    )
                )
            }
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    private suspend fun setLargeIcon(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) = withContext(Dispatchers.IO) {
        val largeIconUrl = payload.largeIconUri
        val largeIconResourceId = R.drawable.io_castled_push_notification_large_icon

        when {
            !largeIconUrl.isNullOrBlank() -> {
                notificationBuilder.setLargeIcon(getBitmapFromUrl(largeIconUrl))
            }
            largeIconResourceId > 0 -> {
                val image = BitmapFactory.decodeResource(
                    context.resources,
                    largeIconResourceId
                )
                notificationBuilder.setLargeIcon(image)
            }
            else -> {
                // Nothing to do
            }
        }
    }

    private suspend fun setImage(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        if (!payload.imageUrl.isNullOrBlank()) {
            val bitmap = getBitmapFromUrl(payload.imageUrl)
            val style = NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setSummaryText(payload.body)
                .setBigContentTitle(payload.title)

            notificationBuilder.setStyle(style)
            if (payload.imageUrl == payload.largeIconUri) {
                // If both are same, user intends to show only 1 image whether it is expanded or collapsed
                notificationBuilder.setLargeIcon(null)
            }
        }
    }

    private suspend fun getBitmapFromUrl(imageUrl: String?): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            return@withContext BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            logger.error(e.message ?: "Bitmap fetch failed!", e)
        }
        return@withContext null
    }

    private fun addNotificationAction(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        val pendingIntent = createNotificationIntent(
            NotificationActionContext(
                notificationId = payload.notificationId,
                teamId = payload.teamId,
                sourceContext = payload.sourceContext,
                eventType = NotificationEventType.CLICKED.toString(),
                actionUri = payload.clickActionUri,
                actionType = payload.clickAction.toString(),
                actionLabel = null,
                keyVals = payload.keyVals
            )
        )
        notificationBuilder.setContentIntent(pendingIntent)
    }

    private fun addActionButtons(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        payload.castledActionButtons?.forEach { actionButton ->
            val event = NotificationActionContext(
                notificationId = payload.notificationId,
                teamId = payload.teamId,
                sourceContext = payload.sourceContext,
                eventType = if (actionButton.castledClickAction == CastledClickAction.DISMISS_NOTIFICATION) {
                    NotificationEventType.DISCARDED.toString()
                } else {
                    NotificationEventType.CLICKED.toString()
                },
                actionUri = actionButton.url,
                actionType = actionButton.castledClickAction.toString(),
                actionLabel = actionButton.label,
                keyVals = actionButton.keyVals
            )
            val pendingIntent =
                if (actionButton.castledClickAction == CastledClickAction.DISMISS_NOTIFICATION) {
                    createDiscardIntent(event)
                } else {
                    createNotificationIntent(event)
                }

            val action = NotificationCompat.Action(0, actionButton.label, pendingIntent)
            notificationBuilder.addAction(action)
        }
    }

    private fun addDiscardAction(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        val pendingIntent = createDiscardIntent(
            NotificationActionContext(
                notificationId = payload.notificationId,
                teamId = payload.teamId,
                sourceContext = payload.sourceContext,
                eventType = NotificationEventType.DISCARDED.toString(),
                actionUri = null,
                actionType = null,
                actionLabel = null,
                keyVals = null
            )
        )
        notificationBuilder.setDeleteIntent(pendingIntent)
    }

    private fun createDiscardIntent(actionContext: NotificationActionContext): PendingIntent {
        val intent = Intent(context, CastledNotificationReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(PushConstants.CASTLED_EXTRA_NOTIF_CONTEXT, Json.encodeToString(actionContext))
        }

        return PendingIntent.getBroadcast(
            context,
            CastledIdUtils.newId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationIntent(actionContext: NotificationActionContext): PendingIntent {
        val intent = Intent(context, CastledNotificationReceiverAct::class.java).apply {
            putExtra(PushConstants.CASTLED_EXTRA_NOTIF_CONTEXT, Json.encodeToString(actionContext))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it or PendingIntent.FLAG_MUTABLE
            } else {
                it
            }
        }
        return PendingIntent.getActivity(context, CastledIdUtils.newId, intent, flags)
    }

    private fun setChannel(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        val channelId = payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_id)
        val channelName = payload.channelName.takeUnless { it.isNullOrBlank() } ?: channelId
        val channelDesc = payload.channelDescription.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_desc)

        notificationBuilder.setChannelId(
            PushNotificationManager.getOrCreateNotificationChannel(
                context,
                channelId,
                channelName,
                channelDesc
            )
        )
    }

    private fun getChannelId(payload: CastledPushMessage): String {
        return payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_id)
    }

    private fun setTimeout(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        payload.ttl?.let { notificationBuilder.setTimeoutAfter(it) }
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}