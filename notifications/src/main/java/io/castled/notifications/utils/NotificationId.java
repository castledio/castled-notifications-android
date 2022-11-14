package io.castled.notifications.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationId {

    private final static AtomicInteger c = new AtomicInteger(0);
    public static int getID() {
        return c.incrementAndGet();
    }
}
