package io.castled.inAppTriggerEvents.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

@Entity(tableName = "trigger_event")
internal data class TriggerEventModel(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int,

    @ColumnInfo(name = "notification_id")
    @SerializedName("notificationId")
    var notificationId: Int,

    @ColumnInfo(name = "trigger_count")
    val triggerCount: Int,

//    @ColumnInfo(name = "last_shown_ts")
//    val lastShownTimestamp: String,

    @ColumnInfo(name = "start_ts")
    @SerializedName("startTs")
    val startTs: Long,

    @ColumnInfo(name = "end_ts")
    @SerializedName("endTs")
    val endTs: Long,

    @ColumnInfo(name = "ttl")
    @SerializedName("ttl")
    val ttl: Int,

    @ColumnInfo(name = "trigger")
    @SerializedName("trigger")
    val trigger: JsonObject,

    @ColumnInfo(name = "message_json")
    @SerializedName("message")
    val message: JsonObject
)
