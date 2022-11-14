package io.castled.notifications.service.models;

public class DeviceRegisterResponse {

    private String anonId;

    public DeviceRegisterResponse(String anonId) {
        this.anonId = anonId;
    }

    public String getAnonId() {
        return anonId;
    }

}
