package io.castled.notifications.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.castled.notifications.push.models.Constants;
import io.castled.notifications.push.models.NotificationEventType;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.logger.LogTags;
import io.castled.notifications.push.models.NotificationEvent;

public class CastledEventReceiver extends BroadcastReceiver {

    private static final CastledLogger logger = CastledLogger.getInstance(LogTags.PUSH);

    @Override
    public void onReceive(Context context, Intent intent) {

        handleIntent(context, intent);
    }

    public void handleIntent(Context context, Intent intent) {
        try {
            String action = intent.getAction(); // see NotificationEventType class
            if (action == null || action.trim().isEmpty()) {
                logger.error("Triggered event listener without action!");
                return;
            }

            NotificationEvent event = intent.hasExtra(Constants.EXTRA_EVENT) ?
                    (NotificationEvent) intent.getSerializableExtra(Constants.EXTRA_EVENT) : null;

            String eventAction = event != null && event.actionType != null ? event.actionType :
                    NotificationEventType.NONE.name();

            logger.debug("onReceive: action - " + action + ", eventAction - " + eventAction);

            if(event != null) {
                event.setEventTime();
                reportEvent(event);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportEvent(NotificationEvent event) {
        PushNotification.INSTANCE.reportPushEvent(event);
    }
}