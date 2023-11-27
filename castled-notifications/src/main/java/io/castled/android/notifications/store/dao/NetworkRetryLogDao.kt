package io.castled.android.notifications.store.dao

import androidx.room.*
import io.castled.android.notifications.store.models.NetworkRetryLog

@Dao
internal interface NetworkRetryLogDao {

    @Query("SELECT * FROM network_retry_log order by id asc")
    suspend fun getAllRetryLogs(): List<NetworkRetryLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRetryLog(retryLogs: List<NetworkRetryLog>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRetryLog(retryLog: NetworkRetryLog): Long

    @Delete
    suspend fun deleteRetryLogs(retryLog: List<NetworkRetryLog>)
}