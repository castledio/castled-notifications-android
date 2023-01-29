package io.castled.inappNotifications.database

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {

    private var INSTANCE: NotificationDatabase? = null

    fun getInstance(context: Context): NotificationDatabase {
        if (INSTANCE == null) {
            synchronized(NotificationDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            NotificationDatabase::class.java,
            "notification_db"
        ).build()

}