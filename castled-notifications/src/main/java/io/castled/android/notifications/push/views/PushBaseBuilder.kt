package io.castled.android.notifications.push.views

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import io.castled.android.notifications.push.models.CastledPushMessage
import kotlinx.coroutines.CoroutineScope

abstract class PushBaseBuilder(
    val context: Context,
    val pushMessage: CastledPushMessage,
    val externalScope: CoroutineScope

) {
    abstract val notificationBuilder: NotificationCompat.Builder
    abstract val coverImageBitmap: Bitmap?
    abstract suspend fun build()
    abstract fun display()
    abstract fun close()
}