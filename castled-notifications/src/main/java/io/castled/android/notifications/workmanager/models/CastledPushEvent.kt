package io.castled.android.notifications.workmanager.models

import kotlinx.serialization.Serializable

@Serializable
internal data class CastledPushEvent(
    val sourceContext: String,
    val actionLabel: String?,
    val actionType: String?,
    val actionUri: String?,
    val eventType: String,
    val tz: String,
    val ts: Long?
)
