package io.castled.notifications.tasks;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.tasks.models.CastledServerTask;

public class ServerTaskHandler extends Handler {

    private final CastledLogger logger;
    private final ServerTaskQueue taskQueue;

    private ServerTaskHandler(Looper looper, ServerTaskQueue taskQueue) {
        super(looper);
        this.logger = CastledLogger.getInstance();
        this.taskQueue = taskQueue;
    }

    public static ServerTaskHandler getInstance(ServerTaskQueue taskQueue) {
        HandlerThread handlerThread = new HandlerThread("CastledServerTaskHandlerThread");
        return new ServerTaskHandler(handlerThread.getLooper(), taskQueue);
    }

    @Override
    public void handleMessage(Message msg) {
        CastledServerTask serverTask = taskQueue.peek();
        if (serverTask != null) {
            processTask(serverTask);
            taskQueue.remove();
        }
    }

    private void processTask(CastledServerTask serverTask) {
        switch (serverTask.getTaskType()) {
            case USERID_SET:
                processUserIdTask(serverTask);
            case TOKEN_REGISTER:
                processTokenRegister(serverTask);
            default:
                logger.error(String.format("Unhandled task type: %s", serverTask.getTaskType()));
        }
    }

    private void processTokenRegister(CastledServerTask serverTask) {
        logger.debug("processing token register");
    }

    private void processUserIdTask(CastledServerTask serverTask) {
        logger.debug("processing user id task");
    }

}
