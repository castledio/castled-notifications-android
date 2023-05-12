package io.castled.android.notifications.push

import android.content.Context

abstract class CastledPushTokenProvider(val context: Context) {

    abstract fun register(context: Context)

    abstract suspend fun getToken(context: Context): String?

    abstract fun unregister(context: Context)
}