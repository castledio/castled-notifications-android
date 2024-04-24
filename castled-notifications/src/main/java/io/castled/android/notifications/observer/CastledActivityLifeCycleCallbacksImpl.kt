package io.castled.android.notifications.observer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.castled.android.notifications.store.CastledSharedStore

class CastledActivityLifeCycleCallbacksImpl(private val lifeCycleListeners: List<CastledAppLifeCycleListener>) :
    Application.ActivityLifecycleCallbacks {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (isCastledInternalActivity(activity)) {
            return
        }
        lifeCycleListeners.forEach { it.onActivityCreated(activity) }
    }

    override fun onActivityStarted(activity: Activity) {
        if (isCastledInternalActivity(activity)) {
            return
        }
        lifeCycleListeners.forEach { it.onActivityStarted(activity, isActivityChangingConfigurations) }
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            CastledSharedStore.isAppInBackground = false
            lifeCycleListeners.forEach { it.onAppMovedToForeground(activity) }
        }
    }

    override fun onActivityResumed(activity: Activity) {
        lifeCycleListeners.forEach { it.onActivityResumed(activity) }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        if (isCastledInternalActivity(activity)) {
            return
        }
        isActivityChangingConfigurations = activity.isChangingConfigurations
        lifeCycleListeners.forEach { it.onActivityStopped(activity, isActivityChangingConfigurations) }
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            CastledSharedStore.isAppInBackground = true
            lifeCycleListeners.forEach { it.onAppMovedToBackground(activity) }
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (isCastledInternalActivity(activity)) {
            return
        }
        lifeCycleListeners.forEach { it.onActivityDestroyed(activity) }
    }

    private fun isCastledInternalActivity(activity: Activity) =
        activity.componentName.shortClassName.contains("CastledNotificationReceiverAct")
}