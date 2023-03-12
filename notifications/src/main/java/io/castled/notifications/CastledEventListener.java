package io.castled.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import io.castled.notifications.consts.ClickAction;
import io.castled.notifications.consts.Constants;
import io.castled.notifications.consts.NotificationEventType;
import io.castled.notifications.logger.CastledLogger;
import io.castled.notifications.service.models.NotificationEvent;
import io.castled.notifications.utils.Utils;

//TODO merge reportEvent implementation here with the inapp reportEvent
public class CastledEventListener extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        handleIntent(this, getIntent());
    }

    public void handleIntent(Context context, Intent intent) {

        try {

            String action = intent.getAction(); // see NotificationEventType class
            if (action == null || action.trim().isEmpty()) {
                CastledLogger.getInstance().error("Triggered event listener without action!");
                return;
            }

            CastledLogger.getInstance().debug("onReceive: action - " + action);

            NotificationEvent event = intent.hasExtra(Constants.EXTRA_EVENT) ?
                    (NotificationEvent) intent.getSerializableExtra(Constants.EXTRA_EVENT) : null;

            if(action.equals(NotificationEventType.CLICKED.name())) { // Not discarded or none

                String clickedAction = intent.hasExtra(Constants.EXTRA_ACTION) ?
                        intent.getStringExtra(Constants.EXTRA_ACTION) : null;

                CastledLogger.getInstance().debug("Click action - " + clickedAction);

                if(clickedAction != null) {

                    Intent clientIntent = null;

                    String clickUri = intent.hasExtra(Constants.EXTRA_URI) ?
                            intent.getStringExtra(Constants.EXTRA_URI) : null;

                    //TODO should action label be sent to backend. Tracked in #68
                    String actionLabel = intent.hasExtra(Constants.EXTRA_LABEL) ?
                            intent.getStringExtra(Constants.EXTRA_LABEL) : null;

                    String keyValues = intent.hasExtra(Constants.EXTRA_KEY_VAL_PARAMS) ?
                            intent.getStringExtra(Constants.EXTRA_KEY_VAL_PARAMS) : null;

                    HashMap<String, String> keyValuesMap = null;
                    if(keyValues != null)
                        keyValuesMap = new Gson().fromJson(keyValues, new TypeToken<HashMap<String, String>>(){}.getType());

                    if(clickedAction.equals(ClickAction.DEEP_LINKING.name())) { // see ClickAction class

                        clientIntent = new Intent();
                        clientIntent.setAction(Intent.ACTION_VIEW);
                        clientIntent.setData(Uri.parse(keyValuesMap != null ? Utils.addQueryParams(clickUri, keyValuesMap) : clickUri));
                    }
                    else if(clickedAction.equals(ClickAction.NAVIGATE_TO_SCREEN.name())) {

                        clientIntent = new Intent(context, Class.forName(clickUri)); // Class name should be fully qualified name
                        if(keyValuesMap != null)
                            clientIntent.putExtras(Utils.mapToBundle(keyValuesMap));
                    }
                    else if(clickedAction.equals(ClickAction.RICH_LANDING.name())) {

                        clientIntent = new Intent();
                        clientIntent.setAction(Intent.ACTION_VIEW);
                        clientIntent.setData(Uri.parse(keyValuesMap != null ? Utils.addQueryParams(clickUri, keyValuesMap) : clickUri));
//                        clientIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    }
                    else if(clickedAction.equals(ClickAction.DEFAULT.name())) { //Default click action

                        clientIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    }

                    if(clientIntent != null) {
                        clientIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(clientIntent);
                    }
                }
            }

            if(event != null) {
                event.setEventTime();
                reportEvent(event);
            }

            onBackPressed();
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void reportEvent(NotificationEvent event) {
        CastledNotifications.getInstance().reportNotificationEvent(event);
    }
}