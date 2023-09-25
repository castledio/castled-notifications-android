package io.castled.android.notifications.inapp.observer

import android.app.Activity

interface AppEventCallbacks {

    fun onAppMovedToForeground(activity: Activity)

    fun onActivityStarted(activity: Activity)

    fun onAppMovedToBackground(activity: Activity)
}