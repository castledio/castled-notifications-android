package io.castled.android.notifications.push.models

import kotlinx.serialization.Serializable

@Serializable
data class CastledPushMessage(
    val notificationId: Int,
    val sourceContext: String,
    val teamId: Long,
    val title: String?,
    val body: String?,
    val summary: String?,
    val sound: String?,
    val priority: CastledPushPriority?,
    val channelId: String?,
    val channelName: String?,
    val channelDescription: String?,
    val smallIconResourceId: String?,
    val largeIconUri: String?,
    val pushMessageFrames: List<CastledPushMessageFrame>,
    val castledActionButtons: List<CastledActionButton>?,
    val ttl: Long?
)