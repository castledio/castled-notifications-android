package io.castled.android.notifications.push.views.templates

import android.content.Context
import android.content.ServiceConnection
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.ColorUtils
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.utils.CastledPushMessageUtils.getChannelId
import io.castled.android.notifications.push.utils.RemoteViewUtils
import io.castled.android.notifications.push.views.PushBaseBuilder
import io.castled.android.notifications.push.views.PushBuilderConfigurator
import io.castled.android.notifications.push.views.PushCountdownServiceListener
import io.castled.android.notifications.push.views.PushServiceBinder
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit

class CountdownTimerBoxPushBuilder(
    context: Context,
    pushMessage: CastledPushMessage,
    externalScope: CoroutineScope
) : PushBaseBuilder(context, pushMessage, externalScope), PushCountdownServiceListener {

    private lateinit var pushServiceBinder: PushServiceBinder
    private var pushTimerService: PushCountdownService? = null
    private lateinit var serviceConnection: ServiceConnection
    private lateinit var configurator: PushBuilderConfigurator
    private lateinit var notificationManager: NotificationManagerCompat
    override lateinit var notificationBuilder: NotificationCompat.Builder
    private var smallLayout: RemoteViews? = null
    private var largeLayout: RemoteViews? = null

    override suspend fun build() {
        createNotification()

        pushServiceBinder = PushServiceBinder(context, this, pushMessage, notificationBuilder)
        pushServiceBinder.bindService()
    }

    override suspend fun display() {
        notificationManager.notify(pushMessage.notificationId, notificationBuilder.build())
    }

    override fun close() {
        pushServiceBinder.unbindService()
    }

    //SERVICE LISTENERS
    override fun onServiceConnected() {
    }

    override fun onServiceDisconnected() {
    }

    override fun onServiceStarted() {
        pushServiceBinder.onServiceStarted()
    }

    override fun onServiceTimerUpdated(millisUntilFinished: Long) {
        updateNotification(millisUntilFinished)
    }

    override fun onServiceTimerFinished() {
        timerFinished()
    }

    private fun timerFinished() {
        smallLayout?.setTextViewText(R.id.txt_hours, "00")
        smallLayout?.setTextViewText(R.id.txt_minutes, "00")
        smallLayout?.setTextViewText(R.id.txt_seconds, "00")

        largeLayout?.setTextViewText(R.id.txt_hours, "00")
        largeLayout?.setTextViewText(R.id.txt_minutes, "00")
        largeLayout?.setTextViewText(R.id.txt_seconds, "00")
        notificationManager.notify(pushMessage.notificationId, notificationBuilder.build())

    }

    private fun updateNotification(millisUntilFinished: Long) {
        // Update countdown timer views in custom layouts
        updateCountdownTimerViews(smallLayout, millisUntilFinished)
        updateCountdownTimerViews(largeLayout, millisUntilFinished)

        // Show the updated notification
        notificationManager.notify(pushMessage.notificationId, notificationBuilder.build())
    }

    private fun updateCountdownTimerViews(layout: RemoteViews?, millisUntilFinished: Long) {
        layout?.apply {
            val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
            val minutes =
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1)
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)

            setTextViewText(R.id.txt_hours, String.format("%02d", hours))
            setTextViewText(R.id.txt_minutes, String.format("%02d", minutes))
            setTextViewText(R.id.txt_seconds, String.format("%02d", seconds))
        }
    }

    private suspend fun createNotification() {
        notificationManager = NotificationManagerCompat.from(context)

        // Initialize notification builder
        notificationBuilder =
            NotificationCompat.Builder(context, pushMessage.getChannelId())
                .setSmallIcon(R.drawable.io_castled_push_default_small_icon)

        configurator = PushBuilderConfigurator(context, pushMessage, notificationBuilder)

        notificationBuilder.setOnlyAlertOnce(true)
        // Set custom layout for large notification
        largeLayout = RemoteViews(context.packageName, R.layout.countdown_notification_box_large)
        notificationBuilder.setCustomBigContentView(largeLayout)

        // Set custom layout for small notification
        smallLayout = RemoteViews(context.packageName, R.layout.countdown_notification_box_small)
        notificationBuilder.setCustomContentView(smallLayout)
        notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        configureNotification()
        customizeViews()
    }

    private fun configureNotification() {
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

    private suspend fun customizeViews() {

        smallLayout?.setTextViewText(R.id.txt_title, pushMessage.title)
        smallLayout?.setTextViewText(R.id.txt_body, pushMessage.body)
        largeLayout?.setTextViewText(R.id.txt_title, pushMessage.title)
        largeLayout?.setTextViewText(R.id.txt_body, pushMessage.body)

        smallLayout?.setTextViewText(R.id.txt_hours, "00")
        smallLayout?.setTextViewText(R.id.txt_minutes, "00")
        smallLayout?.setTextViewText(R.id.txt_seconds, "00")

        largeLayout?.setTextViewText(R.id.txt_hours, "00")
        largeLayout?.setTextViewText(R.id.txt_minutes, "00")
        largeLayout?.setTextViewText(R.id.txt_seconds, "00")

        val imageUrl = pushMessage.pushMessageFrames[0].imageUrl
        if (!imageUrl.isNullOrBlank()) {
            largeLayout?.setImageViewBitmap(
                R.id.img_large,
                RemoteViewUtils.getRemoteViewBitmapFrom(imageUrl)
            )

        } else {
            largeLayout?.setViewVisibility(R.id.img_large, View.GONE)
        }

        val timeColor = ColorUtils.parseColor("#990011", Color.MAGENTA)
        RemoteViewUtils.setRemoteViewBackgroundColor(largeLayout, R.id.txt_seconds, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(largeLayout, R.id.txt_minutes, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(largeLayout, R.id.txt_hours, timeColor)

        RemoteViewUtils.setRemoteViewBackgroundColor(smallLayout, R.id.txt_seconds, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(smallLayout, R.id.txt_minutes, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(smallLayout, R.id.txt_hours, timeColor)
    }
}
