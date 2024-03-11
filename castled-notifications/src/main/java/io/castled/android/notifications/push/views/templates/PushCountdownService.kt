package io.castled.android.notifications.push.views.templates

// CountdownService.kt
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushConstants
import io.castled.android.notifications.push.views.PushCountdownServiceListener
import java.util.concurrent.TimeUnit

class PushCountdownService : Service() {

    private var serviceListener: PushCountdownServiceListener? = null
    private val binder = PushCountdownServiceBinder()
    private lateinit var context: Context
    private lateinit var pushMessage: CastledPushMessage
    private var endTimeInMillis = 0L
    private var countDownTimer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        context = applicationContext
        //  val contextJson =
        intent?.extras?.getString(PushConstants.CASTLED_PUSH_MESSAGE)
        // pushMessage = Json.decodeFromString(contextJson!!) as CastledPushMessage
        countDownTimer?.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceListener?.onServiceStarted()
        setupCountdownTimer()
        return START_STICKY
    }

    override fun onDestroy() {
        stopPushService()
        super.onDestroy()

    }

    inner class PushCountdownServiceBinder : Binder() {
        fun getService(): PushCountdownService = this@PushCountdownService

    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    //LISTENER
    fun setServiceListener(listener: PushCountdownServiceListener) {
        serviceListener = listener
    }

    //CUSTOM METHODS
    fun stopPushService() {
        countDownTimer?.cancel()
        countDownTimer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceListener = null
        stopSelf()
    }

    fun startForeground(notificationId: Int, notificationBuilder: NotificationCompat.Builder) {
        startForeground(notificationId, notificationBuilder.build())
    }

    //COUNTDOWN RELATED
    private fun setupCountdownTimer() {

        val currentTimeMillis = System.currentTimeMillis()
        endTimeInMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(48 * 60 * 60)
        // Calculate the time difference
        val timeDifferenceInMillis = endTimeInMillis - System.currentTimeMillis()

        // Start countdown timer
        startCountdown(timeDifferenceInMillis)
    }

    private fun startCountdown(timeDifferenceInMillis: Long) {
        countDownTimer = object : CountDownTimer(timeDifferenceInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                serviceListener?.onServiceTimerUpdated(millisUntilFinished)
            }

            override fun onFinish() {
                serviceListener?.onServiceTimerFinished()
                stopSelf()
            }
        }.start()
    }
}