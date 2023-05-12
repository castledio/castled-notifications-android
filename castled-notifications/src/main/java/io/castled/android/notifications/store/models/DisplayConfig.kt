package io.castled.android.notifications.store.models

import kotlinx.serialization.Serializable

@Serializable
data class DisplayConfig(
    val displayLimit: Long,
    val minIntervalBtwDisplays: Long,
    val minIntervalBtwDisplaysGlobal: Long,
    val autoDismissInterval: Long
)
