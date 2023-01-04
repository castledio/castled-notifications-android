package io.castled.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.castled.notifications.logger.CastledLogger;

public class CastledMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {

        super.onNewToken(token);
        CastledNotifications.getInstance().handleTokenFetch(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        CastledLogger.getInstance().debug("From: " + remoteMessage.getFrom());

        // Handle message payload
        CastledNotificationManager.handleNotification(this, remoteMessage);
    }
}
