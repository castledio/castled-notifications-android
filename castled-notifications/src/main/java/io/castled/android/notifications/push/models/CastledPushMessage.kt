package io.castled.android.notifications.push.models

import kotlinx.serialization.Serializable

@Serializable
data class CastledPushMessage(
    val notificationId: Int,
    val sourceContext: String,
    val title: String? = null,
    val body: String? = null,
    val summary: String? = null,
    val sound: String? = null,
    val priority: CastledPushPriority? = null,
    val channelId: String? = null,
    val channelName: String? = null,
    val channelDescription: String? = null,
    val smallIconResourceId: String? = null,
    val largeIconUri: String? = null,
    val pushMessageFrames: List<CastledPushMessageFrame>,
    val actionButtons: List<CastledActionButton>? = null,
    val ttl: Long? = null,
    val inboxCopyEnabled: Boolean? = false,
    val isCastledSilentPush: Boolean = false

)