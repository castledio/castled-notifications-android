package io.castled.android.notifications.observer

import android.app.Activity

interface CastledAppLifeCycleListener {
    fun onActivityCreated(activity: Activity) {}

    fun onAppMovedToForeground(activity: Activity) {}

    fun onActivityStarted(activity: Activity, isOrientationChange: Boolean) {}

    fun onActivityStopped(activity: Activity, isOrientationChange: Boolean) {}

    fun onAppMovedToBackground(activity: Activity) {}

    fun onActivityDestroyed(activity: Activity) {}
}