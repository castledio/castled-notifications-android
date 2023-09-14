package io.castled.android.notifications

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.inapp.InAppNotification
import io.castled.android.notifications.inapp.models.consts.AppEvents
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.PushNotification
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.trackevents.TrackEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object CastledNotifications {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.GENERIC)
    private lateinit var apiKey: String
    private val castledCoroutineContext by lazy { Job() }
    private val castledScope = CoroutineScope(castledCoroutineContext)

    @JvmStatic
    fun initialize(application: Application, configs: CastledConfigs) {
        if (!isMainProcess(application)) {
            // In case there are services that are not run from main process, skip init
            // for such processes
            logger.verbose("Not main process...!")
            return
        }
        if (this::apiKey.isInitialized) {
            logger.error("Sdk already initialized!")
            return
        }
        if (configs.apiKey.isBlank()) {
            logger.error("Api key is not set!")
            return
        }

        CastledSharedStore.init(application, configs)
        CastledRetrofitClient.init(configs)

        if (configs.enablePush) {
            PushNotification.init(application, castledScope)
        }
        if (configs.enableInApp) {
            InAppNotification.init(application, castledScope)
        }
        if (configs.enableTracking) {
            TrackEvents.init(application)
        }
        apiKey = configs.apiKey
        logger.info("Sdk initialized successfully")
    }

    private fun isMainProcess(context: Context): Boolean {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfoList = activityManager.runningAppProcesses ?: return false

        for (processInfo in processInfoList) {
            if (processInfo.pid == pid && context.packageName == processInfo.processName) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun setUserId(
        context: Context,
        userId: String?,
        userToken: String?,
        onSuccess: () -> Unit = { },
        onError: (Exception) -> Unit = { }
    ) = castledScope.launch(Dispatchers.Default) {
        try {
            setUserId(context, userId,userToken)
            onSuccess()
        } catch (e: Exception) {
            onError(e)
        }
    }

    private suspend fun setUserId(context: Context, userId: String?,userToken: String?) {
        if (!isMainProcess(context)) {
            // In case there are services that are not run from main process, skip init
            // for such processes
            logger.verbose("skipping user-id set. Not main process!")
            return
        }
        if (!isInited()) {
            throw IllegalStateException("Sdk not yet initialized!")

        } else if (userId.isNullOrBlank()) {
            throw IllegalStateException("UserId is empty!")

        } else {
            if (CastledSharedStore.getUserId() != userId)
            {
                // New user-id
                PushNotification.registerUser(userId)
                CastledSharedStore.setUserId(userId,userToken)
            }
            InAppNotification.startCampaignJob()
        }
    }

    @JvmStatic
    fun onTokenFetch(token: String?, pushTokenType: PushTokenType) =
        castledScope.launch(Dispatchers.Default) {
            if (!isInited()) {
                logger.debug("Sdk not yet initialized!")
                return@launch
            } else if (!CastledSharedStore.configs.enablePush) {
                logger.debug("Push not enabled!")
                return@launch
            }
            PushNotification.onTokenFetch(token, pushTokenType)
        }

    @JvmStatic
    fun logAppPageViewEvent(context: Context, screenName: String) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited()) {
                InAppNotification.logAppEvent(
                    context, AppEvents.APP_PAGE_VIEWED, mapOf("name" to screenName)
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
                TrackEvents.reportEventWith(context, eventName, eventParams)
            }
        }

    fun handlePushNotification(context: Context, pushMessage: CastledPushMessage?) =
        castledScope.launch(Dispatchers.Default) {
            PushNotification.handlePushNotification(context, pushMessage)
        }

    fun isCastledPushMessage(remoteMessage: RemoteMessage): Boolean {
        return PushNotification.isCastledPushMessage(remoteMessage)
    }

    fun getCastledConfigs() = CastledSharedStore.configs

    private fun isInited(): Boolean = this::apiKey.isInitialized

}