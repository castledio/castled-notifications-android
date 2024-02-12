package io.castled.android.notifications.sessions.service

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledSessionRequest
import retrofit2.Response

internal class SessionsRepository(context: Context) {

    private val sessionsApi by lazy { CastledRetrofitClient.create(SessionsApi::class.java) }
    private val networkWorkManager by lazy { CastledNetworkWorkManager.getInstance(context) }

    suspend fun reportSessionEvent(request: CastledSessionRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry sessionsApi.reportEvent(
                getHeaders(), (it as CastledSessionRequest)
            )
        })
    }

    suspend fun reportSessionEventNoRetry(request: CastledSessionRequest): Response<Void?> {
        return sessionsApi.reportEvent(
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