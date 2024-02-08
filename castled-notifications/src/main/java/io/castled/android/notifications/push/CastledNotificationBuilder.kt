package io.castled.android.notifications.push

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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.R
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.*
import io.castled.android.notifications.commons.CastledIdUtils
import io.castled.android.notifications.push.models.PushConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        val summary = payload.summary
        val body = payload.body

        if (!summary.isNullOrBlank()) {
            notificationBuilder.setSubText(summary)
        }
        notificationBuilder.setContentText(body)
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
        val resourceId = if (!smallIcon.isNullOrBlank()) {
            resources.getIdentifier(smallIcon, "drawable", context.packageName)
        } else if (drawableExistsAndDefined(
                context,
                R.drawable.io_castled_push_default_small_icon
            )
        ) {
            R.drawable.io_castled_push_default_small_icon
        } else {
            0
        }
        when {
            resourceId > 0 -> {
                notificationBuilder.setSmallIcon(resourceId)
            }
            else -> {
                val appIcon = IconCompat.createWithBitmap(
                    getBitmapFromDrawable(
                        context.applicationInfo.loadIcon(
                            context.packageManager
                        )
                    )
                )
                notificationBuilder.setSmallIcon(appIcon)
            }
        }
    }

    private fun drawableExistsAndDefined(context: Context, resourceId: Int): Boolean {
        if (resourceId != 0) {
            return try {
                val drawable = ContextCompat.getDrawable(context, resourceId)
                drawable != null
            } catch (e: Exception) {
                false
            }
        }
        return false
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
        val largeIconResourceId =
            if (drawableExistsAndDefined(context, R.drawable.io_castled_push_default_large_icon)) {
                R.drawable.io_castled_push_default_large_icon
            } else {
                0
            }

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
        val imageUrl = payload.pushMessageFrames[0].imageUrl
        val body = payload.body
        val title = payload.title
        if (!imageUrl.isNullOrBlank()) {
            val bitmap = getBitmapFromUrl(imageUrl)
            val style = NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setSummaryText(body)
                .setBigContentTitle(title)
            notificationBuilder.setStyle(style)
        }
    }

    private suspend fun getBitmapFromUrl(imageUrl: String?): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 15000 // 15 seconds
            connection.getInputStream().use { inputStream ->
                return@withContext BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) { // Catch general exceptions
            logger.debug("Bitmap fetch failed, reason: ${e.message}")
        }
        return@withContext null
    }

    private fun addNotificationAction(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        val actionUri = payload.pushMessageFrames[0].clickActionUrl
        val action = payload.pushMessageFrames[0].clickAction
        val keyVals = payload.pushMessageFrames[0].keyVals

        val pendingIntent = createNotificationIntent(
            NotificationActionContext(
                notificationId = payload.notificationId,
                sourceContext = payload.sourceContext,
                eventType = NotificationEventType.CLICKED.toString(),
                actionUri = actionUri,
                actionType = action.toString(),
                actionLabel = null,
                keyVals = keyVals
            )
        )
        notificationBuilder.setContentIntent(pendingIntent)
    }

    private fun addActionButtons(
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        payload.actionButtons?.forEach { actionButton ->
            val event = NotificationActionContext(
                notificationId = payload.notificationId,
                sourceContext = payload.sourceContext,
                eventType = if (actionButton.clickAction == CastledClickAction.DISMISS_NOTIFICATION) {
                    NotificationEventType.DISCARDED.toString()
                } else {
                    NotificationEventType.CLICKED.toString()
                },
                actionUri = actionButton.url,
                actionType = actionButton.clickAction.toString(),
                actionLabel = actionButton.label,
                keyVals = actionButton.keyVals
            )
            val pendingIntent =
                if (actionButton.clickAction == CastledClickAction.DISMISS_NOTIFICATION) {
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
        payload: CastledPushMessage
    ) {
        val channelId = payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: PushConstants.CASTLED_DEFAULT_CHANNEL_ID
        val channelDesc = payload.channelDescription.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_desc)
         notificationBuilder.setChannelId(
            PushNotificationManager.getOrCreateNotificationChannel(
                context,
                channelId,
                channelDesc
            )
        )
    }

    private fun getChannelId(payload: CastledPushMessage): String {
        return payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: PushConstants.CASTLED_DEFAULT_CHANNEL_ID
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