package io.castled.notifications.workmanager

import android.content.Context
import io.castled.notifications.store.CastledDbBuilder
import io.castled.notifications.store.models.NetworkRetryLog

internal class NetworkRetryRepository(val context: Context) {

    private val networkRetryLogDao = CastledDbBuilder.getDbInstance(context).networkRetryLogDao()

    suspend fun getRetryRequests(): List<NetworkRetryLog> = networkRetryLogDao.getAllRetryLogs()

    suspend fun insertRetryRequest(log: NetworkRetryLog) = networkRetryLogDao.insertRetryLog(log)

    suspend fun deleteRetryRequests(logs: List<NetworkRetryLog>) =
        networkRetryLogDao.deleteRetryLogs(logs)

}