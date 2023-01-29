package io.castled.inappNotifications.requests

import io.castled.inappNotifications.notificationConsts.NotificationConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ServiceGenerator {
    fun requestApi(): ApiInterface {
        return Retrofit.Builder()
            .baseUrl(NotificationConstants.notificationUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
            )
            .build()
            .create(ApiInterface::class.java)

    }
}
