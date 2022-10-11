package io.castled.notifications;

import android.content.Context;

public class CastledNotifications {

    private static CastledNotificationInstance instance;

    public static void start(Context context, String instanceId) {
        if (instance == null) {
            instance = new CastledNotificationInstance(context, instanceId);
        } else if (!instance.getInstanceId().equals(instanceId)) {
            String errorMessage =
                    "CastledNotifications.start has been called before with a different instanceId! (before: "
                            + instance.getInstanceId() + ", now: " + instanceId + ").";
            throw new IllegalStateException(errorMessage);
        }

        instance.start();
    }

    public static void setUserId(String userId) {
        instance.setUserId(userId);
    }

}
