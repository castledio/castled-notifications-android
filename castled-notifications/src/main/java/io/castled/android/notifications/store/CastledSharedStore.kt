package io.castled.android.notifications.store

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import io.castled.android.notifications.CastledConfigs
import io.castled.android.notifications.exceptions.CastledRuntimeException
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.push.models.PushTokenType
import io.castled.android.notifications.store.consts.PrefStoreKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal object CastledSharedStore {

    private const val PREFERENCE_FILE_KEY = "io.castled.android.notifications"
    private val logger = CastledLogger.getInstance(LogTags.STORE)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var externalScope: CoroutineScope
    private val storeMutex = Mutex()

    private val listeners = mutableListOf<CastledSharedStoreListener>()

    lateinit var configs: CastledConfigs

    private lateinit var appId: String
    private var userId: String? = null
    private var userToken: String? = null
    private val tokens = mutableMapOf<PushTokenType, String?>()
    private var recentDisplayedPushIds = mutableListOf<Int>()

    var isAppInBackground = true

    fun init(
        context: Context,
        configs: CastledConfigs,
        externalScope: CoroutineScope
    ) {
        this.appId = configs.appId
        this.configs = configs
        listeners?.let { this.listeners.addAll(listeners) }
        this.externalScope = externalScope
        externalScope.launch { initPreferenceStore(context) }
    }

    private suspend fun initPreferenceStore(context: Context) =
        storeMutex.withLock {
            withContext(Dispatchers.IO) {
                val sharedPref = getSharedPreference(context)
                // Restore from shared store
                userId = sharedPref.getString(PrefStoreKeys.USER_ID, null)
                userToken = sharedPref.getString(PrefStoreKeys.USER_TOKEN, null)
                tokens[PushTokenType.FCM] =
                    sharedPref.getString(PrefStoreKeys.FCM_TOKEN, null)
                tokens[PushTokenType.MI_PUSH] =
                    sharedPref.getString(PrefStoreKeys.MI_TOKEN, null)
                val numbersStr =
                    sharedPref.getString(PrefStoreKeys.RECENT_DISPLAYED_PUSH_IDS, "")
                if (!numbersStr.isNullOrBlank()) {
                    val numbersArray = TextUtils.split(numbersStr, ",")
                    numbersArray.forEach {
                        it.toIntOrNull()?.let { num -> recentDisplayedPushIds.add(num) }
                    }
                }
            }
            logger.debug("Store initialization completed")
            listeners.forEach { it.onStoreInitialized(context) }
        }

    suspend fun setUserId(context: Context, userId: String?, userToken: String?) {
        storeMutex.withLock {
            if (userId == CastledSharedStore.userId) {
                logger.debug("Ignoring userId set. Already set")
                return
            }
            val sharedPref = getSharedPreference(context)
            this.userId = userId
            this.userToken = userToken
            sharedPref.edit().putString(PrefStoreKeys.USER_ID, userId).apply()
            userToken?.let {
                sharedPref.edit().putString(PrefStoreKeys.USER_TOKEN, userToken).apply()
            }
            if (!this.userId.isNullOrBlank()) {
                listeners.forEach { it.onStoreUserIdSet(context) }
            }
        }
    }

    suspend fun setToken(fcmToken: String?, tokenType: PushTokenType) {
        storeMutex.withLock {
            if (!::sharedPreferences.isInitialized) {
                logger.debug("ignoring token set. Store not initialized yet!")
                return
            }
            tokens[tokenType] = fcmToken
            when (tokenType) {
                PushTokenType.FCM -> sharedPreferences.edit()
                    .putString(PrefStoreKeys.FCM_TOKEN, fcmToken).apply()

                PushTokenType.MI_PUSH -> sharedPreferences.edit()
                    .putString(PrefStoreKeys.MI_TOKEN, fcmToken).apply()
            }
        }
    }

    suspend fun checkAndSetRecentDisplayedPushId(context: Context, id: Int): Boolean {
        storeMutex.withLock {
            if (id in recentDisplayedPushIds) {
                return true
            }
            while (recentDisplayedPushIds.size >= 20) {
                recentDisplayedPushIds.removeFirst()
            }
            recentDisplayedPushIds.add(id)
            // Save the updated array back to SharedPreferences
            val numbersStr: String = TextUtils.join(",", recentDisplayedPushIds)
            getSharedPreference(context).edit()
                .putString(PrefStoreKeys.RECENT_DISPLAYED_PUSH_IDS, numbersStr)
                .apply()
            return false
        }
    }

    fun registerListener(listener: CastledSharedStoreListener) {
        if (listeners.contains(listener)) {
            throw CastledRuntimeException("Listener already registered!")
        }
        listeners.add(listener)
    }

    private fun getSharedPreference(context: Context): SharedPreferences {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences =
                context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            if (sharedPreferences.getString(PrefStoreKeys.APP_ID, null) != appId) {
                sharedPreferences.edit()
                    .clear()
                    .putString(PrefStoreKeys.APP_ID, appId)
                    .apply()
                sharedPreferences.edit().putString(PrefStoreKeys.APP_ID, appId).apply()
            }
            logger.debug("Pref store initialized")
        }
        return sharedPreferences
    }

    fun getAppId() = appId

    fun getUserId() = userId

    fun getSecureUserId() = userToken

    fun getToken(tokenType: PushTokenType) = tokens[tokenType]

    fun clearUserId() {
        sharedPreferences.edit().remove(PrefStoreKeys.USER_ID).apply()
    }
}