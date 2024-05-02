package io.castled.android.notifications.tracking.device

import android.app.Application
import android.content.Context
import android.os.Build
import io.castled.android.notifications.BuildConfig
import io.castled.android.notifications.CastledNotifications
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.observer.CastledLifeCycleObserver
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener
import io.castled.android.notifications.tracking.device.service.DeviceInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.TimeZone


internal object DeviceInfo : CastledSharedStoreListener {

    private lateinit var deviceInfoRepository: DeviceInfoRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.TRACK_EVENT_REPOSITORY)
    private lateinit var externalScope: CoroutineScope
    private lateinit var deviceInfo: CastledDeviceDetails
    private lateinit var applicationContext: Application
    fun init(application: Application, externalScope: CoroutineScope) {
        DeviceInfo.externalScope = externalScope
        applicationContext = application
        deviceInfoRepository = DeviceInfoRepository(application)
        deviceInfo = CastledDeviceDetails(application)
        CastledSharedStore.registerListener(this)
    }

    internal fun updateDeviceInfo() {
        externalScope.launch(Dispatchers.Default) {
            try {
                val isPushGranted = isPushPermissionGranted()
                val deviceInfoMap = mapOf(
                    "sdkVersion" to BuildConfig.SDK_VERSION,
                    "appVersion" to deviceInfo.getAppVersion(),
                    "model" to deviceInfo.getModel(),
                    "pushPermission" to if (isPushGranted
                    ) "1" else "0",
                    "make" to deviceInfo.getMake(),
                    "osVersion" to deviceInfo.getOSVersion(),
                    "locale" to deviceInfo.getLocale(),
                    "deviceId" to deviceInfo.getDeviceId(),
                    "timeZone" to deviceInfo.getTimeZone(),
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
        listenForPushPermissionChanges()
    }

    override fun onStoreUserIdSet(context: Context) {
        updateDeviceInfo()
    }


    internal fun isPushPermissionGranted(): Boolean {
        return deviceInfo.checkNotificationPermissions(applicationContext)
    }

    private fun listenForPushPermissionChanges() {
        if (CastledNotifications.getCastledConfigs().enablePush &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            // For Android 13 and above
            CastledLifeCycleObserver.registerListener(DeviceInfoAppLifeCycleListener(externalScope))
        }
    }
}
