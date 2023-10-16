package io.castled.android.notifications.inbox

import android.app.Application
import io.castled.android.notifications.inbox.model.CastledInboxItem
import io.castled.android.notifications.inbox.model.InboxResponseConverter.toInboxItem
import io.castled.android.notifications.inbox.viewmodel.InboxRepository
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.Inbox
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


internal object AppInbox {

    private lateinit var inboxRepository: InboxRepository
    private val logger: CastledLogger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)
    private lateinit var externalScope: CoroutineScope
    private var fetchJob: Job? = null
    private var enabled = false

    fun init(application: Application, externalScope: CoroutineScope) {
        AppInbox.externalScope = externalScope
        inboxRepository = InboxRepository(application)
        enabled = true
        startInboxJob()
    }

    internal fun startInboxJob() {
        if (!enabled) {
            logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            return
        }
        if (fetchJob == null || !fetchJob!!.isActive) {
            fetchJob = externalScope.launch(Dispatchers.Default) {
                do {
                    inboxRepository.refreshInbox()
                    delay(TimeUnit.SECONDS.toMillis(CastledSharedStore.configs.inBoxFetchIntervalSec))
                } while (true)
            }
        }
    }

    fun reportInboxIdsRead(ids: Set<Long>) {
        if (!enabled || CastledSharedStore.getUserId() == null) {
            logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            return
        }
        externalScope.launch(Dispatchers.Default) {
            val inboxObjects =
                inboxRepository.inboxDao.getInboxObjectsByMessageIds(ids.toList())
            if (inboxObjects.isNotEmpty())
                reportReadEventsWithObjects(inboxObjects.toSet())
        }
    }

    suspend fun reportReadEventsWithItems(inboxItems: List<CastledInboxItem>) {
        if (!enabled || CastledSharedStore.getUserId() == null) {
            logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            return
        }
        externalScope.launch(Dispatchers.Default) {
            val inboxObjects =
                inboxRepository.inboxDao.getInboxObjectsByMessageIds(inboxItems.map { it.messageId })
            if (!inboxObjects.isEmpty())
                reportReadEventsWithObjects(inboxObjects.toSet())
        }
    }

    fun reportReadEventsWithObjects(inboxObjects: Set<Inbox>) {
        if (!enabled || CastledSharedStore.getUserId() == null) {
            logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            return
        }
        externalScope.launch(Dispatchers.Default) {
            reportInboxEvent(
                InboxEventUtils.getReadInboxEventRequest(inboxObjects)
            )
            inboxRepository.changeTheStatusToRead(inboxObjects)
        }

    }

    fun reportEventWith(inbox: CastledInboxItem, btnLabel: String, eventType: String) {
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

    fun deleteInboxItem(
        inboxItem: CastledInboxItem, completion: (Boolean, String) -> Unit
    ) = externalScope.launch(
        Dispatchers.Default
    ) {
        inboxRepository.deleteInboxItem(inboxItem) { success, message ->
            completion(success, message)
        }
    }

    private fun reportInboxEvent(request: CastledInboxEventRequest) = externalScope.launch(
        Dispatchers.Default
    ) {
        inboxRepository.reportEvent(request)
    }

    suspend fun getInboxUnreadCount(): Int {
        return inboxRepository.inboxDao.getInboxUnreadCount()
    }

    suspend fun getInboxItems(): List<CastledInboxItem> {

        if (!enabled || CastledSharedStore.getUserId() == null) {
            logger.debug("Ignoring inbox event, Castled inbox disabled/ UserId not configured")
            return listOf()
        }
        val inboxListItems = mutableListOf<CastledInboxItem>()
        val inboxDbItems = inboxRepository.inboxDao.dbGetInbox()
        inboxDbItems.forEach { inboxListItems.add(it.toInboxItem()) }
        return inboxListItems
    }

}
