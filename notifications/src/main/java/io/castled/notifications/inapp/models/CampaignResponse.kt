package io.castled.notifications.inapp.models

import io.castled.notifications.store.models.DisplayConfig
import io.castled.notifications.store.models.InAppPriority
import kotlinx.serialization.json.JsonObject

internal data class CampaignResponse(
    val notificationId: Int,
    val teamId: Long,
    val sourceContext: String,
    val startTs: Long,
    val endTs: Long,
    val ttl: Int,
    val displayConfig: DisplayConfig,
    val priority: InAppPriority,
    val trigger: JsonObject,
    val message: JsonObject
)
