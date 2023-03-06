package io.castled.inAppTriggerEvents.observer

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import io.castled.notifications.logger.CastledLogger

private const val TAG = "AppActivityLifecycleObs"
internal class AppActivityLifecycleObserver: Application.ActivityLifecycleCallbacks{

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        CastledLogger.getInstance().debug("$TAG: onActivityPreCreated: ${activity.componentName.shortClassName}")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        CastledLogger.getInstance().debug("$TAG: onActivityCreated: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        CastledLogger.getInstance().debug("$TAG: onActivityPostCreated: ${activity.componentName.shortClassName}")
    }

    override fun onActivityStarted(activity: Activity) {
        CastledLogger.getInstance().debug("$TAG: onActivityStarted: ${activity.componentName.shortClassName}")
    }

    override fun onActivityResumed(activity: Activity) {
        CastledLogger.getInstance().debug("$TAG: onActivityResumed: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPreResumed(activity: Activity) {
        CastledLogger.getInstance().debug("$TAG: onActivityPreResumed: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPostResumed(activity: Activity) {
        CastledLogger.getInstance().debug("$TAG: onActivityPostResumed: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPaused(activity: Activity) {
        CastledLogger.getInstance().debug("$TAG: onActivityPaused: ${activity.componentName.shortClassName}")
    }

    override fun onActivityStopped(activity: Activity) {
        CastledLogger.getInstance().debug("$TAG: onActivityStopped: ${activity.componentName.shortClassName}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        CastledLogger.getInstance().debug("$TAG: onActivitySaveInstanceState: ${activity.componentName.shortClassName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        CastledLogger.getInstance().debug("$TAG: onActivityDestroyed: ${activity.componentName.shortClassName}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.unregisterActivityLifecycleCallbacks(this)
        }
    }


}