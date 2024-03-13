package io.castled.android.notifications.push.views.templates

import android.content.Context
import android.graphics.Bitmap
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
import io.castled.android.notifications.push.views.PushServiceBinder
import io.castled.android.notifications.push.views.PushServiceListener
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.TimeUnit

class CountdownTimerBoxPushBuilder(
    context: Context,
    pushMessage: CastledPushMessage,
    externalScope: CoroutineScope
) : PushBaseBuilder(context, pushMessage, externalScope), PushServiceListener {

    override var coverImageBitmap: Bitmap? = null
    private lateinit var pushServiceBinder: PushServiceBinder
    private lateinit var configurator: PushBuilderConfigurator
    override lateinit var notificationBuilder: NotificationCompat.Builder

    override suspend fun build() {
        coverImageBitmap = RemoteViewUtils.getRemoteViewBitmapFrom(pushMessage)
        // Initialize notification builder
        notificationBuilder =
            NotificationCompat.Builder(context, pushMessage.getChannelId())
                .setSmallIcon(R.drawable.io_castled_push_default_small_icon)
        notificationBuilder.setOnlyAlertOnce(true)
        notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        configureNotification()

        createNotification(0L)

        pushServiceBinder = PushServiceBinder(context, this, pushMessage, notificationBuilder)
        pushServiceBinder.bindService()

    }

    override fun display() {
        with(NotificationManagerCompat.from(context)) {
            notify(
                pushMessage.notificationId,
                notificationBuilder.build()
            )
        }
    }

    override fun close() {
        pushServiceBinder.unbindService()
    }

    //SERVICE LISTENERS
    override fun onBinderServiceConnected() {
    }

    override fun onBinderServiceDisconnected() {
    }

    override fun onServiceStarted() {
        pushServiceBinder.onServiceStarted()
    }

    override fun onServiceTimerUpdated(millisUntilFinished: Long) {
        createNotification(millisUntilFinished)
    }

    override fun onServiceTimerFinished() {
        timerFinished()
    }

    private fun timerFinished() {
        createNotification(0L)
    }

    private fun updateCountdownTimerViews(
        smallLayout: RemoteViews?,
        largeLayout: RemoteViews?,
        millisUntilFinished: Long
    ) {
        val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % TimeUnit.HOURS.toMinutes(1)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % TimeUnit.MINUTES.toSeconds(1)

        smallLayout?.apply {
            setTextViewText(R.id.txt_hours, String.format("%02d", hours))
            setTextViewText(R.id.txt_minutes, String.format("%02d", minutes))
            setTextViewText(R.id.txt_seconds, String.format("%02d", seconds))
        }
        largeLayout?.apply {
            setTextViewText(R.id.txt_hours, String.format("%02d", hours))
            setTextViewText(R.id.txt_minutes, String.format("%02d", minutes))
            setTextViewText(R.id.txt_seconds, String.format("%02d", seconds))
        }
    }

    private fun createNotification(millisUntilFinished: Long = 0) {

        val largeLayout =
            RemoteViews(context.packageName, R.layout.countdown_notification_box_large)
        notificationBuilder.setCustomBigContentView(largeLayout)

        // Set custom layout for small notification
        val smallLayout =
            RemoteViews(context.packageName, R.layout.countdown_notification_box_small)
        notificationBuilder.setCustomContentView(smallLayout)
        notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        customizeViews(millisUntilFinished, smallLayout, largeLayout)

        display()
    }


    private fun customizeViews(
        millisUntilFinished: Long,
        smallLayout: RemoteViews,
        largeLayout: RemoteViews
    ) {
        smallLayout?.setTextViewText(R.id.txt_title, pushMessage.title)
        smallLayout?.setTextViewText(R.id.txt_body, pushMessage.body)
        largeLayout?.setTextViewText(R.id.txt_title, pushMessage.title)
        largeLayout?.setTextViewText(R.id.txt_body, pushMessage.body)

        updateCountdownTimerViews(smallLayout, largeLayout, millisUntilFinished)

        if (coverImageBitmap == null) {
            largeLayout?.setViewVisibility(R.id.img_large, View.GONE)
        } else {
            largeLayout?.setImageViewBitmap(
                R.id.img_large,
                coverImageBitmap
            )
        }

        val timeColor = ColorUtils.parseColor("#990011", Color.WHITE)
        RemoteViewUtils.setRemoteViewBackgroundColor(largeLayout, R.id.txt_seconds, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(largeLayout, R.id.txt_minutes, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(largeLayout, R.id.txt_hours, timeColor)

        RemoteViewUtils.setRemoteViewBackgroundColor(smallLayout, R.id.txt_seconds, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(smallLayout, R.id.txt_minutes, timeColor)
        RemoteViewUtils.setRemoteViewBackgroundColor(smallLayout, R.id.txt_hours, timeColor)
    }

    private fun configureNotification() {
        val configurator = PushBuilderConfigurator(context, pushMessage, notificationBuilder)

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
