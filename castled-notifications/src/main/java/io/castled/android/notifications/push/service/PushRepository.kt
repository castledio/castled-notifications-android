package io.castled.android.notifications.push.service

import android.content.Context
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.network.CastledRetrofitClient.Companion.create
import io.castled.android.notifications.push.extensions.toCastledPushEventRequest
import io.castled.android.notifications.push.models.CastledPushMessage
import io.castled.android.notifications.push.models.NotificationActionContext
import io.castled.android.notifications.push.models.PushTokenInfo
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledLogoutRequest
import io.castled.android.notifications.workmanager.models.CastledPushEventRequest
import io.castled.android.notifications.workmanager.models.CastledPushRegisterRequest
import retrofit2.Response

internal class PushRepository(context: Context) {

    private val networkWorkManager by lazy { CastledNetworkWorkManager.getInstance(context) }
    private val pushApi by lazy { create(PushApi::class.java) }
    private val logger = CastledLogger.getInstance(LogTags.PUSH_REPOSITORY)

    suspend fun register(userId: String, tokens: List<PushTokenInfo>) {
        networkWorkManager.apiCallWithRetry(
            request = CastledPushRegisterRequest(userId, tokens),
            apiCall = {
                return@apiCallWithRetry pushApi.register(
                    CastledSharedStore.getAppId(),
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
                    CastledSharedStore.getAppId(),
                    it as CastledPushEventRequest
                )
            }
        )
    }

    suspend fun registerNoRetry(userId: String, tokens: List<PushTokenInfo>): Response<Void?> {
        return pushApi.register(
            CastledSharedStore.getAppId(),
            CastledPushRegisterRequest(userId, tokens)
        )
    }

    suspend fun reportEventNoRetry(eventRequest: CastledPushEventRequest): Response<Void?> {
        return pushApi.reportEvent(
            CastledSharedStore.getAppId(), eventRequest
        )
    }

    suspend fun getPushMessages(): List<CastledPushMessage> {
        try {
            val response =
                pushApi.getMessages(CastledSharedStore.getAppId(), CastledSharedStore.getUserId())
            if (response.isSuccessful) {
                return response.body() ?: listOf()
            } else {
                logger.error(response.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            logger.error(e.message ?: "Unknown error")
        }
        return listOf()
    }

    suspend fun logoutUser(userId: String, tokens: List<PushTokenInfo>, sessionId: String?) {
        networkWorkManager.apiCallWithRetry(
            request = CastledLogoutRequest(
                userId, tokens, sessionId
            ),
            apiCall = {
                return@apiCallWithRetry pushApi.logout(
                    CastledSharedStore.getAppId(),
                    it as CastledLogoutRequest
                )
            }
        )
    }

    suspend fun logoutNoRetry(
        userId: String,
        tokens: List<PushTokenInfo>,
        sessionId: String?
    ): Response<Void?> {
        return pushApi.logout(
            CastledSharedStore.getAppId(),
            CastledLogoutRequest(userId, tokens, sessionId)
        )
    }


}