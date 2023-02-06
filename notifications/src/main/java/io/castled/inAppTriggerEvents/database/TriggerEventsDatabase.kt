package io.castled.inAppTriggerEvents.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.castled.inAppTriggerEvents.database.dao.TriggerEventsDao
import io.castled.inAppTriggerEvents.eventConsts.TriggerEventTypeConverter
import io.castled.inAppTriggerEvents.models.TriggerEventModel

@Database(
    entities = [TriggerEventModel::class],
    exportSchema = true,
    version = 1
)
@TypeConverters(TriggerEventTypeConverter::class)
internal abstract class TriggerEventsDatabase : RoomDatabase() {
    abstract fun triggerEventsDao(): TriggerEventsDao

}