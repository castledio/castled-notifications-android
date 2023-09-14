package io.castled.android.notifications.workmanager.trackevents

import android.app.Application
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import io.castled.android.notifications.workmanager.trackevents.service.TrackEventRepository

internal object TrackEvents {

    private lateinit var trackEventRepository: TrackEventRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.TRACK_EVENT_REPOSITORY)
    private var enabled = false

    fun init(application: Application) {
        trackEventRepository = TrackEventRepository(application)
        enabled = true
    }

    suspend fun reportEventWith(event: String, properties: Map<String, Any>?) {
        if (!enabled || CastledSharedStore.getUserId() == null) {
            logger.debug("Ignoring app event, Castled tracking disabled/ UserId not configured")
            return
        }
        reportEvent(TrackEventUtils.getTrackEvent(event, properties ?: mapOf()))
    }

    private suspend fun reportEvent(request: CastledTrackEventRequest) =
        trackEventRepository.reportEvent(request)


}
