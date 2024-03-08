package io.castled.android.notifications.push.views.templates

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.IBinder
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.castled.android.notifications.R
import io.castled.android.notifications.commons.ColorUtils
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushConstants
import io.castled.android.notifications.push.utils.RemoteViewUtils
import io.castled.android.notifications.push.views.PushBaseBuilder
import io.castled.android.notifications.push.views.PushBuilderConfigurator
import io.castled.android.notifications.push.views.PushCountdownServiceListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class CountdownTimerProgressBarPushBuilder(
    context: Context,
    pushMessage: CastledPushMessage,
    externalScope: CoroutineScope
) : PushBaseBuilder(context, pushMessage, externalScope),
    PushCountdownServiceListener {

    private var pushTimerService: PushCountdownService? = null
    private lateinit var serviceConnection: ServiceConnection
    private lateinit var configurator: PushBuilderConfigurator
    private lateinit var notificationManager: NotificationManagerCompat
    private var startTimeInMillis = 0L
    private var endTimeInMillis = 0L
    override lateinit var notificationBuilder: NotificationCompat.Builder
    private var smallLayout: RemoteViews? = null
    private var largeLayout: RemoteViews? = null

    override suspend fun build() {

        createNotification()

        val currentTimeMillis = System.currentTimeMillis()

        startTimeInMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(15)
        endTimeInMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(48 * 60 * 60)

        
        val serviceIntent = Intent(context, PushCountdownService::class.java)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                println("onServiceConnected")
                val binder = service as PushCountdownService.PushCountdownServiceBinder
                pushTimerService = binder.getService()
                pushTimerService?.setServiceListener(this@CountdownTimerProgressBarPushBuilder)
                context.startService(serviceIntent)

            }

            override fun onServiceDisconnected(name: ComponentName?) {
                pushTimerService = null
                // Handle service disconnection
            }
        }

        serviceIntent.putExtra(PushConstants.CASTLED_PUSH_MESSAGE, Json.encodeToString(pushMessage))
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

    }

    override suspend fun display() {

    }


    override fun close() {
        pushTimerService?.stopPushService()
        serviceConnection?.let { context.unbindService(it) }
    }


    //SERVICE LISTENERS
    override fun onServiceStarted() {
        println("onServiceStarted")
        pushTimerService?.startForeground(pushMessage.notificationId, notificationBuilder)

        //showNotification()
    }

    override fun onTimerUpdated(millisUntilFinished: Long) {
        updateNotification(millisUntilFinished)
    }

    override fun onTimerFinished() {
        timerFinished()
    }


    private fun timerFinished() {
        RemoteViewUtils.setProgressViewProgress(
            smallLayout,
            R.id.progress_bar_elapsed_time,
            100
        )
        RemoteViewUtils.setProgressViewProgress(
            largeLayout,
            R.id.progress_bar_elapsed_time,
            100
        )
    }

    suspend fun createNotification() {

        notificationManager = NotificationManagerCompat.from(context)

        // Initialize notification builder
        notificationBuilder =
            NotificationCompat.Builder(context, getChannelId(pushMessage))
                .setSmallIcon(R.drawable.io_castled_push_default_small_icon)

        configurator = PushBuilderConfigurator(context, pushMessage, notificationBuilder)

        notificationBuilder.setOnlyAlertOnce(true)
        // Set custom layout for large notification
        largeLayout =
            RemoteViews(context.packageName, R.layout.countdown_notification_progress_bar_large)
        notificationBuilder.setCustomBigContentView(largeLayout)

        // Set custom layout for small notification
        smallLayout =
            RemoteViews(context.packageName, R.layout.countdown_notification_progress_bar_small)
        notificationBuilder.setCustomContentView(smallLayout)
        notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

        configureNotification()

        customizeViews()

    }

    private fun configureNotification() {
        println("configureNotification")
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

            setTextViewText(
                R.id.txt_elapsed_time,
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            )
            val progress = ceil(
                ((System.currentTimeMillis() - startTimeInMillis).coerceAtLeast(0) * 100 / (endTimeInMillis - startTimeInMillis)).coerceAtMost(
                    100
                ).toFloat()
            ).toInt()
            RemoteViewUtils.setProgressViewProgress(
                layout,
                R.id.progress_bar_elapsed_time,
                progress
            )
        }
    }

    private suspend fun customizeViews() {

        smallLayout?.setTextViewText(R.id.txt_title, pushMessage.title)
        smallLayout?.setTextViewText(R.id.txt_body, pushMessage.body)
        largeLayout?.setTextViewText(R.id.txt_title, pushMessage.title)
        largeLayout?.setTextViewText(R.id.txt_body, pushMessage.body)

        val imageUrl = pushMessage.pushMessageFrames[0].imageUrl
        if (!imageUrl.isNullOrBlank()) {
            largeLayout?.setImageViewBitmap(
                R.id.img_large,
                RemoteViewUtils.getRemoteViewBitmapFrom(imageUrl)
            )

        } else {
            largeLayout?.setViewVisibility(R.id.img_large, View.GONE)
        }

        val timeColor = ColorUtils.parseColor("#990011", Color.BLACK)
        smallLayout?.setTextColor(R.id.txt_elapsed_time, timeColor)
        smallLayout?.setTextColor(R.id.txt_title, timeColor)
        largeLayout?.setTextColor(R.id.txt_title, timeColor)
        largeLayout?.setTextColor(R.id.txt_elapsed_time, timeColor)

        largeLayout?.setProgressBar(R.id.progress_bar_elapsed_time, 100, 0, false)
        smallLayout?.setProgressBar(R.id.progress_bar_elapsed_time, 100, 0, false)

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

        RemoteViewUtils.setProgressViewProgress(smallLayout, R.id.progress_bar_elapsed_time, 0)
        RemoteViewUtils.setProgressViewProgress(largeLayout, R.id.progress_bar_elapsed_time, 0)
    }


    fun getChannelId(payload: CastledPushMessage): String {
        return payload.channelId.takeUnless { it.isNullOrBlank() }
            ?: PushConstants.CASTLED_DEFAULT_CHANNEL_ID
    }
}
