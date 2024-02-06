package io.castled.android.notifications.user_life_cycle.services

import android.content.Context
import io.castled.android.notifications.network.CastledRetrofitClient.Companion.create
import io.castled.android.notifications.push.models.PushTokenInfo
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledLogoutRequest
import retrofit2.Response

internal class UserLifeCycletRepository(context: Context) {

    private val networkWorkManager by lazy { CastledNetworkWorkManager.getInstance(context) }
    private val logoutApi by lazy { create(UserLifeCycleApi::class.java) }

    suspend fun logoutUser(userId: String, tokens: List<PushTokenInfo>) {
        networkWorkManager.apiCallWithRetry(
            request = CastledLogoutRequest(userId, tokens),
            apiCall = {
                return@apiCallWithRetry logoutApi.logout(
                    CastledSharedStore.getAppId(),
                    it as CastledLogoutRequest
                )
            }
        )
    }

    suspend fun logoutNoRetry(userId: String, tokens: List<PushTokenInfo>): Response<Void?> {
        return logoutApi.logout(
            CastledSharedStore.getAppId(),
            CastledLogoutRequest(userId, tokens)
        )
    }
}