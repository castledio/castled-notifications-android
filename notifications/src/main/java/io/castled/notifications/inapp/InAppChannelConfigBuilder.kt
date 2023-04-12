package io.castled.notifications.inapp

class InAppChannelConfigBuilder internal constructor(
    private var enabled: Boolean = false,
    private var fetchFromCloudInterval: Long = 60000
) {

    fun enable(enabled: Boolean): InAppChannelConfigBuilder {
        this.enabled = enabled
        return this
    }

    fun fetchFromCloudInterval(timeInSeconds: Long): InAppChannelConfigBuilder {
        this.fetchFromCloudInterval = timeInSeconds
        return this
    }

    fun build(): InAppChannelConfig = InAppChannelConfig(enabled, fetchFromCloudInterval)

}
