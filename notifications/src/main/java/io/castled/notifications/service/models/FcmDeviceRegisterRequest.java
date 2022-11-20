package io.castled.notifications.service.models;

public class FcmDeviceRegisterRequest {

    private final String fcmToken;
    private String userId;

    public FcmDeviceRegisterRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public FcmDeviceRegisterRequest(String fcmToken, String userId) {
        this.fcmToken = fcmToken;
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

}
