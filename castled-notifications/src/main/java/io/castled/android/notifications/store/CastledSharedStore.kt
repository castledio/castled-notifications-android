package io.castled.android.notifications.store

import android.content.Context
import android.content.SharedPreferences
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.store.consts.PrefStoreKeys

internal object CastledSharedStore {

    private const val PREFERENCE_FILE_KEY = "io.castled.android.notifications"

    private lateinit var sharedPreferences: SharedPreferences

    lateinit var configs: CastledConfigs

    private lateinit var apiKey: String
    private var userId: String? = null
    private var userToken: String? = null
    private val tokens = mutableMapOf<PushTokenType, String?>()

    fun init(context: Context, configs: CastledConfigs) {
        CastledSharedStore.configs = configs
        sharedPreferences =
            context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        if (sharedPreferences.getString(PrefStoreKeys.API_KEY, null) != configs.apiKey) {
            clearPreferences()
            setApiKey(configs.apiKey)
        } else {
            apiKey = sharedPreferences.getString(PrefStoreKeys.API_KEY, null)!!
        }
        userId = sharedPreferences.getString(PrefStoreKeys.USER_ID, null)
        userToken = sharedPreferences.getString(PrefStoreKeys.USER_TOKEN, null)
        tokens[PushTokenType.FCM] = sharedPreferences.getString(PrefStoreKeys.FCM_TOKEN, null)
        tokens[PushTokenType.MI_PUSH] = sharedPreferences.getString(PrefStoreKeys.MI_TOKEN, null)
    }

    fun setApiKey(apiKey: String) {
        CastledSharedStore.apiKey = apiKey
        sharedPreferences.edit().putString(PrefStoreKeys.API_KEY, apiKey).apply()
    }

    fun setUserId(userId: String?,userToken: String?) {
        CastledSharedStore.userId = userId
        CastledSharedStore.userToken = userToken
        sharedPreferences.edit().putString(PrefStoreKeys.USER_ID, userId).apply()
        sharedPreferences.edit().putString(PrefStoreKeys.USER_TOKEN, userToken).apply()

    }
    fun setToken(fcmToken: String?, tokenType: PushTokenType) {
        tokens[tokenType] = fcmToken
        when (tokenType) {
            PushTokenType.FCM -> sharedPreferences.edit()
                .putString(PrefStoreKeys.FCM_TOKEN, fcmToken).apply()
            PushTokenType.MI_PUSH -> sharedPreferences.edit()
                .putString(PrefStoreKeys.MI_TOKEN, fcmToken).apply()
        }
    }

    fun getApiKey() = apiKey

    fun getUserId() = userId

    fun getUserToken() = userToken

    fun getToken(tokenType: PushTokenType) = tokens[tokenType]

    private fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}