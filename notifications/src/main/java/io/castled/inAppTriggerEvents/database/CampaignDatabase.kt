package io.castled.inAppTriggerEvents.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.castled.inAppTriggerEvents.database.dao.CampaignDao
import io.castled.inAppTriggerEvents.eventConsts.CampaignTypeConverter
import io.castled.inAppTriggerEvents.models.CampaignModel

@Database(
    entities = [CampaignModel::class],
    exportSchema = true,
    version = 1
)
@TypeConverters(CampaignTypeConverter::class)
internal abstract class CampaignDatabase : RoomDatabase() {
    abstract fun campaignDao(): CampaignDao

}