package io.castled.android.notifications.workmanager.models

@kotlinx.serialization.Serializable
internal data class CastledInAppEvent(
    val teamId: String,
    val sourceContext: String,
    val btnLabel: String? = null,
    val actionType: String? = null,
    val actionUri: String? = null,
    val eventType: String,
    val tz: String,
    val ts: Long
)
