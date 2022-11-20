package io.castled.notifications.tasks.models;

import io.castled.notifications.service.models.NotificationEvent;

public class NotificationEventServerTask extends CastledServerTask {

    private final NotificationEvent event;

    public NotificationEventServerTask(NotificationEvent event) {
        super(CastledServerTaskType.NOTIFICATION_EVENT);
        this.event = event;
    }

    public NotificationEvent getEvent() {
        return event;
    }
}
