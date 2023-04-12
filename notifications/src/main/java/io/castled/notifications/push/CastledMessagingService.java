package io.castled.notifications.push;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.castled.notifications.CastledNotifications;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.logger.LogTags;

public class CastledMessagingService extends FirebaseMessagingService {

    private static final CastledLogger logger = CastledLogger.getInstance(LogTags.PUSH);

    @Override
    public void onNewToken(@NonNull String token) {

        super.onNewToken(token);
        CastledNotifications.onTokenFetch(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        logger.debug("From: " + remoteMessage.getFrom());

        // Handle message payload
        PushNotificationManager.handleNotification(this, remoteMessage);
    }
}
