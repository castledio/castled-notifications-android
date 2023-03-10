package io.castled

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import io.castled.inAppTriggerEvents.ChannelConfig
import io.castled.inAppTriggerEvents.InAppChannelConfig
import io.castled.inAppTriggerEvents.event.EventNotification
import io.castled.inAppTriggerEvents.requests.connectivity.base.ConnectivityProvider
import io.castled.notifications.logger.CastledLogger

private const val TAG = "CastledNotifications"

class CastledNotifications private constructor(internal val configs: ChannelConfig) {

    companion object {
        private val inApp = EventNotification.getInstance
        internal val success1 = "CastledNotifications: SDK initialized successfully."
        internal val error1 = "CastledNotifications: SDK already initialized."
        internal val error2 = "CastledNotifications: Please enable InApp event."
        internal val error3 = "CastledNotifications: Please provide instanceId"
        internal val error4 = "CastledNotifications: SDK not yet initialized."
        internal val error5a = "CastledNotifications: UserId is empty."
        internal val error5b = "CastledNotifications: UserId is not yet provided to the SDK."


        @JvmStatic
        private var instance: CastledNotifications? = null

        @JvmStatic
        fun initialize(
            application: Application,
            instanceId: String,
            configs: ChannelConfig
        ) {
            if (this.instance != null) {
                CastledLogger.getInstance().error(error1)
                return
            }
            //The below two lines are commented because we are not checking userId at the time of initialization.
//            else if(inApp.userId == null) CastledLogger.getInstance().error(error5b)
//            else if(inApp.userId!!.isBlank()) CastledLogger.getInstance().error(error5a)
            if (instanceId.isBlank()) {
                CastledLogger.getInstance().error(error3)
                return
            }
            if (!(configs as InAppChannelConfig).enable) {
                CastledLogger.getInstance().error(error2)
                return
            }


            inApp.apply {
//                observeAppLifecycle(application)
//                connectivityProvider = ConnectivityProvider.createProvider(application)
                triggerEventsFrequencyTime = configs.fetchFromCloudInterval
                instanceIdKey = instanceId
                initialize(application)
            }
            instance = CastledNotifications(configs)
            CastledLogger.getInstance().info(success1)
        }

        @JvmStatic
        fun setUserId(userId: String) =
            if (userId.isBlank()) CastledLogger.getInstance().error(error5a)
            else if (instance == null)  CastledLogger.getInstance().error(error4)
            else {
                inApp.userId = userId
                inApp.checkAndStartJobToGetEvents()
            }


        /**
         *  AppLogPageViewEvent with overloaded methods internal
         */
        @JvmStatic
        fun logInAppPageViewEvent(
            appCompatActivity: AppCompatActivity,
            screenName: String
        ) {
            if (checkSdkInitialized("logInAppPageViewEvent"))
                inApp.logInAppPageViewEvent(appCompatActivity, screenName)
        }

        @JvmStatic
        fun logInAppPageViewEvent(activity: Activity, screenName: String) {
            if (checkSdkInitialized("logInAppPageViewEvent"))
                inApp.logInAppPageViewEvent(activity, screenName)
        }

        @JvmStatic
        fun logInAppPageViewEvent(context: Context, screenName: String) {
            if (checkSdkInitialized("logInAppPageViewEvent"))
                inApp.logInAppPageViewEvent(context, screenName)
        }


        /**
         *  LogAppOpenedEvent with overloaded methods
         */
        @JvmStatic
        fun logAppOpenedEvent(appCompatActivity: AppCompatActivity) {
            if (checkSdkInitialized("logAppOpenedEvent"))
                inApp.logAppOpenedEvent(appCompatActivity)
        }

        @JvmStatic
        fun logAppOpenedEvent(activity: Activity) {
            if (checkSdkInitialized("logAppOpenedEvent"))
                inApp.logAppOpenedEvent(activity)
        }

        @JvmStatic
        fun logAppOpenedEvent(context: Context) {
            if (checkSdkInitialized("logAppOpenedEvent"))
                inApp.logAppOpenedEvent(context)
        }


        /**
         * CustomEvent tracked by SDk with overloaded methods
         */
        @JvmStatic
        fun logCustomEvent(
            appCompatActivity: AppCompatActivity,
            eventName: String,
            eventParams: Map<String, Any>
        ) {
            if (checkSdkInitialized("logCustomEvent"))
                inApp.logCustomEvent(appCompatActivity, eventName, eventParams)
        }

        @JvmStatic
        fun logCustomEvent(
            activity: Activity,
            eventName: String,
            eventParams: Map<String, Any>
        ) {
            if (checkSdkInitialized("logCustomEvent"))
                inApp.logCustomEvent(activity, eventName, eventParams)
        }

        @JvmStatic
        fun logCustomEvent(
            context: Context,
            eventName: String,
            eventParams: Map<String, Any>
        ) {
            if (checkSdkInitialized("logCustomEvent"))
                inApp.logCustomEvent(context, eventName, eventParams)
        }


        /**
         * LogEvent only by event name with overloaded methods
         */
        @JvmStatic
        fun logEvent(appCompatActivity: AppCompatActivity, eventName: String) {
            if (checkSdkInitialized("logEvent"))
                inApp.logEvent(appCompatActivity, eventName)
        }

        @JvmStatic
        fun logEvent(activity: Activity, eventName: String) {
            if (checkSdkInitialized("logEvent"))
                inApp.logEvent(activity, eventName)
        }

        @JvmStatic
        fun logEvent(context: Context, eventName: String) {
            if (checkSdkInitialized("logEvent"))
                inApp.logEvent(context, eventName)
        }


        /**
         * LogEvent with Event name and with Event param with overloaded methods
         */
        @JvmStatic
        fun logEvent(
            appCompatActivity: AppCompatActivity,
            eventName: String,
            eventParams: Map<String, Any>
        ) {
            if (checkSdkInitialized("logEvent"))
                inApp.logEvent(appCompatActivity, eventName, eventParams)
        }

        @JvmStatic
        fun logEvent(activity: Activity, eventName: String, eventParams: Map<String, Any>) {
            if (checkSdkInitialized("logEvent"))
                inApp.logEvent(activity, eventName, eventParams)
        }

        @JvmStatic
        fun logEvent(context: Context, eventName: String, eventParams: Map<String, Any>) {
            if (checkSdkInitialized("logEvent"))
                inApp.logEvent(context, eventName, eventParams)
        }


        private fun checkSdkInitialized(message: String): Boolean =
            if (instance == null) {
                CastledLogger.getInstance().error("$error4 \t Method: $message")
                false
            }
            else if(inApp.userId == null) {
                CastledLogger.getInstance().error("$error5b \t Method: $message")
                false
            }
            else if(inApp.userId!!.isBlank()) {
                CastledLogger.getInstance().error("$error5a \t Method: $message")
                false
            }
            else true
    }
}

//TODO: close gitHub-> implement watching for network state changes and retrying network requests #15
//TODO: close gitHub-> implement exponential backoff #14
//TODO: close gitHub-> store reporting events in db until they are sent successfully #12