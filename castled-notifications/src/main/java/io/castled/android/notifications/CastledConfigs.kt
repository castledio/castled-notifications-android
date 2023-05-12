package io.castled.android.notifications

data class CastledConfigs(
    val location: CastledLocation,
    val enablePush: Boolean,
    val enableInApp: Boolean,
    val inAppFetchIntervalSec: Long,
    val xiaomiAppId: String?,
    val xiaomiAppKey: String?
) {
    class Builder {
        private var enablePush: Boolean = false
        private var enableInApp: Boolean = false
        private var inAppFetchIntervalSec = 3600L
        private var castledLocation = CastledLocation.US
        private var xiaomiAppId: String? = null
        private var xiaomiAppKey: String? = null

        fun enablePush(enablePush: Boolean) = apply { this.enablePush = enablePush }
        fun enableInApp(enableInApp: Boolean) = apply { this.enableInApp = enableInApp }
        fun inAppFetchIntervalSec(inAppFetchIntervalSec: Long) =
            apply { this.inAppFetchIntervalSec = inAppFetchIntervalSec }

        fun location(castledLocation: CastledLocation) = apply { this.castledLocation = castledLocation }
        fun xiaomiAppId(xiaomiAppId: String?) = apply { this.xiaomiAppId = xiaomiAppId }
        fun xiaomiAppKey(xiaomiAppKey: String?) = apply { this.xiaomiAppKey = xiaomiAppKey }
        fun build() = CastledConfigs(
            castledLocation,
            enablePush,
            enableInApp,
            inAppFetchIntervalSec,
            xiaomiAppId,
            xiaomiAppKey
        )
    }

    enum class CastledLocation {
        US, EU, INDIA, AP, TEST
    }
}
