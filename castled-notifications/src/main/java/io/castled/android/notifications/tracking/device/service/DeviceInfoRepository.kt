package io.castled.android.notifications.tracking.events.service

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledDeviceInfoRequest
import retrofit2.Response

internal class DeviceInfoRepository(context: Context) {

    private val deviceInfoApi by lazy { CastledRetrofitClient.create(DeviceInfoApi::class.java) }
    private val networkWorkManager by lazy { CastledNetworkWorkManager.getInstance(context) }

    suspend fun reportDeviceInfo(request: CastledDeviceInfoRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry deviceInfoApi.reportDeviceInfo(
                getHeaders(), (it as CastledDeviceInfoRequest)
            )
        })
    }

    suspend fun reportDeviceInfoNoRetry(request: CastledDeviceInfoRequest): Response<Void?> {
        return deviceInfoApi.reportDeviceInfo(
            getHeaders(), request
        )
    }

    private fun getHeaders(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        val secureUserId: String? = CastledSharedStore.getSecureUserId()
        hashMap["App-Id"] = CastledSharedStore.getAppId()
        secureUserId?.let { hashMap["Auth-Key"] = it }
        return hashMap

    }
}