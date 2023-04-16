package io.castled.notifications.workmanager

import io.castled.notifications.store.dao.NetworkRetryLogDao
import io.castled.notifications.store.models.NetworkRetryLog

internal class NetworkRetryRepository(private val networkRetryLogDao: NetworkRetryLogDao) {

    suspend fun getRetryRequests(): List<NetworkRetryLog> = networkRetryLogDao.getAllRetryLogs()

    suspend fun insertRetryRequest(log: NetworkRetryLog) = networkRetryLogDao.insertRetryLog(log)

    suspend fun deleteRetryRequests(logs: List<NetworkRetryLog>) =
        networkRetryLogDao.deleteRetryLogs(logs)

}