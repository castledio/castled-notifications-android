package io.castled.notifications.store

import android.content.Context
import android.content.SharedPreferences
import io.castled.notifications.CastledConfigs
import io.castled.notifications.store.consts.PrefStoreKeys

internal object CastledSharedStore {

    private const val PREFERENCE_FILE_KEY = "io.castled.notifications"

    private lateinit var sharedPreferences: SharedPreferences

    lateinit var configs: CastledConfigs

    private var apiKey: String? = null
    private var userId: String? = null
    private var fcmToken: String? = null

    fun init(context: Context, apiKey: String, configs : CastledConfigs) {
        this.configs = configs
        this.sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        this.apiKey = sharedPreferences.getString(PrefStoreKeys.API_KEY, null)
        if (this.apiKey != apiKey) {
            clearPreferences()
            setApiKey(apiKey)
        }
        this.userId = sharedPreferences.getString(PrefStoreKeys.USER_ID, null)
        this.fcmToken = sharedPreferences.getString(PrefStoreKeys.FCM_TOKEN, null)
    }

    fun setApiKey(apiKey : String) {
        this.apiKey = apiKey
        sharedPreferences.edit().putString(PrefStoreKeys.API_KEY, userId).apply()
    }

    fun setUserId(userId : String?) {
        this.userId = userId
        sharedPreferences.edit().putString(PrefStoreKeys.USER_ID, userId).apply()
    }

    fun setFcmToken(fcmToken : String?) {
        this.fcmToken = fcmToken
        sharedPreferences.edit().putString(PrefStoreKeys.FCM_TOKEN, userId).apply()
    }

    fun getApiKey() = apiKey

    fun getUserId() = userId

    fun getFcmToken() = fcmToken

    private fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}