package io.castled.notifications.push;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import io.castled.notifications.R;
import io.castled.notifications.push.models.Constants;
import io.castled.notifications.push.models.NotificationEventType;
import io.castled.notifications.push.models.ActionButton;
import io.castled.notifications.push.models.ClickAction;
import io.castled.notifications.push.models.NotificationFields;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.logger.LogTags;
import io.castled.notifications.push.models.NotificationEvent;
import io.castled.notifications.push.utils.CastledUtils;
import io.castled.notifications.push.utils.NotificationId;

public class CastledNotificationBuilder {

    private static final CastledLogger logger = CastledLogger.getInstance(LogTags.PUSH);
    private final Context context;

    public CastledNotificationBuilder(Context context) {
        this.context = context;
    }

    public Notification buildNotification(Map<String, String> payload) {
        return buildNotification(payload, null);
    }

    public Notification buildNotification(Map<String, String> payload, @Nullable NotificationEvent event) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, getChannelId(payload));

        //.setSmallIcon(R.drawable.new_mail)
        notificationBuilder.setContentTitle(payload.get(NotificationFields.TITLE));

        // Priority
        setPriority(notificationBuilder, payload);

        // Small Icon
        setSmallIcon(notificationBuilder, payload);

        // Large icon
        setLargeIcon(notificationBuilder, payload);

        // Image
        setImage(notificationBuilder, payload);

        // Channel
        setChannel(notificationBuilder, payload);

        // notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
        // notificationBuilder.bigText(emailObject.getSubjectAndSnippet()))
        setSummaryAndBody(notificationBuilder, payload);

        setTimeout(notificationBuilder, payload);

        // Notification click action
        addNotificationAction(notificationBuilder, payload, event != null ? event.clickEvent() : null);

        // Action buttons
        addActionButtons(notificationBuilder, payload, event != null ? event.clickEvent() : null);

        // Dismiss action
        addDismissAction(notificationBuilder, payload, event != null ? event.deleteEvent() : null);

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

    private void setSmallIcon(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {

        int resourceId = 0;

        Resources resources = context.getResources();
        String smallIcon = payload.get(NotificationFields.SMALL_ICON);
        if (!CastledUtils.isEmpty(smallIcon))
            resourceId = resources.getIdentifier(smallIcon, "drawable", context.getPackageName());

        if(resourceId <= 0)
            resourceId = R.drawable.io_castled_push_notification_small_icon;

        if(resourceId > 0) {
            notificationBuilder.setSmallIcon(resourceId);
        }
        else {
            notificationBuilder.setSmallIcon(IconCompat.createWithBitmap(
                    getBitmapFromDrawable(context.getApplicationInfo().loadIcon(context.getPackageManager())))
            );
        }
    }

    private void setLargeIcon(NotificationCompat.Builder notificationBuilder, Map<String, String> payload) {

        String largeIconUrl = payload.get(NotificationFields.LARGE_ICON);
        if (!CastledUtils.isEmpty(largeIconUrl)) {
            notificationBuilder.setLargeIcon(getBitmapFromUrl(largeIconUrl));
        }
        else if (R.drawable.io_castled_push_notification_large_icon > 0) {
            Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.io_castled_push_notification_large_icon);
            notificationBuilder.setLargeIcon(image);
        }
        else {
            notificationBuilder.setLargeIcon(
                getBitmapFromDrawable(context.getApplicationInfo().loadIcon(context.getPackageManager()))
            );
        }
    }

    private void addNotificationAction(NotificationCompat.Builder notificationBuilder, Map<String, String> payload, NotificationEvent event) {

        String clickAction = payload.get(NotificationFields.CLICK_ACTION);
        String clickActionUrl = payload.get(NotificationFields.CLICK_ACTION_URL);
        String actionLabel = "";
        String keyValues = payload.get(NotificationFields.KEY_VALUES);

        if(clickAction != null && clickAction.equals(ClickAction.NONE.name())) {
            clickActionUrl = null;
            keyValues = null;
        }
        else if(clickAction == null || clickActionUrl == null || clickActionUrl.trim().isEmpty()) {
            clickAction = ClickAction.DEFAULT.name();
            clickActionUrl = null;
            keyValues = null;
        }

        PendingIntent pendingIntent = createNotificationIntent(event, NotificationEventType.CLICKED.name(),
                clickAction, clickActionUrl, actionLabel, keyValues);

        notificationBuilder.setContentIntent(pendingIntent);
    }

    private void addActionButtons(NotificationCompat.Builder notificationBuilder, Map<String, String> payload, NotificationEvent event) {

        String actionButtonData = payload.get(NotificationFields.ACTION_BUTTONS);

        if(actionButtonData != null && !actionButtonData.trim().isEmpty()) {

            List<ActionButton> actionButtonList = new Gson().fromJson(actionButtonData,
                    new TypeToken<List<ActionButton>>() {}.getType());

            Gson gson = new Gson();

            for (ActionButton button: actionButtonList) {

                try {

                    PendingIntent pendingIntent = createNotificationIntent(event, NotificationEventType.CLICKED.name(),
                            button.clickAction, button.url, button.label, gson.toJson(button.keyVals));

                    NotificationCompat.Action action = new NotificationCompat.Action(0, button.label, pendingIntent);
                    notificationBuilder.addAction(action);
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
    }

    private void addDismissAction(NotificationCompat.Builder notificationBuilder, Map<String, String> payload, NotificationEvent event) {

        try {

            String clickAction = payload.get(NotificationFields.CLICK_ACTION);
            String clickActionUrl = payload.get(NotificationFields.CLICK_ACTION_URL);
            String actionLabel = "";
            String keyValues = payload.get(NotificationFields.KEY_VALUES);

            if(clickAction != null && clickAction.equals(ClickAction.NONE.name())) {
                clickActionUrl = null;
                keyValues = null;
            }
            else if(clickAction == null || clickActionUrl == null || clickActionUrl.trim().isEmpty()) {
                clickAction = ClickAction.DEFAULT.name();
                clickActionUrl = null;
                keyValues = null;
            }

            PendingIntent pendingIntent = createNotificationIntent(event, NotificationEventType.DISCARDED.name(),
                    clickAction, clickActionUrl, actionLabel, keyValues);

            notificationBuilder.setDeleteIntent(pendingIntent);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    private PendingIntent createNotificationIntent(NotificationEvent event, String eventAction, String clickAction,
                                        String clickActionUrl, String clickActionLabel, String keyValues) {

        boolean isDismiss = eventAction.equals(NotificationEventType.DISCARDED.name());

        Intent intent = new Intent(context, isDismiss ? CastledEventReceiver.class : CastledEventListener.class);
        intent.setAction(eventAction);

        if(clickAction != null)
            intent.putExtra(Constants.EXTRA_ACTION, clickAction);

        if(clickActionUrl != null)
            intent.putExtra(Constants.EXTRA_URI, clickActionUrl);

        if(clickActionLabel != null)
            intent.putExtra(Constants.EXTRA_LABEL, clickActionLabel);

        if(keyValues != null)
            intent.putExtra(Constants.EXTRA_KEY_VAL_PARAMS, keyValues);

        if(event != null) {

            event.actionType = clickAction;
            event.actionUri = clickActionUrl;
            event.actionLabel = clickActionLabel;
            intent.putExtra(Constants.EXTRA_EVENT, event);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            flags = flags | PendingIntent.FLAG_MUTABLE;

        if(isDismiss)
            return PendingIntent.getBroadcast(context, NotificationId.getID(), intent, flags);
        else return PendingIntent.getActivity(context, NotificationId.getID(), intent, flags);
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
        notificationBuilder.setChannelId(PushNotificationManager
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
            logger.error(e.getMessage());
        }
        return null;
    }

    @NonNull
    static private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }
}
