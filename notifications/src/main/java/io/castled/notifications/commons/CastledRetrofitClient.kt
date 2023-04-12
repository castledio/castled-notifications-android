package io.castled.notifications.commons

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class CastledRetrofitClient {

    companion object {
        private const val BASE_URL = "https://%s.castled.io/backend/"
        private const val CLUSTER = "app";
        private var retrofit: Retrofit? = null

        fun <T : Any> create(service: Class<T>): T = retrofit!!.create(service)

        @Synchronized
        fun init() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(String.format(BASE_URL, CLUSTER))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }
    }
}