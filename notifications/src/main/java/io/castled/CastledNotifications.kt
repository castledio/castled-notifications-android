package io.castled

import android.app.Application
import android.content.Context
import io.castled.inAppTriggerEvents.ChannelConfig
import io.castled.inAppTriggerEvents.ChannelType
import io.castled.inAppTriggerEvents.InAppChannelConfig
import io.castled.inAppTriggerEvents.event.InAppNotification
import io.castled.inAppTriggerEvents.models.consts.AppEvents
import io.castled.notifications.logger.CastledLogger
import io.castled.notifications.logger.LogTags

object CastledNotifications {

    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.GENERIC)

    private lateinit var apiKey: String;

    @JvmStatic
    fun initialize(application: Application, apiKey: String, configs: List<ChannelConfig>) {
        if (this::apiKey.isInitialized) {
            logger.error("Sdk already initialized!")
            return
        }
        if (apiKey.isBlank()) {
            logger.error("Api-key is not set!")
            return
        }
        this.apiKey = apiKey
        InAppNotification.initialize(application, apiKey, getInAppConfig(configs))
        logger.info("Sdk initialized successfully")
    }

    private fun getInAppConfig(configs: List<ChannelConfig>): InAppChannelConfig {
        return configs.find { it.type == ChannelType.IN_APP } as? InAppChannelConfig
            ?: InAppChannelConfig(false, 0)
    }

    @JvmStatic
    fun setUserId(userId: String) = if (userId.isBlank()) logger.error("UserId is empty!")
    else if (!isInited()) logger.error("Sdk not yet initialized!")
    else {
        InAppNotification.userId = userId
        InAppNotification.checkAndStartJobToGetEvents()
    }

    @JvmStatic
    fun logAppPageViewEvent(context: Context, screenName: String) {
        if (InAppNotification.isInited()) {
            InAppNotification.logAppEvent(context, AppEvents.APP_PAGE_VIEWED, null)
        }
    }

    @JvmStatic
    fun logAppOpenedEvent(context: Context) {
        if (InAppNotification.isInited()) {
            InAppNotification.logAppEvent(context, AppEvents.APP_OPENED, null)
        }
    }

    @JvmStatic
    fun logCustomAppEvent(context: Context, eventName: String, eventParams: Map<String, Any>?) {
        if (InAppNotification.isInited()) {
            InAppNotification.logAppEvent(context, eventName, eventParams)
        }
    }

    private fun isInited(): Boolean = this::apiKey.isInitialized

}

//TODO: close gitHub-> implement watching for network state changes and retrying network requests #15
//TODO: close gitHub-> implement exponential backoff #14
//TODO: close gitHub-> store reporting events in db until they are sent successfully #12