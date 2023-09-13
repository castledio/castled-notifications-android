package io.castled.android.notifications.workmanager.trackevents

import android.app.Application
import android.content.Context
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.workmanager.models.CastledTrackEventRequest
import io.castled.android.notifications.workmanager.trackevents.service.TrackEventRepository
import kotlinx.coroutines.CoroutineScope

internal object TrackEvents {

    private lateinit var trackEventRepository: TrackEventRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.TRACK_EVENT_REPOSITORY)
    private var enabled = false

    fun init(application: Application, externalScope: CoroutineScope) {
        trackEventRepository = TrackEventRepository(application)
        enabled = true
    }

    suspend fun reportEventWith(context: Context, event: String, properties: Map<String, Any>?) {
        if (!enabled) {
            logger.debug("Ignoring app event, Castled tracking disabled")
            return
        }
        reportEvent(TrackEventUtils.getTrackEvent(event, properties ?: mapOf<String, Any>()))
    }

    private suspend fun reportEvent(request: CastledTrackEventRequest) =
        trackEventRepository.reportEvent(request)


}
