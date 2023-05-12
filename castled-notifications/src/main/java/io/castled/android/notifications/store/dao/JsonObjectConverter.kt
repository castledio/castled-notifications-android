package io.castled.android.notifications.store.dao

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal object  JsonObjectConverter {

    @TypeConverter
    fun toJsonObject(value: String): JsonObject {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromJsonObject(json: JsonObject): String {
        return Json.encodeToString(json)
    }

}