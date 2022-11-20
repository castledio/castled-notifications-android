package io.castled.notifications.store;

import android.content.Context;
import android.content.SharedPreferences;

import io.castled.notifications.exceptions.CastledRuntimeException;
import io.castled.notifications.store.consts.PrefStoreKeys;

public class CastledInstancePrefStore {

    private static CastledInstancePrefStore instancePrefStore;
    private static final String CASTLED_APP_NAME = "io.castled.notifications";

    private final String instanceId;
    private final SharedPreferences sharedPreferences;

    private CastledInstancePrefStore(Context context, String instanceId) {
        this.instanceId = instanceId;
        this.sharedPreferences = context.getSharedPreferences(getPrefStoreId(instanceId),
                Context.MODE_PRIVATE);
    }

    public static void init(Context context, String instanceId) {
        instancePrefStore = new CastledInstancePrefStore(context, instanceId);
    }

    public static CastledInstancePrefStore getInstance() {
        if (instancePrefStore == null) {
            throw new CastledRuntimeException("Shared preference store not inited!");
        }
        return instancePrefStore;
    }

    private String getPrefStoreId(String instanceId) {
        return String.format("%s.%s", CASTLED_APP_NAME, instanceId);
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getTokenIfAvailable() {
        String token = get(PrefStoreKeys.PREF_KEY_FCM_TOKEN_UNREGISTERED);
        if(token == null) {
            token = get(PrefStoreKeys.PREF_KEY_FCM_TOKEN);
        }
        return token;
    }

    public String getUserIdIfAvailable() {
        String userId = get(PrefStoreKeys.PREF_KEY_USER_ID_UNREGISTERED);
        if(userId == null) {
            userId = get(PrefStoreKeys.PREF_KEY_USER_ID);
        }
        return userId;
    }

    public String get(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void put(String key, String val) {
        sharedPreferences.edit().putString(key, val).apply();
    }

    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }
}
