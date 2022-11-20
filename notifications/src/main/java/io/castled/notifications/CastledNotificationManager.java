package io.castled.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.castled.notifications.consts.NotificationFields;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.utils.NotificationId;

public class CastledNotificationManager {

    public static boolean isCastledNotification(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey(NotificationFields.CASTLED_KEY)) {
            return true;
        }
        CastledLogger.getInstance().debug("Push message not from Castled!");
        return false;
    }

    public static void handleNotification(Context context, RemoteMessage remoteMessage) {
        CastledLogger.getInstance().info("handling castled notification...");
        CastledNotificationBuilder notificationBuilder = new CastledNotificationBuilder(context);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NotificationId.getID(remoteMessage.getData()), notificationBuilder.buildNotification(remoteMessage.getData()));
    }

    public static String getOrCreateNotificationChannel(Context context, String channelId, String channelName, String channelDesc) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
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
