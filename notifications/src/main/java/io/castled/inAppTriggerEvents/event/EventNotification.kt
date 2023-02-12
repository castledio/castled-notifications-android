package io.castled.inAppTriggerEvents.event

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.castled.inAppTriggerEvents.models.TriggerEventModel
import io.castled.inAppTriggerEvents.trigger.TriggerEvent
import io.castled.inAppTriggerEvents.observer.AppActivityLifecycleObserver
import io.castled.inAppTriggerEvents.observer.AppLifecycleObserver
import io.castled.inAppTriggerEvents.observer.FragmentLifeCycleObserver
import io.castled.inAppTriggerEvents.observer.ScreenLifeCycleObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

private const val TAG = "EventNotification"
class EventNotification private constructor() {
    companion object {

        private lateinit var eventNotification: EventNotification

        private var triggerEventsFrequencyTime: Long = 60000

        @JvmStatic
        fun getInstance(): EventNotification =
            if (this::eventNotification.isInitialized) eventNotification else EventNotification()

    }

    fun initialize(context: Context){
        TriggerEvent.getInstance().fetchAndSaveTriggerEvents(context)
    }

    fun initialize(application: Application){
        GlobalScope.launch {
            do {
                TriggerEvent.getInstance().fetchAndSaveTriggerEvents(application)
                Log.d(TAG, "$triggerEventsFrequencyTime: Start fetching events from cloud")
                delay(triggerEventsFrequencyTime)
            } while (true)
        }
    }

    fun observeLifecycle(context: Context, lifecycle: Lifecycle, screenName: String){
        lifecycle.addObserver(AppLifecycleObserver(context, screenName))
    }

    fun observeLifecycle(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
        }
    }

    fun observeLifecycle(fragment: Fragment, screenName: String) {
        fragment.viewLifecycleOwner.lifecycle.addObserver(FragmentLifeCycleObserver(fragment.requireContext(), screenName))
    }

    fun logCustomEventBySdk(context: Context, eventName: String, eventParams: Map<String, Any>){
        val e = mutableMapOf<String, Any>()
        e["event"] = eventName
        e["params"] = eventParams
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventName, e){ events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    fun logCustomEvent(context: Context, eventName: String, eventParams: Map<String, Any>){
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventName, eventParams){ events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    fun logCustomEventForEventNameInEventParam(context: Context, eventParams: Map<String, Any>){
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventParams){ events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    fun logAppOpenEvent(context: Context){
        val eventParams = mutableMapOf<String, Any?>()
        eventParams["event"] = "app_opened"
        eventParams["params"] = null
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventParams){ events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    fun logPageViewedEvent(context: Context, pageName: String){
        val eventParams = mutableMapOf<String, Any?>()
        eventParams["event"] = "page_viewed"

        val params = mutableMapOf<String, Any>()
        eventParams["name"] = pageName

        eventParams["params"] = params
        TriggerEvent.getInstance().findAndLaunchEvent(context, eventParams){ events ->
            Log.d(TAG, "=>>(Size: ${events.size})")
        }
    }

    fun triggerEventsFetchFromCloudSetFrequencyInSeconds(timeInSeconds: Long){
        triggerEventsFrequencyTime = TimeUnit.SECONDS.toMillis(timeInSeconds)
        Log.d(TAG, "Event Frequency: $timeInSeconds seconds or $triggerEventsFrequencyTime milliseconds")
    }

    fun observeEventNotification(context: Context, lifecycle: Lifecycle) {

//        context.registerActivityLifecycleCallbacks()
    }
}