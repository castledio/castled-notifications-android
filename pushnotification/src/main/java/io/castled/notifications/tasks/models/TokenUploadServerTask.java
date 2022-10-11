package io.castled.notifications.tasks.models;

public class TokenUploadServerTask extends CastledServerTask {

    public TokenUploadServerTask(String token) {
        super(CastledServerTaskType.TOKEN_REGISTER);
        this.fcmToken = token;
    }

    String fcmToken;
}
