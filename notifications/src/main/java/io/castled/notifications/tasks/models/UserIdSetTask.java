package io.castled.notifications.tasks.models;

public class UserIdSetTask extends CastledServerTask {

    private final String userId;

    public UserIdSetTask(String userId) {
        super(CastledServerTaskType.USERID_SET);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
