package io.castled.android.notifications.trackevents.service

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import retrofit2.Response

internal class TrackEventRepository(context: Context) {

    private val trackEventApi = CastledRetrofitClient.create(TrackEventApi::class.java)
    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)

    suspend fun reportEvent(request: CastledTrackEventRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry trackEventApi.reportEvent(
                getHeaders(), (it as CastledTrackEventRequest)
            )
        })
    }

    suspend fun reportEventNoRetry(request: CastledTrackEventRequest): Response<Void?> {
        return trackEventApi.reportEvent(
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