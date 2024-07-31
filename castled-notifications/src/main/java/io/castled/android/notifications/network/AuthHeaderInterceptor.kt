package io.castled.android.notifications.network

import io.castled.android.notifications.network.Constants.NetworkConstants
import io.castled.android.notifications.store.CastledSharedStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        // Add the authorization header to the original request
        val newRequest = originalRequest.newBuilder()
            .header(APP_ID, CastledSharedStore.getAppId())
            .header(AUTH_KEY, CastledSharedStore.getSecureUserId() ?: "")
            .header(PLATFORM, NetworkConstants.CASTLED_ANDROID_PLATFORM)
            .build()
        return chain.proceed(newRequest)
    }

    companion object {

        const val AUTH_KEY = "Auth-Key"
        const val APP_ID = "App-Id"
        const val PLATFORM = "Platform"

    }
}