package io.castled.inAppTriggerEvents.requests

import io.castled.inAppTriggerEvents.eventConsts.TriggerEventConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


internal object ServiceGenerator {
    fun requestApi(): ApiInterface {
        return Retrofit.Builder()
            .baseUrl(TriggerEventConstants.notificationUrl)
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
