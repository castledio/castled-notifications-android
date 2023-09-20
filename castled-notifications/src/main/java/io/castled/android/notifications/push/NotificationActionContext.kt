package io.castled.android.notifications.push

import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationActionContext(
    val notificationId: Int,
    val teamId: Long,
    val sourceContext: String,
    val eventType: String,
    val actionLabel: String?,
    val actionType: String?,
    val actionUri: String?,
    val keyVals: Map<String, String>?
)