package io.castled.android.notifications

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import io.castled.android.notifications.inapp.CastledInappNotificationListener
import io.castled.android.notifications.inapp.InAppNotification
import io.castled.android.notifications.inapp.models.consts.AppEvents
import io.castled.android.notifications.inbox.AppInbox
import io.castled.android.notifications.inbox.CastledInboxListener
import io.castled.android.notifications.inbox.model.CastledInboxDisplayConfig
import io.castled.android.notifications.inbox.model.CastledInboxItem
import io.castled.android.notifications.inbox.model.InboxConstants
import io.castled.android.notifications.inbox.model.InboxEventType
import io.castled.android.notifications.inbox.views.CastledInboxActivity
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.observer.CastledLifeCycleObserver
import io.castled.android.notifications.push.PushNotification
import io.castled.android.notifications.push.extensions.toCastledPushMessage
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.sessions.Sessions
import io.castled.android.notifications.store.CastledDbBuilder
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.tracking.device.DeviceInfoManager
import io.castled.android.notifications.tracking.events.EventsTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object CastledNotifications {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.GENERIC)
    private lateinit var appId: String
    private lateinit var application: Application

    private val castledCoroutineContext by lazy { Job() }
    private val castledScope = CoroutineScope(castledCoroutineContext)

    @JvmStatic
    @Synchronized
    fun initialize(application: Application, configs: CastledConfigs) {
        if (!isMainProcess(application)) {
            // In case there are services that are not run from main process, skip init
            // for such processes
            logger.verbose("Not main process...!")
            return
        }
        if (isInited()) {
            logger.debug("Sdk already initialized!")
            return
        }
        if (configs.appId.isBlank()) {
            logger.error("Api key is not set!")
            return
        }
        this.application = application
        CastledRetrofitClient.init(configs)

        if (configs.enablePush) {
            PushNotification.init(application, castledScope)
        }
        if (configs.enableInApp) {
            InAppNotification.init(application, castledScope)
        }
        if (configs.enableSessionTracking) {
            Sessions.init(application, castledScope)
        }
        if (configs.enableTracking) {
            EventsTracker.init(application, castledScope)
        }
        if (configs.enableAppInbox) {
            AppInbox.init(application, castledScope)
        }
        DeviceInfoManager.init(application, castledScope, configs.enablePush)
        CastledSharedStore.init(
            application,
            configs,
            castledScope
        )

        CastledLifeCycleObserver.start(application)

        appId = configs.appId
        logger.info("Sdk initialized successfully")
    }

    internal fun initializeInternal(application: Application) {
        if (isInited()) {
            return
        }
        val configs = CastledSharedStore.getCachedConfigs(application) ?: return
        initialize(application, configs)
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
            logger.error("skipping userId set. $e")
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
            CastledSharedStore.setUserId(context, userId, userToken)
        }
    }

    @JvmStatic
    fun logout() {
        CastledSharedStore.getUserId()?.let { userId ->
            castledScope.launch(Dispatchers.Default) {
                CastledDbBuilder.getDbInstance(application.applicationContext).clearAllTables()
                CastledSharedStore.clearSavedItems()
                cancelRunningJobs()
                if (getCastledConfigs().enablePush) {
                    PushNotification.logoutUser(
                        userId, Sessions.sessionId
                    )
                }
                logger.verbose("$userId has been logged out successfully.")
            }
        }
    }

    private suspend fun cancelRunningJobs() {
        if (getCastledConfigs().enableInApp) {
            InAppNotification.cancelCampaignJob()
        }
        if (getCastledConfigs().enableAppInbox) {
            AppInbox.cancelInboxJob()
        }
    }

    @JvmStatic
    fun onTokenFetch(token: String?, pushTokenType: PushTokenType) =
        castledScope.launch(Dispatchers.Default) {
            if (!isInited()) {
                logger.error("Sdk not yet initialized!")
                return@launch
            } else if (!CastledSharedStore.configs.enablePush) {
                logger.error("Push not enabled!")
                return@launch
            }
            PushNotification.onTokenFetch(token, pushTokenType)
        }

    @JvmStatic
    fun requestPushPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                logger.debug("PERMISSION_GRANTED")
            } else {

                // Directly ask for the permission
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    @JvmStatic
    fun logPageViewedEvent(context: Context, screenName: String) {
        if (!isInited()) {
            return
        }
        InAppNotification.logAppEvent(
            context,
            AppEvents.APP_PAGE_VIEWED,
            mapOf("name" to screenName)
        )
    }

    @JvmStatic
    fun logAppOpenedEvent(context: Context) {
        if (!isInited()) {
            return
        }
        InAppNotification.logAppEvent(context, AppEvents.APP_OPENED, null)
    }

    @JvmStatic
    fun logCustomAppEvent(
        context: Context,
        eventName: String,
        eventParams: Map<String, Any?>? = null
    ) {
        if (!isInited()) {
            return
        }
        if (getCastledConfigs().enableInApp) {
            InAppNotification.logAppEvent(context, eventName, eventParams)
        }
        if (getCastledConfigs().enableTracking) {
            EventsTracker.logCustomEvent(eventName, eventParams)
        }

    }

    @JvmStatic
    fun pauseInApp() {
        if (!isInited()) {
            return
        }
        InAppNotification.pauseInApp()
    }

    @JvmStatic
    fun stopInApp() {
        if (!isInited()) {
            return
        }
        InAppNotification.stopInApp()
    }

    @JvmStatic
    fun resumeInApp() {
        if (!isInited()) {
            return
        }
        InAppNotification.resumeInApp()
    }


    @JvmStatic
    fun setUserAttributes(context: Context, attributes: CastledUserAttributes) {
        if (!isInited()) {
            return
        }
        if (getCastledConfigs().enableTracking) {
            EventsTracker.logUserTrackingEvent(attributes)
        }
    }

    @JvmStatic
    fun handlePushNotification(context: Context, pushMessage: CastledPushMessage?) {
        // In React Native, SDK init happens outside of onCreate
        // So its possible SDK not inited when push message arrives
        initializeInternal(context.applicationContext as Application)
        if (!isInited()) {
            // Init
            return
        }
        PushNotification.handlePushNotification(context, pushMessage)
    }

    @JvmStatic
    fun handlePushNotification(context: Context, remoteMessage: RemoteMessage) {
        if (!isCastledPushMessage(remoteMessage)) {
            logger.error("Push message not from Castled!")
            return
        }
        if (PushNotification.isCastledSilentPushMessage(remoteMessage)) {
            logger.debug("Silent push message from Castled!")
            return
        }
        handlePushNotification(context, remoteMessage.toCastledPushMessage())
    }

    @JvmStatic
    fun isCastledPushMessage(remoteMessage: RemoteMessage): Boolean {
        return PushNotification.isCastledPushMessage(remoteMessage)
    }

    @JvmStatic
    fun subscribeToPushNotificationEvents(pushNotificationListener: CastledPushNotificationListener) {
        PushNotification.subscribeToPushNotificationEvents(pushNotificationListener)
    }

    @JvmStatic
    fun subscribeToInappEvents(inappListener: CastledInappNotificationListener) {
        InAppNotification.subscribeToInappEvents(inappListener)
    }

    @JvmStatic
    fun subscribeToInboxEvents(inboxListener: CastledInboxListener) {
        AppInbox.subscribeToInboxEvents(inboxListener)
    }

    @JvmStatic
    fun showAppInbox(context: Context, displayConfig: CastledInboxDisplayConfig? = null) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited() && getCastledConfigs().enableAppInbox) {
                val intent = Intent(context, CastledInboxActivity::class.java)
                displayConfig?.let {
                    intent.putExtra(
                        InboxConstants.CASTLED_DISPLAY_CONFIGS,
                        displayConfig
                    )
                }
                context.startActivity(intent)

            } else {
                logger.verbose("enableAppInbox while initializing the sdk")
            }
        }

    @JvmStatic
    fun getInboxItems(onCompletion: (List<CastledInboxItem>) -> Unit) =
        castledScope.launch(Dispatchers.Default) {
            onCompletion(
                if (isInited() && getCastledConfigs().enableAppInbox)
                    AppInbox.getInboxItems()
                else {
                    logger.debug("Castled inbox disabled/ UserId not configured")
                    listOf()
                }
            )
        }

    @JvmStatic
    fun logInboxItemClicked(inboxItem: CastledInboxItem, buttonTitle: String) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited() && getCastledConfigs().enableAppInbox) {
                AppInbox.reportEventWith(
                    inboxItem, buttonTitle, InboxEventType.CLICKED.toString(), null
                )
            } else {
                logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            }
        }

    @JvmStatic
    fun logInboxItemsRead(inboxItems: List<CastledInboxItem>) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited() && getCastledConfigs().enableAppInbox) {
                AppInbox.reportReadEventsWithItems(inboxItems)
            } else {
                logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            }
        }

    @JvmStatic
    fun deleteInboxItem(
        inboxItem: CastledInboxItem
    ) {
        if (isInited() && getCastledConfigs().enableAppInbox) {
            AppInbox.deleteInboxItem(inboxItem)
        } else {
            logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
        }
    }

    @JvmStatic
    fun getInboxUnreadCount(onCompletion: (Int) -> Unit) =
        castledScope.launch(Dispatchers.Default) {
            if (isInited() && getCastledConfigs().enableAppInbox) {
                onCompletion(
                    AppInbox.getInboxUnreadCount()
                )
            } else {
                logger.debug("Castled inbox disabled/ UserId not configured")
                onCompletion(
                    0
                )
            }
        }

    @JvmStatic
    fun startObserver(application: Application) {
        CastledLifeCycleObserver.start(application)
    }

    fun getCastledConfigs() = CastledSharedStore.configs

    internal fun isInited(): Boolean = this::appId.isInitialized

}