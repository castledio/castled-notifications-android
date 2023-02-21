package io.castled.inAppTriggerEvents

/**
 * https://www.baeldung.com/kotlin/builder-pattern
 * https://www.tutorialspoint.com/design_pattern/builder_pattern.htm
 */
data class InAppChannelConfigBuilder internal constructor(
    private var enabled: Boolean = false,
    private var fetchFromCloudInterval: Long = 0
){

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
