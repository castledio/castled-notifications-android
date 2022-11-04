package io.castled.notifications;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import io.castled.notifications.consts.NotificationFields;
import io.castled.notifications.logger.CastledLogger;

public class CastledNotificationBuilder {

    private final Context context;

    public CastledNotificationBuilder(Context context) {
        this.context = context;
    }

    public Notification buildNotification(Map<String, String> payload) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, getChannelId(payload));

        //.setSmallIcon(R.drawable.new_mail)
        notificationBuilder.setContentTitle(payload.get(NotificationFields.TITLE));

        // Priority
        setPriority(notificationBuilder, payload);

        // Small Icon
        setSmallIcon(notificationBuilder, payload);

        // Large icon
        setLargeIcon(notificationBuilder, payload);

        // Channel
        setChannel(notificationBuilder, payload);

        // notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
        // notificationBuilder.bigText(emailObject.getSubjectAndSnippet()))
        if (payload.get(NotificationFields.SUMMARY) != null) {
            notificationBuilder.setContentText(payload.get(NotificationFields.SUMMARY));
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .setSummaryText(payload.get(NotificationFields.SUMMARY))
                    .bigText(payload.get(NotificationFields.BODY)));
        } else {
            notificationBuilder.setContentText(payload.get(NotificationFields.BODY));
        }

        setTimeout(notificationBuilder, payload);

        notificationBuilder.setAutoCancel(true);
        return notificationBuilder.build();
    }

    private void setPriority(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        // Priority
        String priority = payload.get(NotificationFields.PRIORITY);
        if ("high".equals(priority)) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    private void setSmallIcon(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        Resources resources = context.getResources();
        String smallIcon = payload.get(NotificationFields.SMALL_ICON);
        if (smallIcon != null) {
            final int resourceId = resources.getIdentifier(smallIcon, "drawable", context.getPackageName());
            notificationBuilder.setSmallIcon(resourceId);
        } else {
            notificationBuilder.setSmallIcon(R.drawable.io_castled_push_notification_small_icon);
        }
    }

    private void setLargeIcon(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        // Large icon
        String largeIconUrl = payload.get(NotificationFields.LARGE_ICON);
        if (largeIconUrl != null) {
            try {
                URL url = new URL(largeIconUrl);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setLargeIcon(image);
            } catch(IOException e) {
                CastledLogger.getInstance().error(e.getMessage());
            }
        } else if (R.drawable.io_castled_push_notification_large_icon != 0) {
            Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.io_castled_push_notification_large_icon);
            notificationBuilder.setLargeIcon(image);
        }
    }

    private void setChannel(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        String channelId = payload.get(NotificationFields.CHANNEL_ID);
        String channelName = payload.get(NotificationFields.CHANNEL_NAME);
        String channelDesc = payload.get(NotificationFields.CHANNEL_DESCRIPTION);

        if (channelId == null) {
            channelId = context.getString(R.string.io_castled_push_default_channel_id);
            channelName = channelId;
            channelDesc = context.getString(R.string.io_castled_push_default_channel_desc);
        }
        notificationBuilder.setChannelId(CastledNotificationManager
                .getOrCreateNotificationChannel(context, channelId, channelName, channelDesc));
    }

    private String getChannelId(Map<String, String> payload) {
        String channelId = payload.get(NotificationFields.CHANNEL_ID);
        if (channelId == null) {
            channelId = context.getString(R.string.io_castled_push_default_channel_id);
        }
        return channelId;
    }

    private void setTimeout(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        String ttlStr = payload.get(NotificationFields.TTL);
        if (ttlStr != null) {
            notificationBuilder.setTimeoutAfter(Long.parseLong(ttlStr));
        }
    }
}
