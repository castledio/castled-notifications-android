package io.castled.android.notifications.tracking.device

import android.app.Application
import android.content.Context
import io.castled.android.notifications.BuildConfig
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener
import io.castled.android.notifications.tracking.events.service.DeviceInfoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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


    private fun updateDeviceInfo() {
      externalScope.launch(Dispatchers.Default) {
            try {
                val deviceInfoMap = mapOf(
                    "sdkVersion" to BuildConfig.SDK_VERSION,
                    "bundleId" to deviceInfo.getPackageName(applicationContext),
                    "appName" to deviceInfo.getAppName(applicationContext),
                    "appVersion" to deviceInfo.getAppVersion(),
                    "model" to deviceInfo.getModel(),
                    "pushPermission" to if (deviceInfo.checkNotificationPermissions(
                            applicationContext
                        )
                    ) "1" else "0",
                    "make" to deviceInfo.getMake(),
                    "osVersion" to deviceInfo.getOSVersion(),
                    "locale" to deviceInfo.getLocale(),
                    "deviceId" to deviceInfo.getDeviceId(),
                    "platform" to "MOBILE_ANDROID"
                )
                if (deviceInfoMap != CastledSharedStore.getDeviceInfo()) {
                    CastledSharedStore.setDeviceInfo(deviceInfoMap)
                    deviceInfoRepository.reportDeviceInfo(
                        DeviceInfotUtils.getDeviceInfoRequest(
                            deviceInfoMap
                        )
                    )
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

}
