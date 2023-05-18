package io.castled.android.notifications

data class CastledConfigs(
    val apiKey: String,
    val location: CastledLocation,
    val enablePush: Boolean,
    val enableInApp: Boolean,
    val inAppFetchIntervalSec: Long,
    val xiaomiAppId: String?,
    val xiaomiAppKey: String?,
    val enableUserIdEncryption: Boolean
) {
    class Builder {
        private lateinit var apiKey: String
        private var enablePush: Boolean = false
        private var enableInApp: Boolean = false
        private var inAppFetchIntervalSec = 3600L
        private var castledLocation = CastledLocation.US
        private var xiaomiAppId: String? = null
        private var xiaomiAppKey: String? = null
        private var enableUserIdEncryption = false

        fun apiKey(apiKey: String) = apply { this.apiKey = apiKey }
        fun enablePush(enablePush: Boolean) = apply { this.enablePush = enablePush }
        fun enableInApp(enableInApp: Boolean) = apply { this.enableInApp = enableInApp }
        fun inAppFetchIntervalSec(inAppFetchIntervalSec: Long) =
            apply { this.inAppFetchIntervalSec = inAppFetchIntervalSec }

        fun location(castledLocation: CastledLocation) =
            apply { this.castledLocation = castledLocation }

        fun xiaomiAppId(xiaomiAppId: String?) = apply { this.xiaomiAppId = xiaomiAppId }
        fun xiaomiAppKey(xiaomiAppKey: String?) = apply { this.xiaomiAppKey = xiaomiAppKey }
        fun enableUserIdEncryption(enableUserIdEncryption: Boolean) =
            apply { this.enableUserIdEncryption = enableUserIdEncryption }

        fun build() = CastledConfigs(
            apiKey,
            castledLocation,
            enablePush,
            enableInApp,
            inAppFetchIntervalSec,
            xiaomiAppId,
            xiaomiAppKey,
            enableUserIdEncryption
        )
    }

    enum class CastledLocation {
        US,   // United States
        EU,   // Europe
        IN,   // India
        AP,    // Asia Pacific
        TEST   // Test
    }
}
