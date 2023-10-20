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

    private lateinit var appId: String
    private var userId: String? = null
    private var secureUserId: String? = null
    private val tokens = mutableMapOf<PushTokenType, String?>()

    var isAppInBackground = true

    fun init(context: Context, configs: CastledConfigs? = null) {
        sharedPreferences =
            context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        configs?.let {
            CastledSharedStore.configs = configs
            if (sharedPreferences.getString(PrefStoreKeys.APP_ID, null) != configs.appId) {
                clearPreferences()
                setAppId(configs.appId)
            }
        }

        appId = sharedPreferences.getString(PrefStoreKeys.APP_ID, null)!!
        userId = sharedPreferences.getString(PrefStoreKeys.USER_ID, null)
        secureUserId = sharedPreferences.getString(PrefStoreKeys.SECURE_USER_ID, null)
        tokens[PushTokenType.FCM] = sharedPreferences.getString(PrefStoreKeys.FCM_TOKEN, null)
        tokens[PushTokenType.MI_PUSH] = sharedPreferences.getString(PrefStoreKeys.MI_TOKEN, null)
    }

    private fun setAppId(appId: String) {
        CastledSharedStore.appId = appId
        sharedPreferences.edit().putString(PrefStoreKeys.APP_ID, appId).apply()
    }

    fun setUserId(userId: String?, userToken: String?) {
        CastledSharedStore.userId = userId
        secureUserId = userToken
        sharedPreferences.edit().putString(PrefStoreKeys.USER_ID, userId).apply()
        userToken?.let {
            sharedPreferences.edit().putString(PrefStoreKeys.SECURE_USER_ID, userToken).apply()
        }
    }

    fun setSecureUserId(secureUserId: String?) {
        CastledSharedStore.secureUserId = secureUserId
        sharedPreferences.edit().putString(PrefStoreKeys.SECURE_USER_ID, secureUserId).apply()

    }

    fun setPushToken(fcmToken: String?, tokenType: PushTokenType) {
        setToken(fcmToken, tokenType)
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

    fun getAppId() = appId

    fun getUserId() = userId

    fun getSecureUserId() = secureUserId

    fun getToken(tokenType: PushTokenType) = tokens[tokenType]

    private fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}