package io.castled.notifications.push;

import java.util.Map;
import java.util.Optional;

import io.castled.notifications.push.models.NotificationEventType;
import io.castled.notifications.push.models.NotificationFields;
import io.castled.notifications.push.models.NotificationEvent;

public class CastledNotificationEventBuilder {

    public NotificationEvent buildEvent(Map<String, String> payload) {

        NotificationEvent event = new NotificationEvent();

        event.sourceContext = payload.get(NotificationFields.SOURCE_CONTEXT);
        event.teamId = Optional.ofNullable(payload.get(NotificationFields.TEAM_ID)).map(Long::valueOf)
                .orElse(null);
        event.notificationId = Optional.ofNullable(payload.get(NotificationFields.NOTIFICATION_ID)).map(Long::valueOf)
                .orElse(null);
        event.setEventType(NotificationEventType.RECEIVED);
        event.setEventTime();
        return event;
    }

}
