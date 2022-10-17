package io.castled.notifications.service;

import io.castled.notifications.service.models.DeviceRegisterResponse;
import io.castled.notifications.service.models.FcmDeviceRegisterRequest;
import io.castled.notifications.service.models.UserUpdateRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CastledNotificationApi {

    @POST("fcm/{instance}/register")
    Call<DeviceRegisterResponse> registerToken(@Path("instance") String instance,
                                               @Body FcmDeviceRegisterRequest fcmDeviceRegisterRequest);

    @POST("{instance}/user")
    Call<Void> setUserId(@Path("instance") String instance, @Body UserUpdateRequest userUpdateRequest);
}
