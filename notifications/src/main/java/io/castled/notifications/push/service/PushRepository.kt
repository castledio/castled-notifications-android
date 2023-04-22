package io.castled.notifications.push.service

import android.content.Context
import io.castled.notifications.network.CastledRetrofitClient.Companion.create
import io.castled.notifications.push.models.NotificationActionContext
import io.castled.notifications.store.CastledSharedStore
import io.castled.notifications.workmanager.CastledNetworkWorkManager
import io.castled.notifications.workmanager.CastledRequestConverters.toCastledPushEventRequest
import io.castled.notifications.workmanager.models.CastledPushEventRequest
import io.castled.notifications.workmanager.models.CastledPushRegisterRequest
import retrofit2.Response

internal class PushRepository(context: Context) {

    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)
    private val pushApi by lazy { create(PushApi::class.java) }

    suspend fun register(userId: String, token: String?) {
        networkWorkManager.apiCallWithRetry(
            request = CastledPushRegisterRequest(userId, token),
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

    suspend fun registerNoRetry(userId: String, token: String?): Response<Void?> {
        return pushApi.register(
            CastledSharedStore.getApiKey(),
            CastledPushRegisterRequest(userId, token)
        )
    }

    suspend fun reportEventNoRetry(eventRequest: CastledPushEventRequest): Response<Void?> {
        return pushApi.reportEvent(
            CastledSharedStore.getApiKey(), eventRequest
        )
    }

}