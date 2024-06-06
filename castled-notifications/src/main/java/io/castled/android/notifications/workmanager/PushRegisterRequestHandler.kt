package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.commons.extenstions.isSuccessfulOrIgnoredError
import io.castled.android.notifications.push.service.PushRepository
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.workmanager.models.CastledPushRegisterRequest

internal class PushRegisterRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val pushRepository by lazy { PushRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        for (entry in requests) {
            try {
                val response = pushRepository.registerNoRetry(
                    (entry.request as CastledPushRegisterRequest).userId, entry.request.tokens
                )
                if (!response.isSuccessfulOrIgnoredError()) {
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