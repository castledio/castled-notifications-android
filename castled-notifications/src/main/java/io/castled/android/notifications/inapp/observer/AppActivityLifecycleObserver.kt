package io.castled.android.notifications.inapp.observer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags

internal class AppActivityLifecycleObserver(private val appEventCallbacks: AppEventCallbacks) :
    Application.ActivityLifecycleCallbacks {

    var activityReferences = 0
    var isActivityChangingConfigurations = false

    private val logger = CastledLogger.getInstance(
        LogTags.ALC_OBS
    )

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {

        appEventCallbacks.onActivityCreated(p0)
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            appEventCallbacks.onAppMovedToForeground(activity)
        }
        appEventCallbacks.onActivityStarted(activity)
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            appEventCallbacks.onAppMovedToBackground(activity)
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {
        appEventCallbacks.onActivityDestroyed(p0)
    }
}