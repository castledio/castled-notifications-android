package io.castled.android.notifications.push.views

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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class PushCountdownService : Service() {

    private var serviceListener: PushServiceListener? = null
    private val binder = PushCountdownServiceBinder()
    private lateinit var context: Context
    private lateinit var pushMessage: CastledPushMessage
    private var endTimeInMillis = 0L
    private var countDownTimer: CountDownTimer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        context = applicationContext
        val contextJson =
            intent?.extras?.getString(PushConstants.CASTLED_PUSH_MESSAGE)
        pushMessage = Json.decodeFromString(contextJson!!) as CastledPushMessage
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
    fun setServiceListener(listener: PushServiceListener) {
        serviceListener = listener
    }

    //CUSTOM METHODS
    fun stopPushService() {
        invalidateTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceListener = null
        stopSelf()
    }

    fun startForeground(notificationId: Int, notificationBuilder: NotificationCompat.Builder) {
        startForeground(notificationId, notificationBuilder.build())
    }

    //COUNTDOWN RELATED
    private fun invalidateTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun setupCountdownTimer() {
        val currentTimeMillis = System.currentTimeMillis()
        endTimeInMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis(70)
        if (isTimeExceeded()) {
            serviceListener?.onServiceTimerFinished()
            stopSelf()
        } else {
            // Calculate the time difference
            val timeDifferenceInMillis = endTimeInMillis - System.currentTimeMillis()
            // Start countdown timer
            startCountdown(timeDifferenceInMillis)
        }
    }

    private fun startCountdown(timeDifferenceInMillis: Long) {
        countDownTimer = object : CountDownTimer(timeDifferenceInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (isTimeExceeded()) {
                    invalidateTimer()
                    serviceListener?.onServiceTimerFinished()
                    stopSelf()
                } else {
                    serviceListener?.onServiceTimerUpdated(millisUntilFinished)
                }
            }

            override fun onFinish() {
                invalidateTimer()
                serviceListener?.onServiceTimerFinished()
                stopSelf()
            }
        }.start()
    }

    private fun isTimeExceeded(): Boolean = System.currentTimeMillis() > endTimeInMillis
}