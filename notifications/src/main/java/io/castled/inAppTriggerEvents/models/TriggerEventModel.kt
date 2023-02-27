package io.castled.inAppTriggerEvents.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

//TODO rename this to campaign
@Entity(tableName = "trigger_event")
internal data class TriggerEventModel(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    @ColumnInfo(name = "notification_id")
    @SerializedName("notificationId")
    val notificationId: Int,

    @ColumnInfo(name = "team_id")
    @SerializedName("teamId")
    val teamId: Long,

    @ColumnInfo(name = "source_context")
    @SerializedName("sourceContext")
    val sourceContext: String,

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
