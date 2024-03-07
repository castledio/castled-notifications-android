package io.castled.android.notifications.push.views.templates

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.castled.android.notifications.logger.CastledLogger.Companion.getInstance
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushConstants
import io.castled.android.notifications.push.views.PushBaseBuilder
import io.castled.android.notifications.push.views.PushBuilderConfigurator
import kotlinx.coroutines.CoroutineScope

internal class CastledNotificationBuilder(
    context: Context,
    pushMessage: CastledPushMessage,
    externalScope: CoroutineScope

) : PushBaseBuilder(context, pushMessage, externalScope) {

    override lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var configurator: PushBuilderConfigurator

    override suspend fun build() {

        notificationBuilder = NotificationCompat.Builder(
            context, getChannelId(pushMessage)
        )
        configurator = PushBuilderConfigurator(context, pushMessage, notificationBuilder)

        notificationBuilder.setContentTitle(pushMessage.title)

        notificationBuilder.setAutoCancel(true)

        configureNotification()

    }

    override suspend fun display() {
        NotificationManagerCompat.from(context)
            .notify(pushMessage.notificationId, notificationBuilder.build())
    }

    override fun close() {
        TODO("Not yet implemented")
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


    private fun getChannelId(payload: CastledPushMessage): String {
        return payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: PushConstants.CASTLED_DEFAULT_CHANNEL_ID
    }

    companion object {
        private val logger = getInstance(LogTags.PUSH)
    }
}