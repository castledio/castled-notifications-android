package io.castled.android.notifications.store.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.castled.android.notifications.inbox.model.InboxMessageType
import kotlinx.serialization.json.JsonObject

@Entity(
    tableName = "inbox",
    indices = [Index(value = ["messageId"], unique = true)]
)
data class AppInbox(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "messageId")
    val messageId: Long,

    @ColumnInfo(name = "team_id")
    val teamId: Long,

    @ColumnInfo(name = "source_context")
    val sourceContext: String,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String,

    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: Number,

    @ColumnInfo(name = "start_ts")
    val startTs: Long,

    @ColumnInfo(name = "expiryTs")
    val expiryTs: Long,

    @ColumnInfo(name = "trigger")
    val trigger: JsonObject,

    @ColumnInfo(name = "message_json")
    val message: JsonObject,

    @ColumnInfo(name = "is_read")
    val is_read: Boolean,

    @ColumnInfo(name = "messageType")
    val message_type: InboxMessageType
)
