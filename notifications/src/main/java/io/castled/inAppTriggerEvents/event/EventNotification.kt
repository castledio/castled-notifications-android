package io.castled.inAppTriggerEvents.event

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.Lifecycle
import io.castled.inAppTriggerEvents.trigger.TriggerEvent
import io.castled.inAppTriggerEvents.observer.AppActivityLifecycleObserver
import io.castled.inAppTriggerEvents.observer.AppLifecycleObserver
import java.util.concurrent.TimeUnit

private const val TAG = "EventNotification"
class EventNotification private constructor() {
    companion object {

        private lateinit var eventNotification: EventNotification

        private var triggerEventsFrequencyTime: Long = 30L

        @JvmStatic
        fun getInstance(): EventNotification =
            if (this::eventNotification.isInitialized) eventNotification else EventNotification()

    }

    fun initialize(context: Context){
        TriggerEvent.getInstance().fetchAndSaveTriggerEvents(context)
    }

    fun observeLifecycle(context: Context, lifecycle: Lifecycle){
        lifecycle.addObserver(AppLifecycleObserver(context))
    }

    fun observeLifecycle(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.registerActivityLifecycleCallbacks(AppActivityLifecycleObserver())
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