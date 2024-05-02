package io.castled.android.notifications.push.views

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
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.CastledIdUtils
import io.castled.android.notifications.push.CastledNotificationReceiver
import io.castled.android.notifications.push.CastledNotificationReceiverAct
import io.castled.android.notifications.push.PushNotificationManager
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.CastledPushPriority
import io.castled.android.notifications.push.models.NotificationActionContext
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.push.models.PushConstants
import io.castled.android.notifications.push.utils.CastledPushMessageUtils.getChannelId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URL

internal class PushBuilderConfigurator(
    private val context: Context,
    private val pushMessage: CastledPushMessage,
    private val notificationBuilder: NotificationCompat.Builder
) {
    fun addNotificationAction() {
        val actionUri = pushMessage.pushMessageFrames[0].clickActionUrl
        val action = pushMessage.pushMessageFrames[0].clickAction
        val keyVals = pushMessage.pushMessageFrames[0].keyVals

        val pendingIntent = createNotificationIntent(
            context,
            NotificationActionContext(
                notificationId = pushMessage.notificationId,
                sourceContext = pushMessage.sourceContext,
                eventType = NotificationEventType.CLICKED.toString(),
                actionUri = actionUri,
                actionType = action.toString(),
                actionLabel = null,
                keyVals = keyVals
            )
        )
        notificationBuilder.setContentIntent(pendingIntent)
    }

    fun addActionButtons() {
        pushMessage.actionButtons?.forEach { actionButton ->
            val event = NotificationActionContext(
                notificationId = pushMessage.notificationId,
                sourceContext = pushMessage.sourceContext,
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
                    createDiscardIntent(context, event)
                } else {
                    createNotificationIntent(context, event)
                }

            val action = NotificationCompat.Action(0, actionButton.label, pendingIntent)
            notificationBuilder.addAction(action)
        }
    }

    fun addDiscardAction() {
        val pendingIntent = createDiscardIntent(
            context,
            NotificationActionContext(
                notificationId = pushMessage.notificationId,
                sourceContext = pushMessage.sourceContext,
                eventType = NotificationEventType.DISCARDED.toString(),
                actionUri = null,
                actionType = null,
                actionLabel = null,
                keyVals = null
            )
        )
        notificationBuilder.setDeleteIntent(pendingIntent)
    }

    private fun createDiscardIntent(
        context: Context,
        actionContext: NotificationActionContext
    ): PendingIntent {
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

    private fun createNotificationIntent(
        context: Context,
        actionContext: NotificationActionContext
    ): PendingIntent {
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

    fun setChannel() {
        val channelId = pushMessage.getChannelId()
        val channelDesc = pushMessage.channelDescription.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.io_castled_push_default_channel_desc)
        notificationBuilder.setChannelId(
            PushNotificationManager.getOrCreateNotificationChannel(
                context,
                channelId,
                channelDesc
            )
        )
    }

    fun setTimeout() {
        pushMessage.ttl?.let { notificationBuilder.setTimeoutAfter(it) }
    }

    fun setPriority() {
        notificationBuilder.priority = when (pushMessage.priority) {
            CastledPushPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    fun setSummaryAndBody() {
        val summary = pushMessage.summary
        val body = pushMessage.body

        if (!summary.isNullOrBlank()) {
            notificationBuilder.setSubText(summary)
        }
        notificationBuilder.setContentText(body)
    }

    internal fun setSmallIcon() {
        val resources = context.resources
        val smallIcon = pushMessage.smallIconResourceId
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

    internal fun setLargeIcon() {
        val largeIconUrl = pushMessage.largeIconUri
        val largeIconResourceId =
            if (drawableExistsAndDefined(context, R.drawable.io_castled_push_default_large_icon)) {
                R.drawable.io_castled_push_default_large_icon
            } else {
                0
            }

        when {
            !largeIconUrl.isNullOrBlank() -> {
                getBitmapFromUrl(largeIconUrl)?.let { notificationBuilder.setLargeIcon(it) }
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

    internal fun setImage() {
        val imageUrl = pushMessage.pushMessageFrames[0].imageUrl
        if (imageUrl.isNullOrBlank()) {
            return
        }
        val bitmap = getBitmapFromUrl(imageUrl) ?: return
        val style = NotificationCompat.BigPictureStyle()
            .bigPicture(bitmap)
            .setSummaryText(pushMessage.body)
            .setBigContentTitle(pushMessage.title)
        notificationBuilder.setStyle(style)
    }

    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? = runBlocking {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 1500 // 1.5 seconds
                connection.readTimeout = 2500 // 2.5 seconds
                connection.getInputStream().use { inputStream ->
                    return@withContext BitmapFactory.decodeStream(inputStream)
                }
            } catch (_: Exception) { // Catch general exceptions
            }
            return@withContext null
        }
    }
}