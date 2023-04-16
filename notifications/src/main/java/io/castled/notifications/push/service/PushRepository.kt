package io.castled.notifications.push.service

import android.content.Context
import io.castled.notifications.commons.CastledRetrofitClient.Companion.create
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags
import io.castled.notifications.push.models.NotificationEvent
import io.castled.notifications.store.CastledSharedStore
import io.castled.notifications.workmanager.CastledNetworkWorkManager
import io.castled.notifications.workmanager.CastledRequestConverters.toCastledPushEventRequest
import io.castled.notifications.workmanager.models.CastledPushEventRequest
import io.castled.notifications.workmanager.models.CastledPushRegisterRequest
import retrofit2.Response

internal class PushRepository(context: Context) {

    private val logger = CastledLogger.getInstance(LogTags.PUSH_REPOSITORY)
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

    suspend fun reportEvent(event: NotificationEvent) {
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

    // TODO: Intercept and throw exceptions for 4xx and 5xx errors
    suspend fun registerNoRetry(userId: String, token: String?): Response<Void?> {
        return pushApi.register(
            CastledSharedStore.getApiKey(),
            CastledPushRegisterRequest(userId, token)
        )
    }

    suspend fun reportEventNoRetry(event: NotificationEvent): Response<Void?> {
        return pushApi.reportEvent(
            CastledSharedStore.getApiKey(), event.toCastledPushEventRequest()
        )
    }

}