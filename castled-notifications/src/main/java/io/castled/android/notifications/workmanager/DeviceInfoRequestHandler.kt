package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.tracking.events.service.DeviceInfoRepository
import io.castled.android.notifications.workmanager.models.CastledDeviceInfoRequest

internal class DeviceInfoRequestHandler(appContext: Context) : NetworkRequestHandler {
    private val trackDeviceRepository by lazy { DeviceInfoRepository(appContext) }
    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        for (entry in requests) {
            try {
                val response =
                    trackDeviceRepository.reportDeviceInfoNoRetry((entry.request as CastledDeviceInfoRequest))
                if (!response.isSuccessful) {
                    onError(listOf(entry))
                } else {
                    onSuccess(listOf(entry))
                }
            } catch (e: Exception) {
                onError(listOf(entry))
            }
        }
    }
}