package io.castled.notifications.workmanager

import io.castled.notifications.store.models.NetworkRetryLog

internal interface NetworkRequestHandler {

    suspend fun handleRequest(
        requests: List<NetworkRetryLog>,
        onSuccess: (entries: List<NetworkRetryLog>) -> Unit,
        onError: (entries: List<NetworkRetryLog>) -> Unit
    )
}