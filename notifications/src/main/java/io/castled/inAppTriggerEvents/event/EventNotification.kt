package io.castled.inAppTriggerEvents.event

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.castled.inAppTriggerEvents.trigger.TriggerEvent
import io.castled.inAppTriggerEvents.observer.AppActivityLifecycleObserver
import io.castled.inAppTriggerEvents.observer.AppLifecycleObserver
import io.castled.inAppTriggerEvents.observer.FragmentLifeCycleObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "EventNotification"

class EventNotification private constructor() {
    internal lateinit var instanceId: String
    internal var triggerEventsFrequencyTime: Long = 60000
    set(timeInSeconds) {
        Log.d(TAG, "Event Frequency(Default: $triggerEventsFrequencyTime milliseconds) set to $timeInSeconds seconds.")
        field = TimeUnit.SECONDS.toMillis(timeInSeconds)
    }

    companion object {
        private lateinit var eventNotification: EventNotification

        @JvmStatic
        internal val getInstance: EventNotification =
            if (this::eventNotification.isInitialized) eventNotification else EventNotification()
    }

//    fun initialize(context: Context){
//        TriggerEvent.getInstance().fetchAndSaveTriggerEvents(context)
//    }

    internal fun initialize(application: Application) {
        GlobalScope.launch {
            do {
                TriggerEvent.getInstance().fetchAndSaveTriggerEvents(application)
//                Log.d(TAG, "$triggerEventsFrequencyTime: Start fetching events from cloud")
                delay(triggerEventsFrequencyTime)
            } while (true)
        }
    }

    fun observeLifecycle(context: Context, lifecycle: Lifecycle, screenName: String) {
        lifecycle.addObserver(AppLifecycleObserver(context, screenName))
    }

    fun observeLifecycle(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
        }
    }

    private fun observeLifecycle(fragment: Fragment, screenName: String) {
        fragment.viewLifecycleOwner.lifecycle.addObserver(
            FragmentLifeCycleObserver(
                fragment.requireContext(),
                screenName
            )
        )
    }

    private fun logCustomEventBySdk(context: Context, eventName: String, eventParams: Map<String, Any>) {
        val e = mutableMapOf<String, Any>()
        e["event"] = eventName
        e["params"] = eventParams
        TriggerEvent.getInstance().findAndLaunchEvent(context, e) { events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    private fun logCustomEvent1(context: Context, eventName: String) {
        val eventParams = mutableMapOf<String, Any?>()
        eventParams["event"] = eventName
        eventParams["params"] = null
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventParams) { events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    private fun logCustomEvent1(context: Context, eventName: String, eventParams: Map<String, Any>) {
        val eventParamsLocal = mutableMapOf<String, Any?>()
        eventParamsLocal["event"] = eventName
        eventParamsLocal["params"] = eventParams
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventParamsLocal) { events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    private fun logAppOpenEvent(context: Context) {
        val eventParams = mutableMapOf<String, Any?>()
        eventParams["event"] = "app_opened"
        eventParams["params"] = null
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventParams) { events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    private fun logPageViewedEvent(context: Context, pageName: String) {
        val eventParams = mutableMapOf<String, Any?>()
        eventParams["event"] = "page_viewed"

        val params = mutableMapOf<String, Any>()
        eventParams["name"] = pageName

        eventParams["params"] = params
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventParams) { events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    //TODO: close gitHub-> Trigger evaluator implementation #2

    // AppLogPageViewEvent with overloaded methods
    fun logInAppPageViewEvent(appCompatActivity: AppCompatActivity, screenName:String): Unit =
        logPageViewedEvent(appCompatActivity, screenName)

    fun logInAppPageViewEvent(activity: Activity, screenName:String): Unit =
        logPageViewedEvent(activity, screenName)

    fun logInAppPageViewEvent(context: Context, screenName:String): Unit =
        logPageViewedEvent(context, screenName)


    // LogAppOpenedEvent with overloaded methods
    fun logAppOpenedEvent(appCompatActivity: AppCompatActivity): Unit =
        logAppOpenEvent(appCompatActivity)

    fun logAppOpenedEvent(activity: Activity): Unit =
        logAppOpenEvent(activity)

    fun logAppOpenedEvent(context: Context): Unit =
        logAppOpenEvent(context)


    // CustomEvent tracked by SDk with overloaded methods
    fun logCustomEvent(appCompatActivity: AppCompatActivity, eventName: String, eventParams: Map<String, Any>): Unit =
        logCustomEventBySdk(appCompatActivity, eventName, eventParams)

    fun logCustomEvent(activity: Activity, eventName: String, eventParams: Map<String, Any>): Unit =
        logCustomEventBySdk(activity, eventName, eventParams)

    fun logCustomEvent(context: Context, eventName: String, eventParams: Map<String, Any>): Unit =
        logCustomEventBySdk(context, eventName, eventParams)


    // LogEvent only by event name with  overloaded methods
    fun logEvent(appCompatActivity: AppCompatActivity, eventName:String) =
        logCustomEvent1(appCompatActivity, eventName)

    fun logEvent(activity: Activity, eventName:String) =
        logCustomEvent1(activity, eventName)

    fun logEvent(context: Context, eventName:String) =
        logCustomEvent1(context, eventName)


    // LogEvent with Event name and with Event param with  overloaded methods
    fun logEvent(appCompatActivity: AppCompatActivity, eventName:String, eventParams: Map<String, Any>) =
        logCustomEvent1(appCompatActivity, eventName, eventParams)

    fun logEvent(activity: Activity, eventName:String, eventParams: Map<String, Any>) =
        logCustomEvent1(activity, eventName, eventParams)

    fun logEvent(context: Context, eventName:String, eventParams: Map<String, Any>) =
        logCustomEvent1(context, eventName, eventParams)
}