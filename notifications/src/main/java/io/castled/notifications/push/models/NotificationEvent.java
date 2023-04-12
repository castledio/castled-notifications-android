package io.castled.notifications.push.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.TimeZone;

public class NotificationEvent implements Serializable, Cloneable {

    public Long teamId;
    public Long notificationId;
    public String sourceContext;
    public String eventType;

    public String actionLabel;
    public String actionType;
    public String actionUri;

    public String tz;
    public Long ts;

    public void setEventTime() {
        tz = TimeZone.getDefault().getDisplayName();
        ts = System.currentTimeMillis();
    }

    public void setEventType(NotificationEventType type) {
        eventType = type.name();
    }

    public NotificationEvent clickEvent() {

        NotificationEvent event = clone();
        event.setEventType(NotificationEventType.CLICKED);
        return event;
    }
    public NotificationEvent deleteEvent() {

        NotificationEvent event = clone();
        event.setEventType(NotificationEventType.DISCARDED);
        return event;
    }

    public NotificationEvent cloneForEvent(NotificationEventType eType) {

        NotificationEvent event = clone();
        event.setEventType(eType);
        return event;
    }

    @Override @NonNull
    public NotificationEvent clone() {

        try {

            NotificationEvent clone = (NotificationEvent) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
