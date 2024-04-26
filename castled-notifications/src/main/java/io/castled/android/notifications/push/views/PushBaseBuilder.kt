package io.castled.android.notifications.push.views

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import io.castled.android.notifications.push.models.CastledPushMessage

abstract class PushBaseBuilder(
    val context: Context,
    val pushMessage: CastledPushMessage
) {
    abstract val notificationBuilder: NotificationCompat.Builder
    abstract val coverImageBitmap: Bitmap?
    abstract fun build()
    abstract fun display()
    abstract fun close()
}