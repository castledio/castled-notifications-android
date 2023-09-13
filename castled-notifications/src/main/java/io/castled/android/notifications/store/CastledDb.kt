package io.castled.android.notifications.store

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.castled.android.notifications.store.dao.*
import io.castled.android.notifications.store.dao.CampaignDao
import io.castled.android.notifications.store.dao.CampaignTypeConverter
import io.castled.android.notifications.store.dao.DateTimeConverter
import io.castled.android.notifications.store.dao.JsonObjectConverter
import io.castled.android.notifications.store.dao.NetworkRequestConverter
import io.castled.android.notifications.store.dao.*
import io.castled.android.notifications.store.dao.NetworkRetryLogDao
import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.store.models.Campaign
import io.castled.android.notifications.store.models.NetworkRetryLog

@Database(
    entities = [Campaign::class, NetworkRetryLog::class,AppInbox::class],
    exportSchema = true,
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(
    CampaignTypeConverter::class,
    NetworkRequestConverter::class,
    NumberTypeConverter::class,
    DateTimeConverter::class,
    DisplayConfigConverter::class,
    JsonObjectConverter::class
)
internal abstract class CastledDb : RoomDatabase() {

    abstract fun campaignDao(): CampaignDao
    abstract fun inboxDao(): InboxDao
    abstract fun networkRetryLogDao(): NetworkRetryLogDao
}