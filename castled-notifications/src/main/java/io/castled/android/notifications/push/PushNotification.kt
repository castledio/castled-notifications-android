package io.castled.android.notifications.push

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.CastledPushNotificationListener
import io.castled.android.notifications.commons.CastledLinkedHashCache
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.extensions.getNotificationDisplayId
import io.castled.android.notifications.push.extensions.toCastledActionContext
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationActionContext
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.push.models.PushTokenInfo
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.push.service.PushRepository
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledTokenRefreshWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.full.primaryConstructor

internal object PushNotification {

    private val logger = CastledLogger.getInstance(LogTags.PUSH)
    private val tokenProviders = mutableMapOf<PushTokenType, CastledPushTokenProvider>()
    private lateinit var externalScope: CoroutineScope
    private lateinit var pushRepository: PushRepository
    private var enabled = false

    private const val MAX_CACHE_SIZE = 24
    private var pushMessageCache: CastledLinkedHashCache<Int, CastledPushMessage>? = null
    private var pushNotificationListener: CastledPushNotificationListener? = null

    internal fun init(context: Context, externalScope: CoroutineScope) {

        this.externalScope = externalScope
        this.pushRepository = PushRepository(context)
        enabled = true
        initTokenProviders(context)
        refreshPushTokens(context)
        startPeriodicTokenRefreshTask(context)
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

    fun reportPushEvent(actionContext: NotificationActionContext) {
        if (!enabled) {
            logger.debug("Ignoring push event, PushEvent disabled")
            return
        }

        // Listener callbacks
        pushNotificationListener?.let {
            val pushMessage = pushMessageCache?.get(actionContext.displayId)
            pushMessage ?: return
            val eventType = NotificationEventType.valueOf(actionContext.eventType)
            when (eventType) {
                NotificationEventType.RECEIVED -> it.onCastledPushReceived(pushMessage)
                NotificationEventType.CLICKED ->
                    it.onCastledPushClicked(
                        pushMessage,
                        actionContext.toCastledActionContext()
                    )

                NotificationEventType.DISCARDED ->
                    it.onCastledPushDismissed(pushMessage)

                else -> logger.debug("Event type: $eventType not handled!")
            }
            if (eventType != NotificationEventType.RECEIVED) {
                pushMessageCache?.remove(actionContext.displayId)
            }
        }
        externalScope.launch(Dispatchers.Default) {
            pushRepository.reportEvent(actionContext)
        }
    }

    fun subscribeToPushNotificationEvents(listener: CastledPushNotificationListener) {
        pushMessageCache = CastledLinkedHashCache(MAX_CACHE_SIZE)
        pushNotificationListener = listener
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
            try {
                val token = tokenProviders[it]?.getToken(context)
                this@PushNotification.onTokenFetch(token, it)
            } catch (e: NoClassDefFoundError) {
                logger.debug("Class definition for ${it.providerClassName} not found!")
            } catch (e: ClassNotFoundException) {
                logger.debug("Class ${it.providerClassName} not found!")
            } catch (e: Exception) {
                logger.debug("$e")
            }
        }
    }

    private fun startPeriodicTokenRefreshTask(context: Context) =
        externalScope.launch(Dispatchers.Default) {
            CastledTokenRefreshWorkManager.getInstance(context).start()
        }

    suspend fun onTokenFetch(token: String?, tokenType: PushTokenType) {
        logger.info("push token: $token, type: $tokenType")
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

    fun handlePushNotification(context: Context, pushMessage: CastledPushMessage?) {
        pushMessage ?: return
        if (!shouldDisplayPushMessage(context, pushMessage)) {
            return
        }
        pushMessageCache?.set(pushMessage.getNotificationDisplayId(), pushMessage)
        externalScope.launch(Dispatchers.Default) {
            PushNotificationManager.displayNotification(context, pushMessage)
        }
    }

    private fun shouldDisplayPushMessage(
        context: Context,
        pushMessage: CastledPushMessage?
    ): Boolean {
        // Payload from Castled server
        pushMessage ?: run {
            logger.debug("Castled push notification empty! skipping notification handling...")
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logger.debug("Do not have push permission!")
            return false
        }
        if (checkAndSetRecentNotificationId(pushMessage.getNotificationDisplayId())) {
            logger.debug("Message already displayed!")
            return false
        }
        return true
    }

    fun isCastledPushMessage(remoteMessage: RemoteMessage): Boolean =
        PushNotificationManager.isCastledNotification(remoteMessage)

    @Synchronized
    private fun checkAndSetRecentNotificationId(newId: Int): Boolean {
        if (newId in CastledSharedStore.getRecentDisplayedPushIds()) {
            return true
        }
        CastledSharedStore.setRecentDisplayedPushId(newId)
        return false
    }


}