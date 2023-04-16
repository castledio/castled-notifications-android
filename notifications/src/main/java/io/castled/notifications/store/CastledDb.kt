package io.castled.notifications.store

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.castled.notifications.store.dao.*
import io.castled.notifications.store.dao.CampaignDao
import io.castled.notifications.store.dao.CampaignTypeConverter
import io.castled.notifications.store.dao.NetworkRequestConverter
import io.castled.notifications.store.dao.NetworkRetryLogDao
import io.castled.notifications.store.models.Campaign
import io.castled.notifications.store.models.NetworkRetryLog

@Database(
    entities = [Campaign::class, NetworkRetryLog::class],
    exportSchema = true,
    version = 1
)
@TypeConverters(
    CampaignTypeConverter::class,
    NetworkRequestConverter::class,
    DateTimeConverter::class
)
internal abstract class CastledDb : RoomDatabase() {

    abstract fun campaignDao(): CampaignDao
    abstract fun networkRetryLogDao(): NetworkRetryLogDao
}