package io.castled.inAppTriggerEvents.database

import android.content.Context
import androidx.room.Room

internal object DatabaseBuilder {

    private var INSTANCE: TriggerEventsDatabase? = null

    fun getInstance(context: Context): TriggerEventsDatabase {
        if (INSTANCE == null) {
            synchronized(TriggerEventsDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            TriggerEventsDatabase::class.java,
            "trigger_event_db"
        ).build()

}