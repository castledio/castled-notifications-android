package io.castled.inappNotifications.notificationConsts

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.JsonObject
import com.google.gson.JsonParser

//This class will used by ROOM to convert appropriate data type to save it into database.
class NotificationTypeConverter {

    @TypeConverter
    fun fromJsonObject(source: JsonObject?): String {

        return source?.toString() ?: return ""
    }

    @TypeConverter
    fun toJsonObject(source: String): JsonObject {
        return JsonParser().parse(source).asJsonObject
    }
}