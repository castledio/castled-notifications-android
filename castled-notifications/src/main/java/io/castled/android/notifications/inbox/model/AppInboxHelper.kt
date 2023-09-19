package io.castled.android.notifications.inbox.model

import android.app.Application
import io.castled.android.notifications.inbox.InboxEventUtils
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal object AppInboxHelper {

    private lateinit var inboxRepository: InboxRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)

    private lateinit var externalScope: CoroutineScope

    private var enabled = false

    fun init(application: Application, externalScope: CoroutineScope) {
        AppInboxHelper.externalScope = externalScope
        inboxRepository = InboxRepository(application)
        enabled = true
    }

    fun reportEventWith(inbox: AppInbox, btnLabel: String, eventType: String) {
        if (!enabled || CastledSharedStore.getUserId() == null) {
            logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            return
        }
        reportInboxEvent(
            InboxEventUtils.getInboxEventRequest(
                inbox, btnLabel, eventType
            )
        )

    }

    private suspend fun reportEvent(request: CastledInboxEventRequest) =
        inboxRepository?.reportEvent(request)

    private fun reportInboxEvent(request: CastledInboxEventRequest) =
        AppInboxHelper.externalScope.launch(
            Dispatchers.Default
        ) {
            inboxRepository.reportEvent(request)
        }

}
