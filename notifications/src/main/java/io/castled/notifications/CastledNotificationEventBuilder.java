package io.castled.notifications;

import android.content.Context;

import java.util.Map;

import io.castled.notifications.consts.NotificationEventType;
import io.castled.notifications.consts.NotificationFields;
import io.castled.notifications.service.models.NotificationEvent;
import io.castled.notifications.store.CastledInstancePrefStore;
import io.castled.notifications.utils.NotificationId;

public class CastledNotificationEventBuilder {

    private final Context context;
    private final CastledInstancePrefStore prefStore;

    public CastledNotificationEventBuilder(Context context) {
        this.context = context;
        this.prefStore = CastledInstancePrefStore.getInstance();
    }

    public NotificationEvent buildEvent(Map<String, String> payload) {

        NotificationEvent event = new NotificationEvent();

        event.notificationId = ""+ NotificationId.getID(payload);
        event.sourceUUID = payload.get(NotificationFields.SOURCE_UUID);
        event.sourceType = payload.get(NotificationFields.SOURCE_TYPE);
        event.sourceType = payload.get(NotificationFields.SOURCE_TYPE);
        event.stepId = payload.get(NotificationFields.STEP_ID);
        event.userId = prefStore.getUserIdIfAvailable();
        event.setEventType(NotificationEventType.RECEIVED);
        event.setEventTime();

        return event;
    }


}
