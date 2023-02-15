package io.castled.inAppTriggerEvents

import io.castled.inAppTriggerEvents.event.EventNotification

class InAppChannelConfig private constructor(): ChannelConfig() {
    private var enabled = false
    private var fetchFromCloudInterval = 0

    companion object {

        private lateinit var inAppChannelConfig: InAppChannelConfig

        @JvmStatic
        fun builder(): InAppChannelConfig =
            if (this::inAppChannelConfig.isInitialized) inAppChannelConfig else InAppChannelConfig()

    }

    fun enable(enabled: Boolean): InAppChannelConfig{
        this.enabled = enabled

        return this
    }

    fun fetchFromCloudInterval(timeInSeconds: Int): InAppChannelConfig{
        this.fetchFromCloudInterval = timeInSeconds

        return this
    }

    fun build(): InAppChannelConfig = this
}