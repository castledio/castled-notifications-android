package io.castled.android.notifications.store.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.castled.android.notifications.inbox.model.InboxMessageType
import kotlinx.serialization.json.JsonObject
import java.util.Date

@Entity(
    tableName = "inbox",
    indices = [Index(value = ["message_id"], unique = true)]
)
data class AppInbox(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "message_id")
    val messageId: Long,

    @ColumnInfo(name = "team_id")
    val teamId: Long,

    @ColumnInfo(name = "source_context")
    val sourceContext: String,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "body")
    val body: String,

    @ColumnInfo(name = "date_added")
    val dateAdded: Date,

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
    var isRead: Boolean,

    @ColumnInfo(name = "message_type")
    val messageType: InboxMessageType
)
