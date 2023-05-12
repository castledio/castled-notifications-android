package io.castled.android.notifications.store.dao

import androidx.room.TypeConverter
import io.castled.android.notifications.workmanager.models.CastledInAppEventRequest
import io.castled.android.notifications.workmanager.models.CastledNetworkRequest
import io.castled.android.notifications.workmanager.models.CastledPushEventRequest
import io.castled.android.notifications.workmanager.models.CastledPushRegisterRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal class NetworkRequestConverter {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(CastledNetworkRequest::class) {
                subclass(CastledPushRegisterRequest::class)
                subclass(CastledPushEventRequest::class)
                subclass(CastledInAppEventRequest::class)
            }
        }
    }

    @TypeConverter
    fun requestToString(request : CastledNetworkRequest) : String = json.encodeToString(CastledNetworkRequest.serializer(), request)

    @TypeConverter
    fun stringToRequest(request: String) : CastledNetworkRequest = json.decodeFromString(CastledNetworkRequest.serializer(), request)
}