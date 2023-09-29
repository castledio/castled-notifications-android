package io.castled.android.notifications.trackevents

import android.app.Application
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.CastledUserAttributes
import io.castled.android.notifications.trackevents.service.TrackEventRepository

internal object TrackEvents {

    private lateinit var trackEventRepository: TrackEventRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.TRACK_EVENT_REPOSITORY)
    private var enabled = false

    fun init(application: Application) {
        trackEventRepository = TrackEventRepository(application)
        enabled = true
    }

    suspend fun logCustomEvent(event: String, properties: Map<String, Any>?) {
        if (!enabled) {
            logger.debug("Ignoring custom app event, tracking disabled!")
            return
        }
        if (CastledSharedStore.getUserId().isNullOrBlank()) {
            logger.debug("Ignoring app event, UserId not set yet!")
            return
        }
        trackEventRepository.reportCustomEvent(TrackEventUtils.getTrackEvent(event, properties ?: mapOf()))
    }

    suspend fun logUserTrackingEvent(castledUserAttributes: CastledUserAttributes) {
        if (!enabled) {
            logger.debug("Ignoring user track event, tracking disabled!")
            return
        }
        if (CastledSharedStore.getUserId().isNullOrBlank()) {
            logger.debug("Ignoring user track event, UserId not set yet!")
            return
        }
        trackEventRepository.reportUserTrackingEvent(TrackEventUtils.getUserEvent(castledUserAttributes.getAttributes()))
    }

}
