package io.castled.notifications.store.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonObject

@Entity(tableName = "campaigns")
internal data class Campaign(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int,

    @ColumnInfo(name = "notification_id")
    val notificationId: Int,

    @ColumnInfo(name = "team_id")
    val teamId: Long,

    @ColumnInfo(name = "source_context")
    val sourceContext: String,

    @ColumnInfo(name = "start_ts")
    val startTs: Long,

    @ColumnInfo(name = "end_ts")
    val endTs: Long,

    @ColumnInfo(name = "ttl")
    val ttl: Int,

    @ColumnInfo(name = "display_limit")
    val displayLimit: Long,

    @ColumnInfo(name = "times_displayed")
    var timesDisplayed: Long,

    @ColumnInfo(name = "min_interval_btw_displays")
    val minIntervalBtwDisplays: Long,

    @ColumnInfo(name = "last_displayed_time")
    var lastDisplayedTime: Long,

    @ColumnInfo(name = "min_interval_btw_displays_global")
    val minIntervalBtwDisplaysGlobal: Long,

    @ColumnInfo(name = "auto_dismiss_interval")
    val autoDismissInterval: Long,

    @ColumnInfo(name = "trigger")
    val trigger: JsonObject,

    @ColumnInfo(name = "message_json")
    val message: JsonObject
)
