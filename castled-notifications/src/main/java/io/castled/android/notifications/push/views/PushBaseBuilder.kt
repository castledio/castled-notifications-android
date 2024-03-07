package io.castled.android.notifications.push.views

import android.content.Context
import androidx.core.app.NotificationCompat
import io.castled.android.notifications.push.models.CastledPushMessage
import kotlinx.coroutines.CoroutineScope

abstract class PushBaseBuilder(
    val context: Context,
    val pushMessage: CastledPushMessage,
    val externalScope: CoroutineScope

) {
    abstract val notificationBuilder: NotificationCompat.Builder
    abstract suspend fun build()
    abstract suspend fun display()
    abstract fun close()
}