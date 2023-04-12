package io.castled.notifications.inapp

class InAppChannelConfig internal constructor(
    internal var enable: Boolean,
    internal var fetchFromCloudIntervalSec: Long
) : ChannelConfig() {

    override val type: ChannelType = ChannelType.IN_APP

    companion object {
        @JvmStatic
        fun builder(): InAppChannelConfigBuilder = InAppChannelConfigBuilder()

    }
}
