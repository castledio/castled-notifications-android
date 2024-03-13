package io.castled.android.notifications.push.views

import android.content.Context
import androidx.core.app.NotificationCompat
import io.castled.android.notifications.R
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.utils.CastledPushMessageUtils.getChannelId

class CastledRemoteNotificationBuilder(
    private val context: Context,
    private val pushMessage: CastledPushMessage
) :
    NotificationCompat.Builder(context, pushMessage.getChannelId()) {

    init {
        setSmallIcon(R.drawable.io_castled_push_default_small_icon)
        setOnlyAlertOnce(true)
        setStyle(NotificationCompat.DecoratedCustomViewStyle())
        configureNotification()
    }

    private fun configureNotification() {
        val configurator = PushBuilderConfigurator(context, pushMessage, this)
        configurator.setChannel()
        // Notification click action
        configurator.addNotificationAction()
        // Action buttons
        configurator.addActionButtons()
        // Dismiss action
        configurator.addDiscardAction()
        configurator.setTimeout()
        configurator.setPriority()
        configurator.setSummaryAndBody()
    }

}