package io.castled.android.notifications.user_life_cycle

import android.content.Context
import io.castled.android.notifications.push.models.PushTokenInfo
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.user_life_cycle.services.UserLifeCycletRepository

internal object UserLifeCycle {
    private lateinit var userLifeCycletRepository: UserLifeCycletRepository
    internal fun init(context: Context) {
        this.userLifeCycletRepository = UserLifeCycletRepository(context)
    }

    suspend fun logoutUser(userId: String) {
        userLifeCycletRepository.logoutUser(userId, getTokens())
    }

    private fun getTokens(): List<PushTokenInfo> {
        val tokens = mutableListOf<PushTokenInfo>()
        PushTokenType.values().forEach { tokenType ->
            CastledSharedStore.getToken(tokenType)?.let { tokenVal ->
                tokens.add(PushTokenInfo(tokenVal, tokenType))
            }
        }
        return tokens
    }
}