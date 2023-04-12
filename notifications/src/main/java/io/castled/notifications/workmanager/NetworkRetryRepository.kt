package io.castled.notifications.workmanager

import io.castled.notifications.store.dao.NetworkRetryLogDao
import io.castled.notifications.store.models.NetworkRetryLog

internal class NetworkRetryRepository(private val networkRetryLogDao: NetworkRetryLogDao) {

    suspend fun getRetryLogs(): List<NetworkRetryLog> = networkRetryLogDao.getAllRetryLogs()

    suspend fun putRetryLog(log: NetworkRetryLog) = networkRetryLogDao.insertRetryLog(log)
}