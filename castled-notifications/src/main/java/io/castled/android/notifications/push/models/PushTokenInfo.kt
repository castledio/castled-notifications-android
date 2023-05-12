package io.castled.android.notifications.push.models

import kotlinx.serialization.Serializable

@Serializable
data class PushTokenInfo(val token: String, val tokenType: PushTokenType)
