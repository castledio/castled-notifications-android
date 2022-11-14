package io.castled.notifications.tasks;

import io.castled.notifications.tasks.models.CastledServerTask;

public class ServerTaskListener implements TaskQueueListener<CastledServerTask> {

    private final ServerTaskHandler serverTaskHandler;

    public ServerTaskListener(ServerTaskHandler serverTaskHandler) {
        this.serverTaskHandler = serverTaskHandler;
    }

    @Override
    public void onAdd(CastledServerTask task) {
        serverTaskHandler.sendEmptyMessage(1);
    }

    @Override
    public void onRemove(CastledServerTask task) {
        // no-op
    }
}