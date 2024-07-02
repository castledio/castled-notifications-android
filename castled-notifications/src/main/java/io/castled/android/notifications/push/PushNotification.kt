package io.castled.android.notifications.push

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.CastledPushNotificationListener
import io.castled.android.notifications.commons.CastledLinkedHashCache
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.extensions.toCastledActionContext
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationActionContext
import io.castled.android.notifications.push.models.NotificationEventType
import io.castled.android.notifications.push.models.PushTokenInfo
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.push.service.PushRepository
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener
import io.castled.android.notifications.workmanager.CastledPushWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.reflect.full.primaryConstructor

internal object PushNotification : CastledSharedStoreListener {

    private val logger = CastledLogger.getInstance(LogTags.PUSH)
    private val tokenProviders = mutableMapOf<PushTokenType, CastledPushTokenProvider>()
    private lateinit var externalScope: CoroutineScope
    private lateinit var pushRepository: PushRepository
    private var enabled = false

    private const val MAX_PUSH_MESSAGE_CACHE_SIZE = 32
    private var pushMessageCache: CastledLinkedHashCache<Int, CastledPushMessage>? = null
    private var pushNotificationListener: CastledPushNotificationListener? = null

    internal fun init(context: Context, externalScope: CoroutineScope) {

        this.externalScope = externalScope
        this.pushRepository = PushRepository(context)
        CastledSharedStore.registerListener(this)
        enabled = true
        logger.debug("Push module initialized")
    }

    private suspend fun registerUser(userId: String) {
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

        val eventType = NotificationEventType.valueOf(actionContext.eventType)

        // Listener callbacks
        pushNotificationListener?.let {
            val pushMessage = pushMessageCache?.get(actionContext.notificationId)
            pushMessage ?: return
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
                pushMessageCache?.remove(actionContext.notificationId)
            }
        }

        if (eventType == NotificationEventType.RECEIVED) {
            // If received event, this is being run from the main thread of fcm listener.
            // So enqueue first to maximize event delivery chance
            pushRepository.enqueueEvent(actionContext)
        } else {
            externalScope.launch(Dispatchers.Default) {
                pushRepository.reportEvent(actionContext)
            }
        }
    }

    fun subscribeToPushNotificationEvents(listener: CastledPushNotificationListener) {
        pushMessageCache = CastledLinkedHashCache(MAX_PUSH_MESSAGE_CACHE_SIZE)
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
            } catch (e: Exception) {
                logger.debug("$e")
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
        CastledPushWorkManager.getInstance(context).startTokenRefresh()

    private fun startPushBoostSyncTask(context: Context) =
        CastledPushWorkManager.getInstance(context).startPushBoostSync()

    suspend fun onTokenFetch(token: String?, tokenType: PushTokenType) {
        if (CastledSharedStore.getToken(tokenType) != token) {
            // New token
            logger.debug("Updating push token: $token, type: $tokenType")
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
        if (pushMessage.isCastledSilentPush) {
            return
        }
        val pushAlreadyDisplayed = runBlocking {
            withContext(Dispatchers.IO) {
                return@withContext CastledSharedStore.checkAndSetRecentDisplayedPushId(
                    context,
                    pushMessage.notificationId
                )
            }
        }
        if (pushAlreadyDisplayed) {
            // push already displayed
            return
        }
        pushMessageCache?.set(pushMessage.notificationId, pushMessage)
        PushNotificationManager.displayNotification(context, pushMessage)

    }

    fun isCastledPushMessage(remoteMessage: RemoteMessage): Boolean =
        PushNotificationManager.isCastledNotification(remoteMessage)

    suspend fun getPushMessages(): List<CastledPushMessage> {
        return pushRepository.getPushMessages()
    }

    override fun onStoreInitialized(context: Context) {
        initTokenProviders(context)
        refreshPushTokens(context)
        startPeriodicTokenRefreshTask(context)
        if (CastledSharedStore.configs.enablePushBoost) {
            startPushBoostSyncTask(context)
        }
    }

    override fun onStoreUserIdSet(context: Context) {
        externalScope.launch {
            registerUser(CastledSharedStore.getUserId()!!)
        }
    }

    suspend fun logoutUser(userId: String, sessionId: String?) {
        pushRepository.logoutUser(userId, getTokens(), sessionId)
    }

    private fun getTokens(): List<PushTokenInfo> {
        val tokens = mutableListOf<PushTokenInfo>()
        PushTokenType.values().forEach { tokenType ->
            CastledSharedStore.getToken(tokenType)?.let { tokenVal ->
                tokens.add(PushTokenInfo(tokenVal, tokenType))
            }
        }
        return tokens
    }

}