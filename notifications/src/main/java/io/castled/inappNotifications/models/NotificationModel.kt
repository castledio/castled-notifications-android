package io.castled.inappNotifications.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import io.castled.inappNotifications.notificationConsts.NotificationTypeConverter

@Entity(tableName = "notification")
data class NotificationModel(
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
