package io.castled.android.notifications

import kotlinx.serialization.Serializable

@Serializable
data class CastledConfigs(
    val appId: String,
    val location: CastledLocation,
    val enablePush: Boolean,
    val enablePushBoost: Boolean,
    val enableInApp: Boolean,
    val enableTracking: Boolean,
    val enableAppInbox: Boolean,
    val skipUrlHandling: Boolean = false,
    val enableSessionTracking: Boolean = true,
    val inAppFetchIntervalSec: Long,
    val inBoxFetchIntervalSec: Long,
    val sessionTimeOutSec: Long = 900L,
    val xiaomiAppId: String?,
    val xiaomiAppKey: String?,
    val xiaomiRegion: XiaomiRegion?
) {
    class Builder {
        private lateinit var appId: String
        private var enablePush: Boolean = false
        private var enablePushBoost: Boolean = false
        private var enableInApp: Boolean = false
        private var enableTracking: Boolean = false
        private var enableAppInbox: Boolean = false
        private var skipUrlHandling: Boolean = false
        private var enableSessionTracking: Boolean = true

        private var inAppFetchIntervalSec = 600L
        private var inBoxFetchIntervalSec = 600L
        private var sessionTimeOutSec = 900L

        private var castledLocation = CastledLocation.US
        private var xiaomiAppId: String? = null
        private var xiaomiAppKey: String? = null
        private var xiaomiRegion: XiaomiRegion? = null


        fun appId(appId: String) = apply {
            this.appId = appId
        }

        fun enablePush(enablePush: Boolean) = apply { this.enablePush = enablePush }
        fun enablePushBoost(enablePushBoost: Boolean) =
            apply { this.enablePushBoost = enablePushBoost }

        fun enableTracking(enableTracking: Boolean) = apply { this.enableTracking = enableTracking }
        fun enableInApp(enableInApp: Boolean) = apply { this.enableInApp = enableInApp }
        fun enableAppInbox(enableAppInbox: Boolean) = apply { this.enableAppInbox = enableAppInbox }
        fun skipUrlHandling(skipUrlHandling: Boolean) =
            apply { this.skipUrlHandling = skipUrlHandling }

        fun enableSessionTracking(enableSessionTracking: Boolean) =
            apply { this.enableSessionTracking = enableSessionTracking }

        fun inAppFetchIntervalSec(inAppFetchIntervalSec: Long) =
            apply { this.inAppFetchIntervalSec = inAppFetchIntervalSec }

        fun inBoxFetchIntervalSec(inBoxFetchIntervalSec: Long) =
            apply { this.inBoxFetchIntervalSec = inBoxFetchIntervalSec }

        fun sessionTimeOutSec(sessionTimeOutSec: Long) =
            apply { this.sessionTimeOutSec = sessionTimeOutSec }

        fun location(castledLocation: CastledLocation) =
            apply { this.castledLocation = castledLocation }

        fun xiaomiAppId(xiaomiAppId: String?) = apply { this.xiaomiAppId = xiaomiAppId }

        fun xiaomiAppKey(xiaomiAppKey: String?) = apply { this.xiaomiAppKey = xiaomiAppKey }

        fun xiaomiRegion(xiaomiRegion: XiaomiRegion?) = apply { this.xiaomiRegion = xiaomiRegion }

        fun build() = CastledConfigs(
            appId,
            castledLocation,
            enablePush,
            enablePushBoost,
            enableInApp,
            enableTracking,
            enableAppInbox,
            skipUrlHandling,
            enableSessionTracking,
            inAppFetchIntervalSec,
            inBoxFetchIntervalSec,
            sessionTimeOutSec,
            xiaomiAppId,
            xiaomiAppKey,
            xiaomiRegion
        )
    }

    enum class CastledLocation {
        US,   // United States
        EU,   // Europe
        IN,   // India
        AP,    // Asia Pacific
        TEST   // Test
    }

    enum class XiaomiRegion {
        Global,  // United States
        India,   // Europe
        Europe   // India
    }
}
