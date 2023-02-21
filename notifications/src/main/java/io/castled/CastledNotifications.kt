package io.castled

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import io.castled.inAppTriggerEvents.ChannelConfig
import io.castled.inAppTriggerEvents.InAppChannelConfig
import io.castled.inAppTriggerEvents.event.EventNotification

private const val TAG = "CastledNotifications"

class CastledNotifications private constructor(internal val configs: ChannelConfig) {

    companion object {
        private val inApp = EventNotification.getInstance
        private val success1 = "CastledNotifications: SDK initialized successfully."
        private val error1 = "CastledNotifications: SDK already initialized."
        private val error2 = "CastledNotifications: Please enable InApp event."
        private val error3 = "CastledNotifications: Please provide instanceId"
        private val error4 = "CastledNotifications: SDK not yet initialized."
        private val error5a = "CastledNotifications: UserId is empty."
        private val error5b = "CastledNotifications: UserId is not yet provided to the SDK."


        @JvmStatic
        private var instance: CastledNotifications? = null

//        @JvmStatic
//        val instance = castledNotifications
//        val instance: CastledNotifications? =
//            if (this::castledNotifications.isInitialized) castledNotifications else null


        // TODO: close gitHub-> Inapp module initialization cleanups #5
        @JvmStatic
        fun initialize(
            application: Application,
            instanceId: String,
            configs: ChannelConfig
        ): String {
            if (this.instance != null) return error1
            if (inApp.userId.isBlank()) return error5b
            if (instanceId.isBlank()) return error3
            if (!(configs as InAppChannelConfig).enable) return error2

            inApp.apply {
                triggerEventsFrequencyTime = configs.fetchFromCloudInterval
                instanceIdKey = instanceId
                initialize(application)
            }
            instance = CastledNotifications(configs)
            return success1
        }

        // TODO: close gitHub-> add userid in api [] use existing CastledNotification.setUserId(userId) in notifications/castlednotifications class #30
        @JvmStatic
        fun setUserId(userId: String): String =
            if (userId.isBlank()) error5a
            else {
                inApp.userId = userId
                ""
            }


        // AppLogPageViewEvent with overloaded methods internal
        @JvmStatic
        fun logInAppPageViewEvent(
            appCompatActivity: AppCompatActivity,
            screenName: String
        ): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logInAppPageViewEvent(appCompatActivity, screenName)
                ""
            }

        @JvmStatic
        fun logInAppPageViewEvent(activity: Activity, screenName: String): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logInAppPageViewEvent(activity, screenName)
                ""
            }

        @JvmStatic
        fun logInAppPageViewEvent(context: Context, screenName: String): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logInAppPageViewEvent(context, screenName)
                ""
            }


        // LogAppOpenedEvent with overloaded methods
        @JvmStatic
        fun logAppOpenedEvent(appCompatActivity: AppCompatActivity): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logAppOpenedEvent(appCompatActivity)
                ""
            }

        @JvmStatic
        fun logAppOpenedEvent(activity: Activity): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logAppOpenedEvent(activity)
                ""
            }

        @JvmStatic
        fun logAppOpenedEvent(context: Context): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logAppOpenedEvent(context)
                ""
            }


        // CustomEvent tracked by SDk with overloaded methods
        @JvmStatic
        fun logCustomEvent(
            appCompatActivity: AppCompatActivity,
            eventName: String,
            eventParams: Map<String, Any>
        ): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logCustomEvent(appCompatActivity, eventName, eventParams)
                ""
            }

        @JvmStatic
        fun logCustomEvent(
            activity: Activity,
            eventName: String,
            eventParams: Map<String, Any>
        ): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logCustomEvent(activity, eventName, eventParams)
                ""
            }

        @JvmStatic
        fun logCustomEvent(
            context: Context,
            eventName: String,
            eventParams: Map<String, Any>
        ): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logCustomEvent(context, eventName, eventParams)
                ""
            }


        // LogEvent only by event name with overloaded methods
        @JvmStatic
        fun logEvent(appCompatActivity: AppCompatActivity, eventName: String): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logEvent(appCompatActivity, eventName)
                ""
            }

        @JvmStatic
        fun logEvent(activity: Activity, eventName: String): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logEvent(activity, eventName)
                ""
            }

        @JvmStatic
        fun logEvent(context: Context, eventName: String): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logEvent(context, eventName)
                ""
            }


        // LogEvent with Event name and with Event param with  overloaded methods
        @JvmStatic
        fun logEvent(
            appCompatActivity: AppCompatActivity,
            eventName: String,
            eventParams: Map<String, Any>
        ): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logEvent(appCompatActivity, eventName, eventParams)
                ""
            }

        @JvmStatic
        fun logEvent(activity: Activity, eventName: String, eventParams: Map<String, Any>): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logEvent(activity, eventName, eventParams)
                ""
            }

        @JvmStatic
        fun logEvent(context: Context, eventName: String, eventParams: Map<String, Any>): String =
            if (instance == null) error4
            else if(inApp.userId.isBlank()) error5b
            else {
                inApp.logEvent(context, eventName, eventParams)
                ""
            }


    }
}

//TODO: close gitHub-> implement watching for network state changes and retrying network requests #15
//TODO: close gitHub-> implement exponential backoff #14
//TODO: close gitHub-> store reporting events in db until they are sent successfully #12
