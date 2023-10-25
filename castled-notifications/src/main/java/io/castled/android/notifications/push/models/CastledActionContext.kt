package io.castled.android.notifications.push.models

data class CastledActionContext(
    val actionLabel: String?,
    val actionType: CastledClickAction?,
    val actionUri: String?,
    val keyVals: Map<String, String>?
)