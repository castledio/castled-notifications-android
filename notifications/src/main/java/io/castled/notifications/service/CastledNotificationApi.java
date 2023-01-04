package io.castled.notifications.service;

import io.castled.notifications.service.models.FcmDeviceRegisterRequest;
import io.castled.notifications.service.models.NotificationEvent;
import io.castled.notifications.service.models.UserUpdateRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CastledNotificationApi {

    @POST("push/{instance}/fcm/register")
    Call<Void> registerToken(@Path("instance") String instance,
                                               @Body FcmDeviceRegisterRequest fcmDeviceRegisterRequest);

    @POST("push/{instance}/user")
    Call<Void> setUserId(@Path("instance") String instance, @Body UserUpdateRequest userUpdateRequest);

    @POST("push/{instance}/event")
    Call<Void> reportEvent(@Path("instance") String instance, @Body NotificationEvent notificationEvent);

    @POST("inapp/{instance}/{platform}/campaigns")
    Call<Void> fetchInAppPayloads(@Path("instance") String instance, @Path("platform") String platform, @Query("user") String user);
}
