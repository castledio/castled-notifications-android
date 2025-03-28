package io.castled.android.notifications.inapp

import android.app.Activity
import io.castled.android.notifications.inapp.models.consts.AppEvents
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.observer.CastledAppLifeCycleListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InAppAppLifeCycleListener(private val castledScope: CoroutineScope) :
    CastledAppLifeCycleListener {

    private val logger = CastledLogger.getInstance(LogTags.IN_APP)

    override fun onActivityStarted(activity: Activity, isOrientationChange: Boolean) {
        InAppNotification.setCurrentActivity(activity)
        val activityName = activity.javaClass.simpleName
        if (isOrientationChange) {
            InAppNotification.onOrientationChange(activity)
            logger.debug("Orientation changed for :$activityName")

        } else {
            InAppNotification.logAppEvent(
                activity,
                AppEvents.APP_PAGE_VIEWED,
                mapOf("name" to activityName)
            )
            logger.debug("Activity: $activityName started")
        }
    }

    override fun onAppMovedToForeground(activity: Activity) {
        castledScope.launch(Dispatchers.Default) {
            InAppNotification.refreshCampaigns()
            delay(300)
            //  adding a delay to ensure that the in-app display state value is set
            //  if the user sets discard/suspended in-app state at app launch
            InAppNotification.logAppEvent(activity, AppEvents.APP_OPENED, null)
        }
        logger.debug("App in foreground")
    }

    override fun onActivityStopped(activity: Activity, isOrientationChange: Boolean) {
        if (isOrientationChange) {
            InAppNotification.dismissInAppDialogsIfAny()
        }
        InAppNotification.clearCurrentActivity(activity)
        logger.debug("Activity: ${activity.javaClass.simpleName} stopped")
    }

}