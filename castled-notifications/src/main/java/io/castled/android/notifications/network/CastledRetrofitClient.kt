package io.castled.android.notifications.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.castled.android.notifications.CastledConfigs
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

internal class CastledRetrofitClient {

    companion object {
        private const val BASE_URL = "https://%s.castled.io/backend/"
        private val clusterMap = mapOf(
            CastledConfigs.CastledLocation.US to "api",
            CastledConfigs.CastledLocation.AP to "in",
            CastledConfigs.CastledLocation.IN to "in",
            CastledConfigs.CastledLocation.TEST to "test"
        )
        private var retrofit: Retrofit? = null
        private val json = Json { ignoreUnknownKeys = true }
        private val contentType = MediaType.get("application/json")

        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthHeaderInterceptor()) // Add your token here
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        fun <T : Any> create(service: Class<T>): T = retrofit!!.create(service)

        @OptIn(ExperimentalSerializationApi::class)
        @Synchronized
        fun init(configs: CastledConfigs) {
            if (retrofit == null) {

                retrofit = Retrofit.Builder()
                    .baseUrl(String.format(BASE_URL, clusterMap[configs.location]))
                    .client(okHttpClient)
                    .addConverterFactory(json.asConverterFactory(contentType))
                    .build()
            }
        }
    }
}