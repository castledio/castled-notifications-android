package io.castled.notifications.push;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.castled.notifications.push.models.NotificationEventType;
import io.castled.notifications.push.models.NotificationFields;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.logger.LogTags;
import io.castled.notifications.push.models.NotificationEvent;

public class PushNotificationManager {

    private static final CastledLogger logger = CastledLogger.getInstance(LogTags.PUSH);

    public static boolean isCastledNotification(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey(NotificationFields.CASTLED_KEY)) {
            return true;
        }
        logger.debug("Push message not from Castled!");
        return false;
    }

    public static boolean handleNotification(Context context, RemoteMessage remoteMessage) {

        if (!PushNotificationManager.isCastledNotification(remoteMessage)) {
            return false;
        }
        // Payload from Castled server
        logger.debug("handling castled notification...");

        CastledNotificationEventBuilder eventBuilder = new CastledNotificationEventBuilder();
        NotificationEvent event = eventBuilder.buildEvent(remoteMessage.getData());

        if (PushNotification.INSTANCE.isAppInForeground()) {
            //Ignore the notification, mark event as foreground and report!
            event.setEventType(NotificationEventType.FOREGROUND);
        } else {
            CastledNotificationBuilder notificationBuilder = new CastledNotificationBuilder(context);
            Notification notification = notificationBuilder.buildNotification(remoteMessage.getData(), event.clickEvent());

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            notificationManager.notify(event.notificationId.intValue(), notification);
        }
        reportNotificationEvent(event);
        return true;
    }

    public static void reportNotificationEvent(NotificationEvent event) {
        PushNotification.INSTANCE.reportPushEvent(event);
    }

    public static String getOrCreateNotificationChannel(Context context, String channelId, String channelName, String channelDesc) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDesc);
                notificationManager.createNotificationChannel(channel);
            }
        }

        return channelId;
    }

}
