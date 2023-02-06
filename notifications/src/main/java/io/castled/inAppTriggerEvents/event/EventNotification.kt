package io.castled.inAppTriggerEvents.event

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.Lifecycle
import io.castled.inAppTriggerEvents.models.TriggerEventModel
import io.castled.inAppTriggerEvents.trigger.TriggerEvent
import io.castled.inAppTriggerEvents.observer.AppActivityLifecycleObserver
import io.castled.inAppTriggerEvents.observer.AppLifecycleObserver
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