package io.castled.notifications.store

import android.content.Context
import android.content.SharedPreferences
import io.castled.notifications.CastledConfigs
import io.castled.notifications.push.models.PushTokenType
import io.castled.notifications.store.consts.PrefStoreKeys

internal object CastledSharedStore {

    private const val PREFERENCE_FILE_KEY = "io.castled.notifications"

    private lateinit var sharedPreferences: SharedPreferences

    lateinit var configs: CastledConfigs

    private lateinit var apiKey: String
    private var userId: String? = null
    private val tokens = mutableMapOf<PushTokenType, String?>()

    fun init(context: Context, apiKey: String, configs: CastledConfigs) {
        this.configs = configs
        this.sharedPreferences =
            context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        if (this.sharedPreferences.getString(PrefStoreKeys.API_KEY, null) != apiKey) {
            clearPreferences()
            setApiKey(apiKey)
        } else {
            this.apiKey = sharedPreferences.getString(PrefStoreKeys.API_KEY, null)!!
        }
        this.userId = sharedPreferences.getString(PrefStoreKeys.USER_ID, null)
        tokens[PushTokenType.FCM] = sharedPreferences.getString(PrefStoreKeys.FCM_TOKEN, null)
        tokens[PushTokenType.MI_PUSH] = sharedPreferences.getString(PrefStoreKeys.MI_TOKEN, null)
    }

    fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
        sharedPreferences.edit().putString(PrefStoreKeys.API_KEY, apiKey).apply()
    }

    fun setUserId(userId: String?) {
        this.userId = userId
        sharedPreferences.edit().putString(PrefStoreKeys.USER_ID, userId).apply()
    }

    fun setToken(fcmToken: String?, tokenType: PushTokenType) {
        this.tokens[tokenType] = fcmToken
        when (tokenType) {
            PushTokenType.FCM -> sharedPreferences.edit()
                .putString(PrefStoreKeys.FCM_TOKEN, fcmToken).apply()
            PushTokenType.MI_PUSH -> sharedPreferences.edit()
                .putString(PrefStoreKeys.MI_TOKEN, fcmToken).apply()
        }
    }

    fun getApiKey() = apiKey

    fun getUserId() = userId

    fun getToken(tokenType: PushTokenType) = tokens[tokenType]

    private fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}