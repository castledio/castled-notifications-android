package io.castled.android.notifications.tracking.device

import android.app.Activity
import io.castled.android.notifications.observer.CastledAppLifeCycleListener
import io.castled.android.notifications.store.CastledSharedStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DeviceInfoAppLifeCycleListener(private val castledScope: CoroutineScope) :
    CastledAppLifeCycleListener {

    override fun onActivityResumed(activity: Activity) {
        castledScope.launch {
            CastledSharedStore.getUserId()?.let {
                val isPushGranted = DeviceInfoManager.isPushPermissionGranted()
                if (CastledSharedStore.getPushPermission() != isPushGranted) {
                    DeviceInfoManager.updateDeviceInfo()
                }
            }
        }
    }
}