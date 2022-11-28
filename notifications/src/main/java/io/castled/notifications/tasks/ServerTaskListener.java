package io.castled.notifications.tasks;

import android.os.Message;

import io.castled.notifications.tasks.models.CastledServerTask;

public class ServerTaskListener implements TaskQueueListener<CastledServerTask> {

    static final int QUEUE_ADD = 1;
    static final int QUEUE_REMOVE = 2;
    static final int QUEUE_FLUSH = 3;
    static final int QUEUE_EMPTY = 4;

    private final ServerTaskHandler serverTaskHandler;

    public ServerTaskListener(ServerTaskHandler serverTaskHandler) {
        this.serverTaskHandler = serverTaskHandler;
    }

    @Override
    public void onAdd(CastledServerTask task) {
        serverTaskHandler.sendMessage(createMessage(QUEUE_ADD, task));
    }

    @Override
    public void onRemove(CastledServerTask task) {
        serverTaskHandler.sendMessage(createMessage(QUEUE_REMOVE, task));
    }

    @Override
    public void onFlush(CastledServerTask task) {
        serverTaskHandler.sendMessage(createMessage(QUEUE_FLUSH, task));
    }

    @Override
    public void onEmpty() {
        serverTaskHandler.sendMessage(createMessage(QUEUE_EMPTY));
    }

    private Message createMessage(int action) {
        return createMessage(action, null);
    }

    private Message createMessage(int action, CastledServerTask task) {
        Message message = new Message();
        message.what = action;
        if(task != null)
            message.obj = task;
        return message;
    }
}