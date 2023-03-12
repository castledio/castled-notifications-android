package io.castled.inAppTriggerEvents.database

import android.content.Context
import androidx.room.Room

internal object DatabaseBuilder {

    private var INSTANCE: CampaignDatabase? = null

    fun getInstance(context: Context): CampaignDatabase {
        if (INSTANCE == null) {
            synchronized(CampaignDatabase::class) {
                INSTANCE = buildRoomDB(context)
            }
        }
        return INSTANCE!!
    }

    private fun buildRoomDB(context: Context) =
        Room.databaseBuilder(
            context.applicationContext,
            CampaignDatabase::class.java,
            "campaign_db"
        ).build()

}