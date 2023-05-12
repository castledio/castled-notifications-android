package io.castled.android.notifications.store.dao

import androidx.room.TypeConverter
import io.castled.android.notifications.store.models.DisplayConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal object DisplayConfigConverter {

    @TypeConverter
    fun toDisplayConfig(value: String): DisplayConfig {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromDisplayConfig(config: DisplayConfig): String {
        return Json.encodeToString(config)
    }
}