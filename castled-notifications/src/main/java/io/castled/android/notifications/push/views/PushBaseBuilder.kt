package io.castled.android.notifications.push.views

import android.content.Context
import androidx.core.app.NotificationCompat
import io.castled.android.notifications.push.models.CastledPushMessage

abstract class PushBaseBuilder(
    val context: Context,
    val pushMessage: CastledPushMessage
) {
    abstract val notificationBuilder: NotificationCompat.Builder
    abstract suspend fun build()
    abstract suspend fun display()
    abstract suspend fun close()
}