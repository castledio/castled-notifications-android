package io.castled.android.notifications.tracking.device

import android.app.Application
import android.content.Context
import io.castled.android.notifications.BuildConfig
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.CastledSharedStoreListener
import io.castled.android.notifications.tracking.events.service.TrackDeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal object DeviceTracker : CastledSharedStoreListener {

    private lateinit var trackDeviceRepository: TrackDeviceRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.TRACK_EVENT_REPOSITORY)
    private lateinit var externalScope: CoroutineScope
    private lateinit var deviceInfo: CastledDeviceInfo

    fun init(application: Application, externalScope: CoroutineScope) {
        DeviceTracker.externalScope = externalScope
        trackDeviceRepository = TrackDeviceRepository(application)
        deviceInfo = CastledDeviceInfo(application)
        CastledSharedStore.registerListener(this)
    }

    private fun updateDeviceInfo() {

        externalScope.launch(Dispatchers.Default) {
            try {
                val deviceInfoMap = mapOf(
                    "sdkVersion" to BuildConfig.SDK_VERSION,
                    "appVersion" to deviceInfo.getAppVersion(),
                    "model" to deviceInfo.getModel(),
                    "make" to deviceInfo.getMake(),
                    "osVersion" to deviceInfo.getOSVersion(),
                    "locale" to deviceInfo.getLocale(),
                    "deviceId" to deviceInfo.getDeviceId(),
                    "platform" to "MOBILE_ANDROID"
                )
                if (deviceInfoMap != CastledSharedStore.getDeviceInfo()) {
                    CastledSharedStore.setDeviceInfo(deviceInfoMap)
                    trackDeviceRepository.reportDeviceTracking(
                        TrackDevicetUtils.getDeviceTrackRequest(
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
