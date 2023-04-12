package io.castled.notifications.store

import android.content.Context
import androidx.room.Room

internal object CastledDbBuilder {

    private var castledDb: CastledDb? = null

    @Synchronized
    fun getDbInstance(context: Context): CastledDb {
        if (castledDb == null) {
            castledDb = buildRoomDb(context)
        }
        return castledDb!!
    }

    private fun buildRoomDb(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            CastledDb::class.java,
            "castled_notifications"
        ).build()

}