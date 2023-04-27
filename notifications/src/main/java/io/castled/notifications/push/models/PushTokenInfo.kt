package io.castled.notifications.push.models

import kotlinx.serialization.Serializable

@Serializable
data class PushTokenInfo(val token: String, val tokenType: PushTokenType)
