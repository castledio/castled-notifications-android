package io.castled.notifications.mipush

import android.content.Context
import io.castled.notifications.push.CastledPushTokenProvider

@Suppress("unused")
class MiPushTokenProvider(context: Context) : CastledPushTokenProvider(context) {

    override fun register(context: Context) {
        MiPushManager.register(context)
    }

    override suspend fun getToken(context: Context): String? {
        return MiPushManager.getToken(context)
    }

    override fun unregister(context: Context) = MiPushManager.unRegister(context)
}