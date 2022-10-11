package io.castled.notifications;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;

import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.store.InstancePrefStore;
import io.castled.notifications.tasks.ServerTaskHandler;
import io.castled.notifications.tasks.ServerTaskListener;
import io.castled.notifications.tasks.ServerTaskQueue;
import io.castled.notifications.tasks.models.TokenRefreshServerTask;
import io.castled.notifications.tasks.models.UserIdSetTask;

public class CastledNotificationInstance {

    private final CastledLogger logger;
    private final String instanceId;
    private final ServerTaskQueue serverTaskQueue;
    private final InstancePrefStore instancePrefStore;

    public String getInstanceId() {
        return instanceId;
    }

    CastledNotificationInstance(Context context, String instanceId) {
        this.logger = CastledLogger.getInstance();
        this.instanceId = instanceId;
        this.instancePrefStore = new InstancePrefStore(context, instanceId);
        File file = new File("./castled-notifications/push");

        this.serverTaskQueue = new ServerTaskQueue(file);
        ServerTaskHandler serverTaskHandler = ServerTaskHandler.getInstance(serverTaskQueue);
        ServerTaskListener listener = new ServerTaskListener(serverTaskHandler);
        this.serverTaskQueue.register(listener);
    }

    public void start() {

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                logger.warning("Fetching FCM registration token failed!");
                return;
            }
            // Get new FCM registration token
            String token = task.getResult();
            logger.debug(token);
            handleTokenFetch(task.getResult());
        });
    }

    public void setUserId(String userId) {
        UserIdSetTask task = new UserIdSetTask(userId);
        serverTaskQueue.add(task);
    }

    private void handleTokenFetch(String token) {
        TokenRefreshServerTask tokenRefreshServerTask = new TokenRefreshServerTask(token);
        serverTaskQueue.add(tokenRefreshServerTask);
    }

}
