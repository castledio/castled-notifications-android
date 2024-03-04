package io.castled.android.notifications.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.CastledIdUtils
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledClickAction
import io.castled.android.notifications.push.models.CastledNotificationFieldConsts
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationActionContext
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.push.models.PushConstants
import io.castled.android.notifications.push.views.PushBuilderFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    suspend fun displayNotification(context: Context, pushMessage: CastledPushMessage) {
        logger.debug("Building castled notification...")

        val pushBuilder = PushBuilderFactory.createPushBuilder(context, pushMessage)
        pushBuilder?.let {
            it.build()
            val notificationBuilder = it.notificationBuilder

            setChannel(context, notificationBuilder, pushMessage)

            // Notification click action
            addNotificationAction(context, notificationBuilder, pushMessage)

            // Action buttons
            addActionButtons(context, notificationBuilder, pushMessage)

            // Dismiss action
            addDiscardAction(context, notificationBuilder, pushMessage)

            it.display()

        }
//        val notification =
//            CastledNotificationBuilder(context).buildNotification(pushMessage)
//        NotificationManagerCompat.from(context)
//            .notify(pushMessage.notificationId, notification)

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

    private fun addNotificationAction(
        context: Context,
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        val actionUri = payload.pushMessageFrames[0].clickActionUrl
        val action = payload.pushMessageFrames[0].clickAction
        val keyVals = payload.pushMessageFrames[0].keyVals

        val pendingIntent = createNotificationIntent(
            context,
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
        context: Context,
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
                    createDiscardIntent(context, event)
                } else {
                    createNotificationIntent(context, event)
                }

            val action = NotificationCompat.Action(0, actionButton.label, pendingIntent)
            notificationBuilder.addAction(action)
        }
    }

    private fun addDiscardAction(
        context: Context,
        notificationBuilder: NotificationCompat.Builder,
        payload: CastledPushMessage
    ) {
        val pendingIntent = createDiscardIntent(
            context,
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

    private fun setChannel(
        context: Context,
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

    fun getChannelId(payload: CastledPushMessage): String {
        return payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: PushConstants.CASTLED_DEFAULT_CHANNEL_ID
    }

}