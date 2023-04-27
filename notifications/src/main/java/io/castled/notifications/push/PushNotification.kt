package io.castled.notifications.push

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.RemoteMessage
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.CastledPushMessage
import io.castled.notifications.push.models.NotificationActionContext
import io.castled.notifications.push.models.PushTokenInfo
import io.castled.notifications.push.models.PushTokenType
import io.castled.notifications.push.service.PushRepository
import io.castled.notifications.store.CastledSharedStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.full.primaryConstructor

internal object PushNotification {

    private val logger = CastledLogger.getInstance(LogTags.PUSH)

    private val tokenProviders = mutableMapOf<PushTokenType, CastledPushTokenProvider>()

    private lateinit var externalScope: CoroutineScope
    private lateinit var pushRepository: PushRepository

    var isAppInForeground = true

    internal fun init(context: Context, externalScope: CoroutineScope) {

        this.externalScope = externalScope
        this.pushRepository = PushRepository(context)

        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _: LifecycleOwner?, event: Lifecycle.Event ->
            if (event == Lifecycle.Event.ON_START) {
                logger.verbose("App in foreground")
                this@PushNotification.isAppInForeground = true
            } else if (event == Lifecycle.Event.ON_STOP) {
                logger.verbose("App in background")
                this@PushNotification.isAppInForeground = false
            }
        })
        initTokenProviders(context)
        refreshPushTokens(context)
    }

    suspend fun registerUser(userId: String) {
        val tokens = mutableListOf<PushTokenInfo>()
        PushTokenType.values().forEach { tokenType ->
            CastledSharedStore.getToken(tokenType)?.let { tokenVal ->
                tokens.add(PushTokenInfo(tokenVal, tokenType))
            }
        }
        if (tokens.isNotEmpty()) {
            pushRepository.register(userId, tokens)
        }
    }

    fun reportPushEvent(actionContext: NotificationActionContext) =
        externalScope.launch(Dispatchers.Default) {
            pushRepository.reportEvent(actionContext)
        }

    private fun initTokenProviders(context: Context) {
        PushTokenType.values().forEach {
            try {
                val javaClass = Class.forName(it.providerClassName)
                val constructor = javaClass.kotlin.primaryConstructor!!
                this.tokenProviders[it] = constructor.call(context) as CastledPushTokenProvider
                this.tokenProviders[it]?.register(context)
            } catch (e: ClassNotFoundException) {
                logger.debug("Class ${it.providerClassName} not found!")
            }
        }
    }

    private fun refreshPushTokens(context: Context) = externalScope.launch(Dispatchers.Default) {
        PushTokenType.values().forEach {
            val token = tokenProviders[it]?.getToken(context)
            this@PushNotification.onTokenFetch(token, it)
        }
    }

    suspend fun onTokenFetch(token: String?, tokenType: PushTokenType) {
        if (CastledSharedStore.getToken(tokenType) != token) {
            // New token
            CastledSharedStore.setToken(token, tokenType)
            val userId = CastledSharedStore.getUserId()
            if (!userId.isNullOrEmpty() && !token.isNullOrEmpty()) {
                // TODO: Reset the server token if new token is null
                pushRepository.register(userId, listOf(PushTokenInfo(token, tokenType)))
            }
        }
    }

    suspend fun handlePushNotification(context: Context, pushMessage: CastledPushMessage) {
        PushNotificationManager.handleNotification(context, pushMessage)
    }

    fun isCastledPushMessage(remoteMessage: RemoteMessage) : Boolean =
        PushNotificationManager.isCastledNotification(remoteMessage)


}