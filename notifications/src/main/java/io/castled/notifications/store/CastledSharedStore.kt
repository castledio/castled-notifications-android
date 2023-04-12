package io.castled.notifications.store

import android.content.Context
import android.content.SharedPreferences
import io.castled.notifications.store.consts.PrefStoreKeys

internal object CastledSharedStore {

    private const val PREFERENCE_FILE_KEY = "io.castled.notifications"

    private lateinit var sharedPreferences: SharedPreferences

    var apiKey: String? = null
        set(value) {
            field = value
            sharedPreferences.edit().putString(PrefStoreKeys.API_KEY, value).apply()
        }

    var userId: String? = null
        set(value) {
            field = value
            sharedPreferences.edit().putString(PrefStoreKeys.USER_ID, value).apply()
        }

    var fcmToken: String? = null
        set(value) {
            field = value
            sharedPreferences.edit().putString(PrefStoreKeys.FCM_TOKEN, value).apply()
        }

    fun init(context: Context, apiKey: String) {
        sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        CastledSharedStore.apiKey = sharedPreferences.getString(PrefStoreKeys.API_KEY, null)
        if (CastledSharedStore.apiKey != apiKey) {
            clearPreferences()
            CastledSharedStore.apiKey = apiKey
        }
        userId = sharedPreferences.getString(PrefStoreKeys.USER_ID, null)
        fcmToken = sharedPreferences.getString(PrefStoreKeys.FCM_TOKEN, null)
    }

    private fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}