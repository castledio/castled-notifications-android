package io.castled.android.notifications.push.views

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushConstants
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class PushServiceBinder(
    private val context: Context,
    private val serviceListener: PushServiceListener,
    private val pushMessage: CastledPushMessage,
    private val notificationBuilder: NotificationCompat.Builder
) {
    private var serviceConnection: ServiceConnection? = null
    private var pushTimerService: PushCountdownService? = null

    fun bindService() {
        try {
            val serviceIntent = Intent(context, PushCountdownService::class.java)
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as PushCountdownService.PushCountdownServiceBinder
                    pushTimerService = binder.getService()
                    pushTimerService?.setServiceListener(serviceListener)
                    serviceListener.onBinderServiceConnected()
                    context.startService(serviceIntent)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    pushTimerService = null
                    serviceListener.onBinderServiceDisconnected()
                }
            }
            serviceIntent.putExtra(
                PushConstants.CASTLED_PUSH_MESSAGE,
                Json.encodeToString(pushMessage)
            )
            context.bindService(serviceIntent, serviceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (_: Exception) {
        }

    }

    fun unbindService() {
        try {
            pushTimerService?.stopPushService()
            serviceConnection?.let {
                context.unbindService(it)
                serviceConnection = null
            }
        } catch (_: Exception) {
        }
    }

    fun onServiceStarted() {
        try {
            pushTimerService?.startForeground(
                pushMessage.notificationId,
                notificationBuilder
            )
        } catch (_: Exception) {
        }
    }
}


