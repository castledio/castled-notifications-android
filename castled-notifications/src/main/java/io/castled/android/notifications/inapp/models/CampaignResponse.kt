package io.castled.android.notifications.inapp.models

import io.castled.android.notifications.store.models.DisplayConfig
import io.castled.android.notifications.store.models.InAppPriority
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class CampaignResponse(
    val notificationId: Int,
    val teamId: Long,
    val sourceContext: String,
    val startTs: Long,
    val endTs: Long,
    val ttl: Int?,
    val displayConfig: DisplayConfig,
    val priority: InAppPriority,
    val trigger: JsonObject,
    val message: JsonObject
)
