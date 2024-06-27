package io.castled.android.notifications.tracking.device

import android.app.Application
import android.content.Context
import android.os.Build
import io.castled.android.notifications.BuildConfig
import io.castled.android.notifications.commons.CastledUUIDUtils
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.observer.CastledLifeCycleObserver
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener
import io.castled.android.notifications.tracking.device.service.DeviceInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal object DeviceInfoManager : CastledSharedStoreListener {

    private lateinit var deviceInfoRepository: DeviceInfoRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.TRACK_EVENT_REPOSITORY)
    private lateinit var externalScope: CoroutineScope
    private lateinit var deviceInfoDetails: CastledDeviceDetails
    private lateinit var applicationContext: Application

    fun init(application: Application, externalScope: CoroutineScope, enablePush: Boolean) {
        DeviceInfoManager.externalScope = externalScope
        applicationContext = application
        deviceInfoRepository = DeviceInfoRepository(application)
        deviceInfoDetails = CastledDeviceDetails(application)
        listenForPushPermissionChanges(enablePush)
        CastledSharedStore.registerListener(this)

    }

    internal fun updateDeviceInfo() {
        externalScope.launch(Dispatchers.Default) {
            try {
                val isPushGranted = isPushPermissionGranted()
                deviceInfoDetails.getDeviceId() ?: run {
                    CastledSharedStore.setDeviceId(CastledUUIDUtils.getIdBase64())
                }
                val deviceInfoMap = mapOf(
                    "sdkVersion" to BuildConfig.SDK_VERSION,
                    "appVersion" to deviceInfoDetails.getAppVersion(),
                    "model" to deviceInfoDetails.getModel(),
                    "pushPermission" to if (isPushGranted
                    ) "1" else "0",
                    "make" to deviceInfoDetails.getMake(),
                    "osVersion" to deviceInfoDetails.getOSVersion(),
                    "locale" to deviceInfoDetails.getLocale(),
                    "deviceId" to deviceInfoDetails.getDeviceId()!!,
                    "timeZone" to deviceInfoDetails.getTimeZone(),
                    "platform" to "MOBILE_ANDROID",
                )
                if (deviceInfoMap != CastledSharedStore.getDeviceInfo()) {
                    deviceInfoRepository.reportDeviceInfo(
                        DeviceInfotUtils.getDeviceInfoRequest(
                            deviceInfoMap
                        )
                    )
                    CastledSharedStore.setPushPermission(isPushGranted)
                    CastledSharedStore.setDeviceInfo(deviceInfoMap)
                }
            } catch (e: Exception) {
                logger.error("Couldn't load  the  device info!", e)
            }
        }
    }

    override fun onStoreInitialized(context: Context) {
        CastledSharedStore.getUserId()?.let {
            updateDeviceInfo()
        }
    }

    override fun onStoreUserIdSet(context: Context) {
        updateDeviceInfo()
    }

    internal fun isPushPermissionGranted(): Boolean {
        return deviceInfoDetails.checkNotificationPermissions(applicationContext)
    }

    private fun listenForPushPermissionChanges(pushEnabled: Boolean) {
        if (pushEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            // For Android 13 and above
            CastledLifeCycleObserver.registerListener(DeviceInfoAppLifeCycleListener(externalScope))
        }
    }
}
