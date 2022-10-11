package io.castled.notifications.tasks.models;

public class UserIdSetTask extends CastledServerTask {

    public UserIdSetTask(String userId) {
        super(CastledServerTaskType.USERID_SET);
        this.userId = userId;
    }

    private String userId;
}
