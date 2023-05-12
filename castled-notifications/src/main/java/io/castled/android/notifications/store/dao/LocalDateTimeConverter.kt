package io.castled.android.notifications.store.dao

import androidx.room.TypeConverter
import java.util.*

internal object DateTimeConverter {

    @TypeConverter
    fun toDateTime(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun fromDateTime(date: Date): Long {
        return date.time
    }
}