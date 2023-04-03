package io.castled.inAppTriggerEvents.observer

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags

internal class AppActivityLifecycleObserver : Application.ActivityLifecycleCallbacks {

    private val logger = CastledLogger.getInstance(LogTags.ALC_OBS)

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        logger.debug("onActivityPreCreated: ${activity.componentName.shortClassName}")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        logger.debug("onActivityCreated: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        logger.debug("onActivityPostCreated: ${activity.componentName.shortClassName}")
    }

    override fun onActivityStarted(activity: Activity) {
        logger.debug("onActivityStarted: ${activity.componentName.shortClassName}")
    }

    override fun onActivityResumed(activity: Activity) {
        logger.debug("onActivityResumed: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPreResumed(activity: Activity) {
        logger.debug("onActivityPreResumed: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPostResumed(activity: Activity) {
        logger.debug("onActivityPostResumed: ${activity.componentName.shortClassName}")
    }

    override fun onActivityPaused(activity: Activity) {
        logger.debug("onActivityPaused: ${activity.componentName.shortClassName}")
    }

    override fun onActivityStopped(activity: Activity) {
        logger.debug("onActivityStopped: ${activity.componentName.shortClassName}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        logger.debug("onActivitySaveInstanceState: ${activity.componentName.shortClassName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        logger.debug("onActivityDestroyed: ${activity.componentName.shortClassName}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.unregisterActivityLifecycleCallbacks(this)
        }
    }


}