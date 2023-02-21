package io.castled.inAppTriggerEvents

class InAppChannelConfig internal constructor(
    internal var enable: Boolean,
    internal var fetchFromCloudInterval: Long
): ChannelConfig() {

    companion object {
        @JvmStatic
        fun builder(): InAppChannelConfigBuilder = InAppChannelConfigBuilder()

    }
}
