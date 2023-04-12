package io.castled.notifications

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import io.castled.notifications.commons.CastledRetrofitClient
import io.castled.notifications.inapp.InAppChannelConfig
import io.castled.notifications.inapp.InAppNotification
import io.castled.notifications.inapp.models.consts.AppEvents
import io.castled.notifications.push.PushNotification
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.store.CastledSharedStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object CastledNotifications {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.GENERIC)
    private lateinit var apiKey: String
    private val castledCoroutineContext by lazy { Job() }
    private val castledScope = CoroutineScope(castledCoroutineContext)

    @JvmStatic
    fun initialize(
        application: Application,
        apiKey: String,
        configs: List<io.castled.notifications.inapp.ChannelConfig>,
    ) {
        if (this::apiKey.isInitialized) {
            logger.error("Sdk already initialized!")
            return
        }
        if (apiKey.isBlank()) {
            logger.error("Api key is not set!")
            return
        }
        CastledRetrofitClient.init()
        CastledSharedStore.init(application, apiKey)

        PushNotification.init(castledScope)
        InAppNotification.init(application, castledScope, getInAppConfig(configs))
        refreshFcmToken(application)

        this.apiKey = apiKey
        logger.info("Sdk initialized successfully")
    }

    private fun getInAppConfig(configs: List<io.castled.notifications.inapp.ChannelConfig>): InAppChannelConfig {
        return configs.find { it.type == io.castled.notifications.inapp.ChannelType.IN_APP } as? InAppChannelConfig
            ?: InAppChannelConfig(false, 0)
    }

    @JvmStatic
    fun setUserId(
        context: Context, userId: String?, onSuccess: () -> Unit, onError: (Exception) -> Unit
    ) = this.castledScope.launch(Dispatchers.Default) {
        try {
            setUserId(context, userId)
            onSuccess()
        } catch (e: Exception) {
            onError(e)
        }
    }

    private fun refreshFcmToken(context: Context) = castledScope.launch(Dispatchers.Default) {
        val fcmToken = fetchFcmToken(context)
        onFcmTokenFetch(fcmToken)
    }

    private suspend fun setUserId(context: Context, userId: String?) {
        if (!isInited()) {
            throw IllegalStateException("Sdk not yet initialized!")

        } else if (userId.isNullOrBlank()) {
            throw IllegalStateException("UserId is empty!")

        } else {
            if (CastledSharedStore.userId != userId) {
                // New user-id
                CastledSharedStore.fcmToken?.let { PushNotification.registerUser(userId, it) }
                CastledSharedStore.userId = userId
            }
            InAppNotification.startCampaignJob()
        }
    }

    @JvmStatic
    fun onTokenFetch(token: String?) = castledScope.launch(Dispatchers.Default) {
        if (!isInited()) {
            logger.debug("Sdk not yet initialized!")
            return@launch
        }
        onFcmTokenFetch(token)
    }

    private suspend fun onFcmTokenFetch(token: String?) {
        if (CastledSharedStore.fcmToken != token) {
            // New token
            CastledSharedStore.userId?.let { PushNotification.registerUser(it, token) }
            CastledSharedStore.fcmToken = token
        }
    }

    private suspend fun fetchFcmToken(context: Context) = suspendCoroutine { continuation ->
        if (FirebaseApp.getApps(context).isEmpty()) {
            logger.debug("fcm token fetch failed! Please make sure Firebase is initialized")
            continuation.resume(null)
            return@suspendCoroutine
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                task.exception?.let { logger.error("fcm token fetch failed!", it) }
                continuation.resume(null)
            }
        }
    }

    @JvmStatic
    fun logAppPageViewEvent(context: Context, screenName: String) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited()) {
                InAppNotification.logAppEvent(
                    context,
                    AppEvents.APP_PAGE_VIEWED,
                    mapOf("name" to screenName)
                )
            }
        }

    @JvmStatic
    fun logAppOpenedEvent(context: Context) = castledScope.launch(Dispatchers.Default) {
        if (isInited()) {
            InAppNotification.logAppEvent(context, AppEvents.APP_OPENED, null)
        }
    }

    @JvmStatic
    fun logCustomAppEvent(context: Context, eventName: String, eventParams: Map<String, Any>?) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited()) {
                InAppNotification.logAppEvent(context, eventName, eventParams)
            }
        }

    private fun isInited(): Boolean = this::apiKey.isInitialized

}