package io.castled.android.notifications

import android.app.Activity
import io.castled.android.notifications.inapp.InAppNotification
import io.castled.android.notifications.inapp.models.consts.AppEvents
import io.castled.android.notifications.inapp.observer.AppEventCallbacks
import io.castled.android.notifications.inbox.AppInbox
import io.castled.android.notifications.store.CastledSharedStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class CastledLifeCycleObserver {
    companion object {
        private var isInitialLaunch = true
        val appEventCallbacks = object : AppEventCallbacks {
            override fun onActivityCreated(activity: Activity) {
                if (CastledNotifications.getCastledConfigs().enableInApp) {
                    InAppNotification.inAppController.updateInAppForOrientationChanges(activity)
                }
            }

            override fun onAppMovedToForeground(activity: Activity) {
                CastledSharedStore.isAppInBackground = true
                performAppOpenedActions(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                if (CastledNotifications.getCastledConfigs().enableInApp) {
                    InAppNotification.logAppEvent(
                        activity,
                        AppEvents.APP_PAGE_VIEWED,
                        mapOf("name" to activity.componentName.shortClassName.drop(1))
                    )
                }

            }

            override fun onAppMovedToBackground(activity: Activity) {
                CastledSharedStore.isAppInBackground = false
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (CastledNotifications.getCastledConfigs().enableInApp) {
                    InAppNotification.inAppController.dismissDialogIfAny()
                }
            }
        }

        fun performAppOpenedActions(activity: Activity) =
            CastledNotifications.castledScope.launch(Dispatchers.Default) {
                if (CastledNotifications.getCastledConfigs().enableInApp) {
                    InAppNotification.inAppController.refreshLiveCampaigns()
                    InAppNotification.logAppEvent(activity, AppEvents.APP_OPENED, null)
                }
                if (CastledNotifications.getCastledConfigs().enableAppInbox) {
                    AppInbox.inboxRepository.refreshInbox()
                }
            }


    }
}