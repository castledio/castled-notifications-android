package io.castled.inAppTriggerEvents.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonObject
import java.util.*

@Entity(tableName = "log_campaign")
internal data class LogCampaignModel(
    @ColumnInfo(name = "log_campaign_id")
    @PrimaryKey(autoGenerate = true)
    var logCampaignCount: Long,

    @ColumnInfo(name = "log_campaign_row")
    val jsonObject: JsonObject
)
//    @ColumnInfo(name = "log_events_time")
//    val logEventDate: Date = Calendar.getInstance().time
//TODO check are these for reporting events?