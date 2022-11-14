package io.castled.notifications;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.castled.notifications.logger.CastledLogger;

public class CastledMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        CastledLogger.getInstance().info("fcm token:" + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        CastledLogger.getInstance().info("In onMessageReceived");
        CastledLogger.getInstance().debug("From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (CastledNotificationManager.isCastledNotification(remoteMessage)) {
            CastledNotificationManager.handleNotification(this, remoteMessage);
        }
    }

}
