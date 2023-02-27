package io.castled.inAppTriggerEvents.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonObject
import java.util.*

//TODO check are these for reporting events?
@Entity(tableName = "log_trigger_event")
class LogTriggerEventModel (
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    @ColumnInfo(name = "log_events")
    val jsonObject: JsonObject,

    @ColumnInfo(name = "log_events_time")
    val logEventDate: Date = Calendar.getInstance().time


)