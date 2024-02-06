package io.castled.android.notifications.workmanager

import android.content.Context
import io.castled.android.notifications.store.models.NetworkRetryLog
import io.castled.android.notifications.user_life_cycle.services.UserLifeCycletRepository
import io.castled.android.notifications.workmanager.models.CastledLogoutRequest

internal class LogoutRequestHandler(appContext: Context) : NetworkRequestHandler {

    private val userLifeCycletRepository by lazy { UserLifeCycletRepository(appContext) }

    override suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    ) {
        for (entry in requests) {
            try {
                val response = userLifeCycletRepository.logoutNoRetry(
                    (entry.request as CastledLogoutRequest).userId,
                    entry.request.tokens
                )
                if (!response.isSuccessful) {
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