package io.castled.notifications.store.models

import kotlinx.serialization.Serializable

@Serializable
internal data class DisplayConfig(
    val displayLimit: Long,
    val minIntervalBtwDisplays: Long,
    val minIntervalBtwDisplaysGlobal: Long,
    val autoDismissInterval: Long
)
