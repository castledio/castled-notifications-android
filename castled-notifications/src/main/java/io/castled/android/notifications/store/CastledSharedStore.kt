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
import kotlinx.serialization.json.Json
import org.json.JSONObject

internal object CastledSharedStore {

    private const val PREFERENCE_FILE_KEY = "io.castled.android.notifications"
    private const val PUSH_MESSAGE_SLIDING_WINDOW_SIZE = 64
    private val logger = CastledLogger.getInstance(LogTags.STORE)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var externalScope: CoroutineScope
    private val storeMutex = Mutex()

    private val listeners = mutableListOf<CastledSharedStoreListener>()

    lateinit var configs: CastledConfigs

    private lateinit var appId: String
    private var userId: String? = null
    private var deviceId: String? = null
    private var deviceInfo: Map<String, String>? = null

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
        this.externalScope = externalScope
        sharedPreferences = getSharedPreference(context)
        // Restore from shared store
        userId = sharedPreferences.getString(PrefStoreKeys.USER_ID, null)
        userToken = sharedPreferences.getString(PrefStoreKeys.USER_TOKEN, null)
        externalScope.launch { initPreferenceStore(context) }
    }

    private suspend fun initPreferenceStore(context: Context) =
        storeMutex.withLock {
            withContext(Dispatchers.IO) {
                val sharedPref = getSharedPreference(context)
                // Restore from shared store
                deviceId = sharedPref.getString(PrefStoreKeys.DEVICE_ID, null)
                deviceInfo = fetchDeviceInfo()
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
                sharedPref.edit()
                    .putString(
                        PrefStoreKeys.CONFIGS,
                        Json.encodeToString(CastledConfigs.serializer(), configs)
                    ).apply()
            }
            logger.debug("Store initialization completed")
            listeners.forEach { it.onStoreInitialized(context) }
        }

    fun getCachedConfigs(
        context: Context
    ): CastledConfigs? {
        val sharedPref = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val storedConfigs = sharedPref.getString(PrefStoreKeys.CONFIGS, null) ?: return null
        return try {
            Json.decodeFromString(CastledConfigs.serializer(), storedConfigs)
        } catch (e: Exception) {
            logger.error("Deserializing config: $storedConfigs failed!")
            null
        }
    }

    suspend fun setUserId(context: Context, userId: String?, userToken: String?) {
        storeMutex.withLock {
            if (userId == CastledSharedStore.userId &&
                userToken == CastledSharedStore.userToken) {
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
            while (recentDisplayedPushIds.size >= PUSH_MESSAGE_SLIDING_WINDOW_SIZE) {
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

    private fun fetchDeviceInfo(): Map<String, String>? {
        val deviceInfoJson = sharedPreferences.getString(PrefStoreKeys.DEVICE_INFO, null)
        return if (deviceInfoJson != null) {
            val jsonObject = JSONObject(deviceInfoJson)
            val map = HashMap<String, String>()
            for (key in jsonObject.keys()) {
                map[key] = jsonObject.getString(key)
            }
            map
        } else {
            null
        }
    }

    fun setDeviceInfo(deviceInfoMap: Map<String, String>) {
        deviceInfo = deviceInfoMap
        val deviceInfoJson = JSONObject(deviceInfoMap).toString()
        sharedPreferences.edit().putString(PrefStoreKeys.DEVICE_INFO, deviceInfoJson).apply()
    }

    fun setDeviceId(deviceID: String) {
        deviceId = deviceID
        sharedPreferences.edit().putString(PrefStoreKeys.DEVICE_ID, deviceId).apply()
    }

    fun <T : Any> getValue(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> sharedPreferences.getString(key, defaultValue) as T
            is Int -> sharedPreferences.getInt(key, defaultValue) as T
            is Long -> sharedPreferences.getLong(key, defaultValue) as T
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported preference type")
        }
    }

    fun <T : Any> setValue(key: String, value: T) {
        val editor = sharedPreferences.edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is Boolean -> editor.putBoolean(key, value)
            else -> throw IllegalArgumentException("Unsupported preference type")
        }
        editor.apply()
    }

    fun getAppId() = appId

    fun getUserId() = userId

    fun getSecureUserId() = userToken

    fun getToken(tokenType: PushTokenType) = tokens[tokenType]

    fun getDeviceInfo() = deviceInfo

    fun getDeviceId() = deviceId

    fun clearUserId() {
        sharedPreferences.edit().remove(PrefStoreKeys.USER_ID).apply()
    }

    fun clearSavedItems() {
        sharedPreferences.edit()
            .remove(PrefStoreKeys.USER_ID)
            .remove(PrefStoreKeys.USER_TOKEN)
            .remove(PrefStoreKeys.RECENT_DISPLAYED_PUSH_IDS)
            .remove(PrefStoreKeys.SESSION_ID)
            .remove(PrefStoreKeys.SESSION_IS_FIRST)
            .remove(PrefStoreKeys.SESSION_START_TIME)
            .remove(PrefStoreKeys.SESSION_END_TIME)
            .remove(PrefStoreKeys.SESSION_DURATION)
            .apply()
        userId = null

    }
}