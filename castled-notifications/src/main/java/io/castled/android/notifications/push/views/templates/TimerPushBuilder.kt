package io.castled.android.notifications.push.views.templates

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.castled.android.notifications.R
import io.castled.android.notifications.push.PushNotificationManager
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.views.PushBaseBuilder
import java.util.concurrent.TimeUnit

class TimerPushBuilder(
    context: Context,
    pushMessage: CastledPushMessage
) : PushBaseBuilder(context, pushMessage) {

    private lateinit var notificationManager: NotificationManagerCompat
    private var countDownTimer: CountDownTimer? = null
    override lateinit var notificationBuilder: NotificationCompat.Builder

    override suspend fun build() {
        notificationManager = NotificationManagerCompat.from(context)

        // Initialize notification builder
        notificationBuilder =
            NotificationCompat.Builder(context, PushNotificationManager.getChannelId(pushMessage))
                .setSmallIcon(R.drawable.io_castled_push_default_small_icon)

        notificationBuilder.setOnlyAlertOnce(true)
        // Set custom layout for large notification
        val largeLayout = RemoteViews(context.packageName, R.layout.notification_large_layout)
        notificationBuilder.setCustomBigContentView(largeLayout)

        // Set custom layout for small notification
        val smallLayout = RemoteViews(context.packageName, R.layout.notification_small_layout)
        notificationBuilder.setCustomContentView(smallLayout)

    }

    override suspend fun display() {
        val currentTimeMillis = System.currentTimeMillis()
        val futureTimeInMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(20)
        handlePushNotification(futureTimeInMillis)
    }

    override suspend fun close() {
        countDownTimer?.cancel()
    }

    fun handlePushNotification(futureTimeInMillis: Long) {
        // Initialize notification manager

        // Calculate the time difference
        val timeDifferenceInMillis = futureTimeInMillis - System.currentTimeMillis()

        // Start countdown timer
        startCountDown(timeDifferenceInMillis)
    }

    private fun startCountDown(timeDifferenceInMillis: Long) {
        Handler(Looper.getMainLooper()).post {
            countDownTimer = object : CountDownTimer(timeDifferenceInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    // Update notification
                    updateNotification(millisUntilFinished)
                }

                override fun onFinish() {
                    notificationManager.cancel(pushMessage.notificationId)
                }
            }.start()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun updateNotification(millisUntilFinished: Long) {
        // Update countdown timer views in custom layouts
        updateCountdownTimerViews(notificationBuilder.bigContentView, millisUntilFinished)
        updateCountdownTimerViews(notificationBuilder.contentView, millisUntilFinished)

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

            setTextViewText(R.id.hoursTextView, String.format("%02d", hours))
            setTextViewText(R.id.minutesTextView, String.format("%02d", minutes))
            setTextViewText(R.id.secondsTextView, String.format("%02d", seconds))
        }
    }

}
