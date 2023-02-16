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

    val inApp = EventNotification.getInstance

    companion object {

        @JvmStatic
        var instance: CastledNotifications? = null

//        @JvmStatic
//        val instance = castledNotifications
//        val instance: CastledNotifications? =
//            if (this::castledNotifications.isInitialized) castledNotifications else null


        // TODO: close gitHub-> Inapp module initialization cleanups #5
        @JvmStatic
        fun initialize(application: Application, instanceId: String, configs: ChannelConfig): String {
            if (this.instance != null) {
                return "Already initialized."
            } else {

                val s = configs as InAppChannelConfig
                if (!s.enable)
                    return "Please enable InApp event."

                val eventNotification = EventNotification.getInstance
                eventNotification.triggerEventsFrequencyTime = configs.fetchFromCloudInterval
                eventNotification.instanceId = instanceId
                eventNotification.initialize(application)
                instance = CastledNotifications(configs)
                return "Initialized successfully."
            }
        }
    }
}

//TODO: close gitHub-> Fix current crashes #23
//TODO: close gitHub-> implement watching for network state changes and retrying network requests #15
//TODO: close gitHub-> implement exponential backoff #14
//TODO: close gitHub-> store reporting events in db until they are sent successfully #12
//TODO: close gitHub-> Do e2e testing #9
