package io.castled.android.notifications.workmanager.models

@kotlinx.serialization.Serializable
internal data class CastledInboxEvent(
    val teamId: String,
    val eventType: String,
    val btnLabel: String? = null,
    val sourceContext: String,
    val tz: String,
    val ts: Long
)
