package io.castled.inAppTriggerEvents

import android.content.Context


class CastledNotifications private constructor(){

    companion object {

        private lateinit var castledNotifications: CastledNotifications

        @JvmStatic
        fun initialize(context: Context, instanceId: String, configs: List<ChannelConfig>): CastledNotifications =
            if (this::castledNotifications.isInitialized) castledNotifications else CastledNotifications()

    }
}