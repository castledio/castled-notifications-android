package io.castled.notifications.push

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.NotificationEvent
import io.castled.notifications.push.service.PushRepository
import io.castled.notifications.store.CastledSharedStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal object PushNotification {

    private val logger = CastledLogger.getInstance(LogTags.PUSH)

    private lateinit var externalScope : CoroutineScope
    private lateinit var pushRepository : PushRepository

    var isAppInForeground = true

    internal fun init(externalScope: CoroutineScope) {

        this.externalScope = externalScope
        this.pushRepository = PushRepository(CastledSharedStore.getApiKey()!!)

        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _: LifecycleOwner?, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_START) {
                logger.verbose("App in foreground")
                this@PushNotification.isAppInForeground = true
            } else if (event == Lifecycle.Event.ON_STOP) {
                logger.verbose("App in background")
                this@PushNotification.isAppInForeground = false
            }
        })
    }

    suspend fun registerUser(userId: String, token: String?)  {
        pushRepository.register(userId, token)
    }

    fun reportPushEvent(event: NotificationEvent) = externalScope.launch(Dispatchers.Default) {
        pushRepository.reportEvent(event)
    }

}