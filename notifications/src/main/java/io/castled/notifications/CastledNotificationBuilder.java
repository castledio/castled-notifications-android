package io.castled.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import io.castled.notifications.consts.ActionButton;
import io.castled.notifications.consts.ClickAction;
import io.castled.notifications.consts.NotificationFields;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.utils.CastledUtils;
import io.castled.notifications.utils.Utils;

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

        // Action buttons
        addActionButtons(notificationBuilder, payload);

        // Image
        setImage(notificationBuilder, payload);

        // Channel
        setChannel(notificationBuilder, payload);

        // notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
        // notificationBuilder.bigText(emailObject.getSubjectAndSnippet()))
        setSummaryAndBody(notificationBuilder, payload);

        setTimeout(notificationBuilder, payload);

        Intent intent1 = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Intent intent = new Intent(Intent.ACTION_MAIN).setPackage(context.getPackageName());
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context, 0, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        notificationBuilder.setContentIntent(notifyPendingIntent);
        notificationBuilder.setAutoCancel(true);
        return notificationBuilder.build();
    }

    private void setSummaryAndBody(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        String summary = payload.get(NotificationFields.SUMMARY);
        String body = payload.get(NotificationFields.BODY);
        String image = payload.get(NotificationFields.IMAGE);
        if (!CastledUtils.isEmpty(image)) {
            if (!CastledUtils.isEmpty(summary)) {
                notificationBuilder.setContentText(summary);
            }
        } else {
            if (!CastledUtils.isEmpty(summary)) {
                String summaryAndBody = String.format("%s\n%s",summary, body);
                notificationBuilder.setContentText(summary);
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(summaryAndBody));
            } else {
                notificationBuilder.setContentText(body);
            }
        }
    }

    private void setPriority(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        // Priority
        String priority = payload.get(NotificationFields.PRIORITY);
        if ("high".equals(priority)) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    // TODO: @Arun Jose
    private void setSmallIcon(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        Resources resources = context.getResources();
        String smallIcon = payload.get(NotificationFields.SMALL_ICON);
        if (!CastledUtils.isEmpty(smallIcon)) {
            //final int resourceId = resources.getIdentifier(smallIcon, "drawable", context.getPackageName());
            //notificationBuilder.setSmallIcon(resourceId);
            notificationBuilder.setSmallIcon(R.mipmap.small_icon); // Testing, Jose
        } else {
            notificationBuilder.setSmallIcon(R.drawable.io_castled_push_notification_small_icon);
        }
    }

    private void setLargeIcon(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        // Large icon
        String largeIconUrl = payload.get(NotificationFields.LARGE_ICON);
        if (!CastledUtils.isEmpty(largeIconUrl)) {
            notificationBuilder.setLargeIcon(getBitmapFromUrl(largeIconUrl));
        } else if (R.drawable.io_castled_push_notification_large_icon != 0) {
            Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.io_castled_push_notification_large_icon);
            notificationBuilder.setLargeIcon(image);
        }
    }

    private void addActionButtons(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {

        String actionButtonData = payload.get(NotificationFields.ACTION_BUTTONS);

        if(actionButtonData != null && !actionButtonData.trim().isEmpty()) {

            List<ActionButton> actionButtonList = new Gson().fromJson(actionButtonData,
                    new TypeToken<List<ActionButton>>() {}.getType());

            boolean isOS11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

            for (ActionButton button: actionButtonList) {

                try {

                    Intent intent = null;

                    if(button.clickAction.equals(ClickAction.DEEP_LINKING.name())) {

                        intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(Utils.addQueryParams(button.url, button.keyVals)));
                    }
                    else if(button.clickAction.equals(ClickAction.NAVIGATE_TO_SCREEN.name())) {

                        intent = new Intent(context, Class.forName(button.url)); // Class name should be fully qualified name
                        intent.putExtras(Utils.mapToBundle(button.keyVals));
                    }

                    if(intent != null) {

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                                isOS11 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Action action = new NotificationCompat.Action(0, button.label, pendingIntent);
                        notificationBuilder.addAction(action);
                    }
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }

    private void setChannel(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        String channelId = payload.get(NotificationFields.CHANNEL_ID);
        String channelName = payload.get(NotificationFields.CHANNEL_NAME);
        String channelDesc = payload.get(NotificationFields.CHANNEL_DESCRIPTION);

        if (CastledUtils.isEmpty(channelId) || CastledUtils.isEmpty(channelName)) {
            channelId = context.getString(R.string.io_castled_push_default_channel_id);
            channelName = channelId;
            channelDesc = context.getString(R.string.io_castled_push_default_channel_desc);
        }
        notificationBuilder.setChannelId(CastledNotificationManager
                .getOrCreateNotificationChannel(context, channelId, channelName, channelDesc));
    }

    private String getChannelId(Map<String, String> payload) {
        String channelId = payload.get(NotificationFields.CHANNEL_ID);
        if (CastledUtils.isEmpty(channelId)) {
            channelId = context.getString(R.string.io_castled_push_default_channel_id);
        }
        return channelId;
    }

    private void setTimeout(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        String ttlStr = payload.get(NotificationFields.TTL);
        if (!CastledUtils.isEmpty(ttlStr)) {
            notificationBuilder.setTimeoutAfter(Long.parseLong(ttlStr));
        }
    }

    private void setImage(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {
        String image = payload.get(NotificationFields.IMAGE);
        String body = payload.get(NotificationFields.BODY);
        if (!CastledUtils.isEmpty(image)) {
            notificationBuilder.setStyle( new NotificationCompat.BigPictureStyle()
                    .bigPicture(getBitmapFromUrl(image))
                    .setSummaryText(body)
            );
        }
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch(IOException e) {
            CastledLogger.getInstance().error(e.getMessage());
        }
        return null;
    }
}
