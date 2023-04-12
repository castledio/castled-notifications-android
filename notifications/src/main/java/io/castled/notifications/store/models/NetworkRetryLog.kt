package io.castled.notifications.store.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.castled.notifications.workmanager.models.CastledNetworkRequest

@Entity(tableName = "network_retry_log")
internal data class NetworkRetryLog(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    var createdAt: Long? = null,

    @ColumnInfo(name = "channel_type")
    val channel: String,

    @ColumnInfo(name = "request")
    val request: CastledNetworkRequest
)