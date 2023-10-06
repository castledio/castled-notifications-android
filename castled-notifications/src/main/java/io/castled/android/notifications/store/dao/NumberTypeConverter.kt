package io.castled.android.notifications.store.dao

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class NumberTypeConverter {

    @TypeConverter
    fun fromNumber(number: Number?): Double? {
        return number?.toDouble()
    }

    @TypeConverter
    fun toNumber(value: Double?): Number? {
        return value?.let { it }
    }
}