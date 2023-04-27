package io.castled.notifications.network

import io.castled.notifications.CastledConfigs
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class CastledRetrofitClient {

    companion object {
        private const val BASE_URL = "https://%s.castled.io/backend/"
        private val clusterMap = mapOf(
            CastledConfigs.CastledLocation.US to "app",
            CastledConfigs.CastledLocation.AP to "in",
            CastledConfigs.CastledLocation.INDIA to "in",
            CastledConfigs.CastledLocation.TEST to "test"
        )
        private var retrofit: Retrofit? = null

        fun <T : Any> create(service: Class<T>): T = retrofit!!.create(service)

        @Synchronized
        fun init(configs: CastledConfigs) {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(String.format(BASE_URL, clusterMap[configs.location]))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }
    }
}