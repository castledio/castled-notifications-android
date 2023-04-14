package io.castled.notifications

data class CastledConfigs(
    val enablePush: Boolean,
    val enableInApp: Boolean,
    val inAppFetchIntervalSec: Long
) {
    class Builder {
        private var enablePush: Boolean = false
        private var enableInApp: Boolean = false
        private var inAppFetchIntervalSec = 3600L

        fun enablePush(enablePush: Boolean) = apply { this.enablePush = enablePush }
        fun enableInApp(enableInApp: Boolean) = apply { this.enableInApp = enableInApp }
        fun inAppFetchIntervalSec(inAppFetchIntervalSec: Long) =
            apply { this.inAppFetchIntervalSec = inAppFetchIntervalSec }

        fun build() = CastledConfigs(enablePush, enableInApp, inAppFetchIntervalSec)
    }
}
