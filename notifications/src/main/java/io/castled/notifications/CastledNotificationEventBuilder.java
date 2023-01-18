package io.castled.notifications;

import android.content.Context;

import java.util.Map;

import io.castled.notifications.consts.NotificationEventType;
import io.castled.notifications.consts.NotificationFields;
import io.castled.notifications.service.models.NotificationEvent;
import io.castled.notifications.store.CastledInstancePrefStore;

public class CastledNotificationEventBuilder {

    private final Context context;
    private final CastledInstancePrefStore prefStore;

    public CastledNotificationEventBuilder(Context context) {
        this.context = context;
        this.prefStore = CastledInstancePrefStore.getInstance();
    }

    public NotificationEvent buildEvent(Map<String, String> payload) {

        NotificationEvent event = new NotificationEvent();

        event.sourceContext = payload.get(NotificationFields.SOURCE_CONTEXT);
        event.teamId = Long.valueOf(payload.get(NotificationFields.TEAM_ID));
        event.notificationId = Integer.valueOf(payload.get(NotificationFields.NOTIFICATION_ID));
        event.setEventType(NotificationEventType.RECEIVED);
        event.setEventTime();
        return event;
    }

}
