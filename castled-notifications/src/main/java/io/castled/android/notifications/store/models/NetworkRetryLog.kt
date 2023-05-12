package io.castled.android.notifications.store.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.castled.android.notifications.workmanager.models.CastledNetworkRequest
import java.util.*

@Entity(tableName = "network_retry_log")
internal data class NetworkRetryLog(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    var createdAt: Date = Date(),

    @ColumnInfo(name = "request")
    val request: CastledNetworkRequest
)