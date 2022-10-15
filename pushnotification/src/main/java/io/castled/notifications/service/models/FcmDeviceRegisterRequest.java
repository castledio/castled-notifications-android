package io.castled.notifications.service.models;

public class FcmDeviceRegisterRequest {

    private String fcmToken;

    public FcmDeviceRegisterRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

}
