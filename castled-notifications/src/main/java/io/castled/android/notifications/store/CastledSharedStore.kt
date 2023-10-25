package io.castled.android.notifications.store

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.store.consts.PrefStoreKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal object CastledSharedStore {

    private const val PREFERENCE_FILE_KEY = "io.castled.android.notifications"
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var externalScope: CoroutineScope

    lateinit var configs: CastledConfigs

    private lateinit var appId: String
    private var userId: String? = null
    private var secureUserId: String? = null
    private val tokens = mutableMapOf<PushTokenType, String?>()
    private var recentDisplayedPushIds = mutableListOf<Int>()

    var isAppInBackground = true

    fun init(context: Context, configs: CastledConfigs, externalScope: CoroutineScope) {
        appId = configs.appId
        CastledSharedStore.configs = configs
        CastledSharedStore.externalScope = externalScope
        sharedPreferences =
            context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        if (sharedPreferences.getString(PrefStoreKeys.APP_ID, null) != configs.appId) {
            clearPreferences()
        } else {
            restoreSharedStore()
        }
    }

    private fun restoreSharedStore() =
        externalScope.launch {
            withContext(Dispatchers.IO) {
                synchronized(this) {
                    sharedPreferences.edit().putString(PrefStoreKeys.APP_ID, appId).apply()
                    userId = sharedPreferences.getString(PrefStoreKeys.USER_ID, null)
                    secureUserId =
                        sharedPreferences.getString(PrefStoreKeys.SECURE_USER_ID, null)
                    tokens[PushTokenType.FCM] =
                        sharedPreferences.getString(PrefStoreKeys.FCM_TOKEN, null)
                    tokens[PushTokenType.MI_PUSH] =
                        sharedPreferences.getString(PrefStoreKeys.MI_TOKEN, null)
                    val numbersStr =
                        sharedPreferences.getString(PrefStoreKeys.RECENT_DISPLAYED_PUSH_IDS, "")
                    if (!numbersStr.isNullOrEmpty()) {
                        val numbersArray = TextUtils.split(numbersStr, ",")
                        numbersArray.forEach {
                            it.toIntOrNull()?.let { num -> recentDisplayedPushIds.add(num) }
                        }
                    }
                }
            }
        }

    fun setUserId(userId: String?, userToken: String?) {
        synchronized(this) {
            CastledSharedStore.userId = userId
            secureUserId = userToken
            sharedPreferences.edit().putString(PrefStoreKeys.USER_ID, userId).apply()
            userToken?.let {
                sharedPreferences.edit().putString(PrefStoreKeys.SECURE_USER_ID, userToken).apply()
            }
        }
    }

    fun setToken(fcmToken: String?, tokenType: PushTokenType) {
        synchronized(this) {
            tokens[tokenType] = fcmToken
            when (tokenType) {
                PushTokenType.FCM -> sharedPreferences.edit()
                    .putString(PrefStoreKeys.FCM_TOKEN, fcmToken).apply()

                PushTokenType.MI_PUSH -> sharedPreferences.edit()
                    .putString(PrefStoreKeys.MI_TOKEN, fcmToken).apply()
            }
        }
    }

    fun setRecentDisplayedPushId(id: Int) {
        while (recentDisplayedPushIds.size >= 20) {
            recentDisplayedPushIds.removeFirst()
        }
        recentDisplayedPushIds.add(id)
        // Save the updated array back to SharedPreferences
        val numbersStr: String = TextUtils.join(",", recentDisplayedPushIds)
        sharedPreferences.edit()
            .putString(PrefStoreKeys.RECENT_DISPLAYED_PUSH_IDS, numbersStr)
            .apply()
    }

    fun getAppId() = appId

    fun getUserId() = userId

    fun getSecureUserId() = secureUserId

    fun getToken(tokenType: PushTokenType) = tokens[tokenType]

    fun getRecentDisplayedPushIds() = recentDisplayedPushIds

    private fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}