package io.castled.notifications.push.service

import io.castled.notifications.commons.CastledRetrofitClient.Companion.create
import io.castled.notifications.push.models.FcmDeviceRegisterRequest
import io.castled.notifications.push.models.NotificationEvent

internal class PushRepository(private val apiKey : String) {

    private val pushApi by lazy { create(PushApi::class.java) }

    suspend fun register(userId: String, token: String?) {
        pushApi.register(apiKey, FcmDeviceRegisterRequest(userId, token))
    }

    suspend fun reportEvent(event : NotificationEvent) {
        pushApi.reportEvent(apiKey, event)
    }
}