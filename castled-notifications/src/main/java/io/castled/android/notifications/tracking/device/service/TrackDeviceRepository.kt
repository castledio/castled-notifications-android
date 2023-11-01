package io.castled.android.notifications.tracking.events.service

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledDeviceTrackingRequest
import retrofit2.Response

internal class TrackDeviceRepository(context: Context) {

    private val trackDeviceApi by lazy { CastledRetrofitClient.create(TrackDeviceApi::class.java) }
    private val networkWorkManager by lazy { CastledNetworkWorkManager.getInstance(context) }

    suspend fun reportDeviceTracking(request: CastledDeviceTrackingRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry trackDeviceApi.reportDeviceTracking(
                getHeaders(), (it as CastledDeviceTrackingRequest)
            )
        })
    }

    suspend fun reportDeviceTrackingNoRetry(request: CastledDeviceTrackingRequest): Response<Void?> {
        return trackDeviceApi.reportDeviceTracking(
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