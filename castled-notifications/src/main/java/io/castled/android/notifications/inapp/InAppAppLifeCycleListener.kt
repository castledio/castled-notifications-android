package io.castled.android.notifications.inapp

import android.app.Activity
import io.castled.android.notifications.inapp.models.consts.AppEvents
import io.castled.android.notifications.observer.CastledAppLifeCycleListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InAppAppLifeCycleListener(private val castledScope: CoroutineScope) :
    CastledAppLifeCycleListener {
    override fun onActivityCreated(activity: Activity) {
        if (!isCastledInternalActivity(activity)) {
            InAppNotification.onOrientationChange(activity)
        }
    }

    override fun onAppMovedToForeground(activity: Activity) {
        castledScope.launch(Dispatchers.Default) {
            InAppNotification.refreshCampaigns()
            InAppNotification.logAppEvent(activity, AppEvents.APP_OPENED, null)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isCastledInternalActivity(activity)) {
            InAppNotification.logAppEvent(
                activity,
                AppEvents.APP_PAGE_VIEWED,
                mapOf("name" to activity.componentName.shortClassName.drop(1))
            )
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (!isCastledInternalActivity(activity)) {
            InAppNotification.dismissInAppDialogsIfAny()
        }
    }

    private fun isCastledInternalActivity(activity: Activity) =
        activity.componentName.shortClassName.contains("CastledNotificationReceiverAct")
}