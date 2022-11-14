package io.castled.notifications.tasks.models;

public abstract class CastledServerTask {

    private CastledServerTaskType taskType;

    public CastledServerTask(CastledServerTaskType taskType) {
        this.taskType = taskType;
    }

    public CastledServerTaskType getTaskType() {
        return taskType;
    }
}
