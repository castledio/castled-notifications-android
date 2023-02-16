package io.castled.inAppTriggerEvents

import android.os.Bundle
import io.castled.inAppTriggerEvents.event.EventNotification

/*class InAppChannelConfig private constructor(
    var enable: Boolean,
    var fetchFromCloudInterval: Int) {

    companion object {
        private lateinit var inAppChannelConfig: InAppChannelConfig


        @JvmStatic
        fun builder(): InAppChannelConfig = if (this::inAppChannelConfig.isInitialized) inAppChannelConfig else InAppChannelConfig()

    }

    fun enable(enabled: Boolean): InAppChannelConfig{
        this.enable = enabled
        return this
    }

    fun fetchFromCloudInterval(timeInSeconds: Int): InAppChannelConfig{
        this.fetchFromCloudInterval = timeInSeconds
        return this
    }

    fun build(): InAppChannelConfig = this


}*/





class InAppChannelConfig internal constructor(
    internal var enable: Boolean,
    internal var fetchFromCloudInterval: Long
): ChannelConfig() {

    companion object {
        @JvmStatic
        fun builder(): InAppChannelConfigBuilder = InAppChannelConfigBuilder()

    }

    /*data class Builder internal constructor(
        private var enabled: Boolean = false,
                private var fetchFromCloudInterval: Int = 0
    ){

        fun enable(enabled: Boolean): Builder{
            this.enabled = enabled

            return this
        }

        fun fetchFromCloudInterval(timeInSeconds: Int): Builder{
            this.fetchFromCloudInterval = timeInSeconds

            return this
        }

        fun build(): InAppChannelConfig = InAppChannelConfig(enabled, fetchFromCloudInterval)

    }*/
}


//This is working

/*class InAppChannelConfig private constructor(): ChannelConfig() {
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
}*/

