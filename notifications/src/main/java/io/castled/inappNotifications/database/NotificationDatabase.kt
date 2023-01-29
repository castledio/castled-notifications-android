package io.castled.inappNotifications.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.castled.inappNotifications.database.dao.NotificationDao
import io.castled.inappNotifications.models.NotificationModel
import io.castled.inappNotifications.notificationConsts.NotificationTypeConverter

@Database(
    entities = [NotificationModel::class],
    exportSchema = true,
    version = 1
)
@TypeConverters(NotificationTypeConverter::class)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

}