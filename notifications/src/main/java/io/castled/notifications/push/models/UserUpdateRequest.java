package io.castled.notifications.push.models;

public class UserUpdateRequest {

    private String userId;
    private String anonId;

    public UserUpdateRequest(String userId, String anonId) {
        this.userId = userId;
        this.anonId = anonId;
    }
}