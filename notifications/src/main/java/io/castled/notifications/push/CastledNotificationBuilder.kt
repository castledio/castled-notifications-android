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
import io.castled.notifications.push.PushNotificationManager.getOrCreateNotificationChannel
import io.castled.notifications.push.models.*
import io.castled.notifications.commons.CastledIdUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URL

internal class CastledNotificationBuilder(private val context: Context) {

    fun buildNotification(pushPayload: CastledPushPayload): Notification {
        val notificationBuilder = NotificationCompat.Builder(
            context, getChannelId(pushPayload)
        )

        notificationBuilder.setContentTitle(pushPayload.title)

        // Priority
        setPriority(notificationBuilder, pushPayload)

        // Small Icon
        setSmallIcon(notificationBuilder, pushPayload)

        // Large icon
        setLargeIcon(notificationBuilder, pushPayload)

        // Image
        setImage(notificationBuilder, pushPayload)

        // Channel
        setChannel(notificationBuilder, pushPayload)

        // notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
        // notificationBuilder.bigText(emailObject.getSubjectAndSnippet()))
        setSummaryAndBody(notificationBuilder, pushPayload)
        setTimeout(notificationBuilder, pushPayload)

        // Notification click action
        addNotificationAction(notificationBuilder, pushPayload)

        // Action buttons
        addActionButtons(notificationBuilder, pushPayload)

        // Dismiss action
        addDiscardAction(notificationBuilder, pushPayload)
        notificationBuilder.setAutoCancel(true)
        return notificationBuilder.build()
    }

    private fun setSummaryAndBody(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        val summary = payload.summary
        val body = payload.body
        val image = payload.imageUrl
        if (image.isNullOrBlank()) {
            if (!summary.isNullOrBlank()) {
                notificationBuilder.setContentText(summary)
            }
        } else {
            if (!summary.isNullOrBlank()) {
                val summaryAndBody = String.format("%s\n%s", summary, body)
                notificationBuilder.setContentText(summary)
                notificationBuilder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(summaryAndBody)
                )
            } else {
                notificationBuilder.setContentText(body)
            }
        }
    }

    private fun setPriority(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        notificationBuilder.priority = when (payload.priority) {
            PushPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private fun setSmallIcon(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        val resources = context.resources
        val smallIcon = payload.smallIconResourceId
        val resourceId = smallIcon?.let {
            resources.getIdentifier(it, "drawable", context.packageName)
        } ?: 0

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

    private fun setLargeIcon(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        val largeIconUrl = payload.largeIconUrl
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
        }
    }

    private fun addNotificationAction(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        val pendingIntent = createNotificationIntent(
            NotificationActionContext(
                notificationId = payload.notificationId,
                teamId = payload.teamId,
                sourceContext = payload.sourceContext,
                eventType = NotificationEventType.CLICKED.toString(),
                actionUri = payload.clickActionUrl,
                actionType = payload.clickAction.toString(),
                actionLabel = null,
                keyVals = payload.keyVals
            )
        )
        notificationBuilder.setContentIntent(pendingIntent)
    }

    private fun addActionButtons(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        payload.actionButtons?.forEach { actionButton ->
            val event = NotificationActionContext(
                notificationId = payload.notificationId,
                teamId = payload.teamId,
                sourceContext = payload.sourceContext,
                eventType = if (actionButton.clickAction == ClickAction.DISMISS_NOTIFICATION) {
                    NotificationEventType.DISCARDED.toString()
                } else {
                    NotificationEventType.CLICKED.toString()
                },
                actionUri = actionButton.url,
                actionType = actionButton.clickAction.toString(),
                actionLabel = actionButton.label,
                keyVals = actionButton.keyVals
            )
            val pendingIntent = if (actionButton.clickAction == ClickAction.DISMISS_NOTIFICATION) {
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
        payload: CastledPushPayload
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
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
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
        payload: CastledPushPayload
    ) {
        val channelId = payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_id)
        val channelName = payload.channelName.takeUnless { it.isNullOrBlank() } ?: channelId
        val channelDesc = payload.channelDescription.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_desc)

        notificationBuilder.setChannelId(
            getOrCreateNotificationChannel(context, channelId, channelName, channelDesc)
        )
    }

    private fun getChannelId(payload: CastledPushPayload): String {
        return payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_id)
    }

    private fun setTimeout(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        payload.ttl?.let { notificationBuilder.setTimeoutAfter(it) }
    }

    private fun setImage(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushPayload
    ) {
        payload.imageUrl?.let { imageUrl ->
            val bitmap = getBitmapFromUrl(imageUrl)
            val style = NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setSummaryText(payload.body)

            notificationBuilder.setStyle(style)
        }
    }

    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        try {
            val url = URL(imageUrl)
            return BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            logger.error(e.message ?: "Bitmap fetch failed!", e)
        }
        return null
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)

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
    }
}