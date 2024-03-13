package io.castled.android.notifications.push.views.templates

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.utils.CastledPushMessageUtils.getChannelId
import io.castled.android.notifications.push.views.PushBaseBuilder
import io.castled.android.notifications.push.views.PushBuilderConfigurator
import kotlinx.coroutines.CoroutineScope

internal class CastledDefaultNotificationBuilder(
    context: Context,
    pushMessage: CastledPushMessage,
    externalScope: CoroutineScope

) : PushBaseBuilder(context, pushMessage, externalScope) {

    override val coverImageBitmap: Bitmap? = null
    override lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var configurator: PushBuilderConfigurator

    override suspend fun build() {

        notificationBuilder = NotificationCompat.Builder(
            context, pushMessage.getChannelId()
        )
        configurator = PushBuilderConfigurator(context, pushMessage, notificationBuilder)

        notificationBuilder.setContentTitle(pushMessage.title)

        notificationBuilder.setAutoCancel(true)

        configureNotification()

        display()
    }

    override fun display() {
        NotificationManagerCompat.from(context)
            .notify(pushMessage.notificationId, notificationBuilder.build())
    }

    override fun close() {
    }

    private suspend fun configureNotification() {
        configurator.setChannel()

        // Notification click action
        configurator.addNotificationAction()

        // Action buttons
        configurator.addActionButtons()

        // Dismiss action
        configurator.addDiscardAction()

        configurator.setTimeout()

        configurator.setPriority()

        configurator.setSmallIcon()

        configurator.setLargeIcon()

        configurator.setSummaryAndBody()

        configurator.setImage()
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}