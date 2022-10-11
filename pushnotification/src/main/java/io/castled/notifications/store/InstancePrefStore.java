package io.castled.notifications.store;

import android.content.Context;
import android.content.SharedPreferences;

public class InstancePrefStore {

    private static final String CASTLED_APP_NAME = "io.castled.notifications";

    private final String instanceId;
    private final SharedPreferences sharedPreferences;

    public InstancePrefStore(Context context, String instanceId) {
        this.instanceId = instanceId;
        this.sharedPreferences = context.getSharedPreferences(getPrefStoreId(instanceId),
                Context.MODE_PRIVATE);
    }

    private String getPrefStoreId(String instanceId) {
        return String.format("%s.%s", CASTLED_APP_NAME, instanceId);
    }

    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }

}
