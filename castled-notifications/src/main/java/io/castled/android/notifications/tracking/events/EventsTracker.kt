package io.castled.android.notifications.tracking.events

import android.app.Application
import io.castled.android.notifications.CastledUserAttributes
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.tracking.events.service.TrackEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch

internal object EventsTracker {

    private lateinit var trackEventRepository: TrackEventRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.TRACK_EVENT_REPOSITORY)
    private var enabled = false
    private lateinit var externalScope: CoroutineScope

    fun init(application: Application, externalScope: CoroutineScope) {
        EventsTracker.externalScope = externalScope
        trackEventRepository = TrackEventRepository(application)
        enabled = true
    }

    fun logCustomEvent(event: String, properties: Map<String, Any?>?) =
        externalScope.launch(Default) {
            if (!enabled) {
                logger.debug("Ignoring custom app event, tracking disabled!")
                return@launch

            } else if (CastledSharedStore.getUserId().isNullOrBlank()) {
                logger.debug("Ignoring event tracking, UserId not set yet!")
                return@launch
            }
            trackEventRepository.reportCustomEvent(
                TrackEventUtils.getTrackEvent(
                    event,
                    properties ?: mapOf()
                )
            )
        }

    fun logUserTrackingEvent(castledUserAttributes: CastledUserAttributes) =
        externalScope.launch(Default) {
            if (!enabled) {
                logger.debug("Ignoring user track event, tracking disabled!")
                return@launch
            } else if (CastledSharedStore.getUserId().isNullOrBlank()) {
                logger.debug("Ignoring user track event, UserId not set yet!")
                return@launch
            }
            trackEventRepository.reportUserTrackingEvent(
                TrackEventUtils.getUserEvent(
                    castledUserAttributes.getAttributes()
                )
            )

        }

}
