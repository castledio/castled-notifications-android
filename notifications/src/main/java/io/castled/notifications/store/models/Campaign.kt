package io.castled.notifications.store.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.json.JsonObject

@Entity(
    tableName = "campaigns",
    indices = [Index(value = ["notification_id"], unique = true)]
)
internal data class Campaign(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "notification_id")
    val notificationId: Int,

    @ColumnInfo(name = "team_id")
    val teamId: Long,

    @ColumnInfo(name = "source_context")
    val sourceContext: String,

    @ColumnInfo(name = "start_ts")
    val startTs: Long,

    @ColumnInfo(name = "ttl")
    val ttl: Long,

    @ColumnInfo(name = "end_ts")
    val endTs: Long,

    @ColumnInfo(name = "display_config")
    val displayConfig: DisplayConfig,

    @ColumnInfo(name = "times_displayed")
    var timesDisplayed: Long,

    @ColumnInfo(name = "last_displayed_time")
    var lastDisplayedTime: Long,

    @ColumnInfo(name = "trigger")
    val trigger: JsonObject,

    @ColumnInfo(name = "message_json")
    val message: JsonObject,

    @ColumnInfo(name = "priority")
    val priority: InAppPriority,

    @ColumnInfo(name = "expired")
    val expired: Boolean
)
