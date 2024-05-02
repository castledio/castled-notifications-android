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
import io.castled.android.notifications.push.utils.RemoteViewUtils
import io.castled.android.notifications.push.views.CastledRemoteNotificationBuilder
import io.castled.android.notifications.push.views.PushBaseBuilder
import io.castled.android.notifications.push.views.PushServiceBinder
import io.castled.android.notifications.push.views.PushServiceListener
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class CountdownTimerProgressBarPushBuilder(
    context: Context,
    pushMessage: CastledPushMessage
) : PushBaseBuilder(context, pushMessage),
    PushServiceListener {

    override var coverImageBitmap: Bitmap? = null
    private lateinit var pushServiceBinder: PushServiceBinder
    override lateinit var notificationBuilder: NotificationCompat.Builder

    private var startTimeInMillis = 0L
    private var endTimeInMillis = 0L

    override fun build() {

        val currentTimeMillis = System.currentTimeMillis()
        startTimeInMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(15)
        endTimeInMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(715)

        coverImageBitmap = RemoteViewUtils.getRemoteViewBitmapFrom(pushMessage)
        // Initialize notification builder
        notificationBuilder = CastledRemoteNotificationBuilder(context, pushMessage)

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
        val progress = ceil(
            ((System.currentTimeMillis() - startTimeInMillis).coerceAtLeast(0) * 100 / (endTimeInMillis - startTimeInMillis)).coerceAtMost(
                100
            ).toFloat()
        ).toInt()

        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        smallLayout?.apply {
            setTextViewText(
                R.id.txt_elapsed_time,
                timeString
            )
            RemoteViewUtils.setProgressViewProgress(
                smallLayout,
                R.id.progress_bar_elapsed_time,
                progress
            )
        }
        largeLayout?.apply {
            setTextViewText(
                R.id.txt_elapsed_time,
                timeString
            )
            RemoteViewUtils.setProgressViewProgress(
                largeLayout,
                R.id.progress_bar_elapsed_time,
                progress
            )
        }
    }

    private fun createNotification(millisUntilFinished: Long = 0) {

        // Set custom layout for large notification
        val largeLayout =
            RemoteViews(context.packageName, R.layout.countdown_notification_progress_bar_large)
        notificationBuilder.setCustomBigContentView(largeLayout)

        // Set custom layout for small notification
        val smallLayout =
            RemoteViews(context.packageName, R.layout.countdown_notification_progress_bar_small)
        notificationBuilder.setCustomContentView(smallLayout)

        customizeViews(millisUntilFinished, smallLayout, largeLayout)

        display()
    }


    private fun customizeViews(
        millisUntilFinished: Long,
        smallLayout: RemoteViews,
        largeLayout: RemoteViews
    ) {
        smallLayout.setTextViewText(R.id.txt_title, pushMessage.title)
        smallLayout.setTextViewText(R.id.txt_body, pushMessage.body)

        largeLayout.setTextViewText(R.id.txt_title, pushMessage.title)
        largeLayout.setTextViewText(R.id.txt_body, pushMessage.body)


        largeLayout.setProgressBar(R.id.progress_bar_elapsed_time, 100, 0, false)
        smallLayout.setProgressBar(R.id.progress_bar_elapsed_time, 100, 0, false)

        updateCountdownTimerViews(smallLayout, largeLayout, millisUntilFinished)


        if (coverImageBitmap == null) {
            largeLayout.setViewVisibility(R.id.img_large, View.GONE)
        } else {
            largeLayout.setImageViewBitmap(
                R.id.img_large,
                coverImageBitmap
            )
        }

        val timeColor = ColorUtils.parseColor("#990011", Color.BLACK)
        smallLayout.setTextColor(R.id.txt_elapsed_time, timeColor)
        smallLayout.setTextColor(R.id.txt_title, timeColor)
        largeLayout.setTextColor(R.id.txt_elapsed_time, timeColor)
        largeLayout.setTextColor(R.id.txt_title, timeColor)

        RemoteViewUtils.setProgressViewTintColor(
            largeLayout,
            R.id.progress_bar_elapsed_time,
            timeColor
        )
        RemoteViewUtils.setProgressViewTintColor(
            smallLayout,
            R.id.progress_bar_elapsed_time,
            timeColor
        )

        RemoteViewUtils.setProgressViewBackgroundColor(
            largeLayout,
            R.id.progress_bar_elapsed_time,
            timeColor
        )
        RemoteViewUtils.setProgressViewBackgroundColor(
            smallLayout,
            R.id.progress_bar_elapsed_time,
            timeColor
        )
    }
}
