package io.castled.inAppTriggerEvents.database

import android.content.Context
import android.content.SharedPreferences

private const val TAG = "PreferencesManager"
class PreferencesManager(val context: Context) {
    private val PREFS_FILE_NAME = "campaign_prefs"
    private val PREF_FIELD_USERID = "user_id"

    private lateinit var sharedPreferences: SharedPreferences

    var userId: String = ""
        get() {
            checkAndInitializePreferences()
        field = sharedPreferences.getString(PREF_FIELD_USERID, "").toString()
            return field
    }
    set(value) {
        checkAndInitializePreferences()
        val editor = sharedPreferences.edit()
        editor.putString(PREF_FIELD_USERID, value)
        editor.apply()
        field = value
    }

    private fun checkAndInitializePreferences(){
        if (!this::sharedPreferences.isInitialized)
            sharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

}