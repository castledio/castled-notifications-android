package io.castled.android.notifications.push.service

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient.Companion.create
import io.castled.android.notifications.push.NotificationActionContext
import io.castled.android.notifications.push.extensions.toCastledPushEventRequest
import io.castled.android.notifications.push.models.PushTokenInfo
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledPushEventRequest
import io.castled.android.notifications.workmanager.models.CastledPushRegisterRequest
import retrofit2.Response

internal class PushRepository(context: Context) {

    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)
    private val pushApi by lazy { create(PushApi::class.java) }

    suspend fun register(userId: String, tokens: List<PushTokenInfo>) {
        networkWorkManager.apiCallWithRetry(
            request = CastledPushRegisterRequest(userId, tokens),
            apiCall = {
                return@apiCallWithRetry pushApi.register(
                    CastledSharedStore.getApiKey(),
                    it as CastledPushRegisterRequest
                )
            }
        )
    }

    suspend fun reportEvent(event: NotificationActionContext) {
        networkWorkManager.apiCallWithRetry(
            request = event.toCastledPushEventRequest(),
            apiCall = {
                return@apiCallWithRetry pushApi.reportEvent(
                    CastledSharedStore.getApiKey(),
                    it as CastledPushEventRequest
                )
            }
        )
    }

    suspend fun registerNoRetry(userId: String, tokens: List<PushTokenInfo>): Response<Void?> {
        return pushApi.register(
            CastledSharedStore.getApiKey(),
            CastledPushRegisterRequest(userId, tokens)
        )
    }

    suspend fun reportEventNoRetry(eventRequest: CastledPushEventRequest): Response<Void?> {
        return pushApi.reportEvent(
            CastledSharedStore.getApiKey(), eventRequest
        )
    }

}