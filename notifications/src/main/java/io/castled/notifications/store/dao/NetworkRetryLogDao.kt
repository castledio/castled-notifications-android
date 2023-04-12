package io.castled.notifications.store.dao

import androidx.room.*
import io.castled.notifications.store.models.NetworkRetryLog

@Dao
internal interface NetworkRetryLogDao {

    @Query("SELECT * FROM network_retry_log")
    suspend fun getAllRetryLogs(): List<NetworkRetryLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRetryLog(retryLogs: List<NetworkRetryLog>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRetryLog(retryLog: NetworkRetryLog): Long

    @Delete
    suspend fun deleteRetryLog(retryLog: NetworkRetryLog)
}