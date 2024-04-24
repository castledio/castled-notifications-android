package io.castled.android.notifications.tracking.device.service

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledDeviceInfoRequest
import retrofit2.Response

internal class DeviceInfoRepository(context: Context) {

    private val deviceInfoApi by lazy { CastledRetrofitClient.create(DeviceInfoApi::class.java) }
    private val networkWorkManager by lazy { CastledNetworkWorkManager.getInstance(context) }

    suspend fun reportDeviceInfo(request: CastledDeviceInfoRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry deviceInfoApi.reportDeviceInfo(
                (it as CastledDeviceInfoRequest)
            )
        })
    }

    suspend fun reportDeviceInfoNoRetry(request: CastledDeviceInfoRequest): Response<Void?> {
        return deviceInfoApi.reportDeviceInfo(
            request
        )
    }

}