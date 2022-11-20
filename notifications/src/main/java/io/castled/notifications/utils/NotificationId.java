package io.castled.notifications.utils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.castled.notifications.consts.NotificationFields;

public class NotificationId {

    private final static AtomicInteger c = new AtomicInteger(0);

    public static int getID() {
        return c.incrementAndGet();
    }

    public static int getID(Map<String, String> payload) {

        int notificationId = -1;

        try {

            String serverId = payload.get(NotificationFields.ID);
            if(serverId != null)
                notificationId = Integer.parseInt(serverId);
        }
        catch (Exception ignored) { }

        if(notificationId == -1)
            notificationId = c.incrementAndGet();

        return notificationId;
    }
}
