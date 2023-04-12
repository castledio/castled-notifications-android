package io.castled.notifications.push.models;

public class FcmDeviceRegisterRequest {

    private final String userId;
    private final String fcmToken;

    public FcmDeviceRegisterRequest(String userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
    }
}
