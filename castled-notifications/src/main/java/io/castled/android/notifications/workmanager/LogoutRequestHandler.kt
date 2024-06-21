package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.commons.extenstions.isSuccessfulOrIgnoredError
import io.castled.android.notifications.push.service.PushRepository
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.workmanager.models.CastledLogoutRequest

internal class LogoutRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val pushRepository by lazy { PushRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        for (entry in requests) {
            try {
                if ((entry.request as CastledLogoutRequest).userId == CastledSharedStore.getUserId()) {
                    onSuccess(listOf(entry))
                    // adding this condition to prevent retrying for a previous user who logged in with the same user ID.
                    onSuccess(listOf(entry))
                    continue
                }
                val response = pushRepository.logoutNoRetry(
                    entry.request.userId,
                    entry.request.tokens,
                    entry.request.sessionId
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