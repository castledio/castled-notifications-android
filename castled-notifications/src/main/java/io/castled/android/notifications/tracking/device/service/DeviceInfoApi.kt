package io.castled.android.notifications.tracking.device.service

import io.castled.android.notifications.workmanager.models.CastledDeviceInfoRequest
import retrofit2.Response
import retrofit2.http.*

internal interface DeviceInfoApi {

    @POST("external/v1/collections/devices")
    suspend fun reportDeviceInfo(
        @Body request: CastledDeviceInfoRequest,
    ): Response<Void?>
}
