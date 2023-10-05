package io.castled.android.notifications

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.inapp.InAppNotification
import io.castled.android.notifications.inapp.models.consts.AppEvents
import io.castled.android.notifications.inbox.AppInboxHelper
import io.castled.android.notifications.inbox.model.CastledInboxConfig
import io.castled.android.notifications.inbox.model.CastledInboxItem
import io.castled.android.notifications.inbox.views.CastledInboxActivity
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.push.PushNotification
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.trackevents.TrackEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object CastledNotifications {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.GENERIC)
    private lateinit var appId: String
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
        if (this::appId.isInitialized) {
            logger.error("Sdk already initialized!")
            return
        }
        if (configs.appId.isBlank()) {
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
        if (configs.enableAppInbox) {
            AppInboxHelper.init(application, castledScope)
        }
        appId = configs.appId
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
        userId: String,
        userToken: String? = null
    ) = castledScope.launch(Dispatchers.Default) {
        try {
            saveUserId(context, userId, userToken)
        } catch (e: Exception) {
            logger.verbose("skipping userId set. $e")

        }
    }

    private suspend fun saveUserId(context: Context, userId: String, userToken: String?) {
        if (!isMainProcess(context)) {
            // In case there are services that are not run from main process, skip init
            // for such processes
            logger.verbose("skipping userId/userToken set. Not main process!")
            return
        }
        if (!isInited()) {
            throw IllegalStateException("Sdk not yet initialized!")

        } else if (userId.isBlank()) {
            throw IllegalStateException("UserId is empty!")

        } else {
            if (CastledSharedStore.getUserId() != userId) {
                // New user-id
                CastledSharedStore.setUserId(userId, userToken)
                PushNotification.registerUser(userId)
            }
            InAppNotification.startCampaignJob()
            AppInboxHelper.startInboxJob()


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
    fun logAppPageViewedEvent(context: Context, screenName: String) {
        if (isInited()) {
            InAppNotification.logAppEvent(
                context,
                AppEvents.APP_PAGE_VIEWED,
                mapOf("name" to screenName)
            )
        }
    }

    @JvmStatic
    fun logAppOpenedEvent(context: Context) {
        if (isInited()) {
            InAppNotification.logAppEvent(context, AppEvents.APP_OPENED, null)
        }
    }

    @JvmStatic
    fun logCustomAppEvent(context: Context, eventName: String, eventParams: Map<String, Any>?) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited()) {
                InAppNotification.logAppEvent(context, eventName, eventParams)
                TrackEvents.reportEventWith(eventName, eventParams)
            }
        }

    @JvmStatic
    fun setUserProfile(details: Map<String, Any>) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited()) {
                TrackEvents.reportUserTrackingEventWith(details)
            }
        }

    fun handlePushNotification(context: Context, pushMessage: CastledPushMessage?) =
        castledScope.launch(Dispatchers.Default) {
            PushNotification.handlePushNotification(context, pushMessage)
        }


    fun isCastledPushMessage(remoteMessage: RemoteMessage): Boolean {
        return PushNotification.isCastledPushMessage(remoteMessage)
    }

    @JvmStatic
    fun showAppInbox(context: Context, displayConfig: CastledInboxConfig? = null) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited()) {
                val intent = Intent(context, CastledInboxActivity::class.java)
                displayConfig?.let { intent.putExtra("displayConfig", displayConfig) }
                context.startActivity(intent)

            }
        }

    @JvmStatic
    fun getInboxItems(completion: (List<CastledInboxItem>) -> Unit) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited()) {
                completion(if (isInited()) AppInboxHelper.getInboxItems() else listOf())
            }
        }

    fun getCastledConfigs() = CastledSharedStore.configs

    private fun isInited(): Boolean = this::appId.isInitialized

}