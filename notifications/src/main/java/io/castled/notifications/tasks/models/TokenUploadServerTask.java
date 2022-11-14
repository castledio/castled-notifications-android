package io.castled.notifications.tasks.models;

public class TokenUploadServerTask extends CastledServerTask {

    public TokenUploadServerTask(String token) {
        super(CastledServerTaskType.TOKEN_REGISTER);
        this.fcmToken = token;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    String fcmToken;
}
