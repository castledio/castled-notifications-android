package io.castled.android.notifications.tracking.events.service

import io.castled.android.notifications.workmanager.models.CastledDeviceInfoRequest
import retrofit2.Response
import retrofit2.http.*

internal interface DeviceInfoApi {

    @POST("external/v1/collections/devices")
    suspend fun reportDeviceInfo(
        @HeaderMap headers: Map<String, String>,
        @Body request: CastledDeviceInfoRequest,
    ): Response<Void?>
}
