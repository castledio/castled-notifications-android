package io.castled.android.notifications.inbox.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import io.castled.android.notifications.inbox.InboxEventUtils
import io.castled.android.notifications.inbox.model.CastledInboxItem
import io.castled.android.notifications.inbox.model.InboxResponseConverter.toInbox
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.store.CastledDbBuilder
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.Inbox
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import io.castled.android.notifications.workmanager.models.CastledInboxEventRequest
import retrofit2.Response
import java.io.IOException

internal class InboxRepository(context: Context) {

    internal val inboxDao = CastledDbBuilder.getDbInstance(context).inboxDao()
    private val logger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)
    private val inboxApi = CastledRetrofitClient.create(InboxApi::class.java)
    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)
    internal suspend fun refreshInbox() {
        val cachedInboxItems = inboxDao.dbGetInbox()
        val liveInboxResponse = fetchLiveInbox() ?: return
        val liveInboxItems = liveInboxResponse.map { it.toInbox() }
        val cachedInboxItemsMapById = cachedInboxItems.associateBy { it.messageId }
        val liveInInboxItemsMapById = liveInboxItems.associateBy { it.messageId }
        val expiredInboxItems =
            cachedInboxItems.filterNot { liveInInboxItemsMapById.containsKey(it.messageId) }
        val newInboxItems =
            liveInboxItems.filterNot { cachedInboxItemsMapById.containsKey(it.messageId) }
        insertInboxIntoDb(newInboxItems)
        deleteDbInbox(expiredInboxItems)
    }

    private suspend fun insertInboxIntoDb(inboxItems: List<Inbox>): LongArray {
        return inboxDao.dbInsertInbox(inboxItems)
    }

    private suspend fun deleteDbInbox(inboxItems: List<Inbox>): Int {
        return inboxDao.dbDeleteAllInboxItems(inboxItems)
    }

    internal fun deleteInboxItem(inbox: Inbox) {
        inboxDao.delete(inbox)
    }

    private suspend fun fetchLiveInbox(): List<CastledInboxItem>? {
        try {
            val response = inboxApi.fetchInboxItems(
                CastledSharedStore.getAppId(), CastledSharedStore.getUserId()
            )
            return if (response.isSuccessful) {
                response.body()
            } else {
                // Handle API errors (e.g., 4xx or 5xx status codes)
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                logger.error(errorMessage)
                null
            }
        } catch (e: IOException) {
            logger.error("Network error!", e)
        } catch (e: Exception) {
            logger.error("Unknown error!", e)
        }
        return null
    }

    internal suspend fun deleteInboxItem(
        inboxItem: CastledInboxItem, completion: (Boolean, String) -> Unit
    ) {
        val inboxObject = inboxDao.getInboxObjectByMessageId(inboxItem.messageId)

        try {
            val response = inboxApi.reportInboxEvent(
                CastledSharedStore.getAppId(), InboxEventUtils.getInboxEventRequest(
                    inboxItem, "", "DELETED"
                )
            )

            if (response.isSuccessful) {
                inboxObject?.let {
                    inboxDao.delete(inboxObject)
                }
                completion(true, "")
                return
            } else {
                inboxObject?.let {
                    resetInboxObjectStatusAfterFailure(inboxObject)
                }
                // Handle API errors (e.g., 4xx or 5xx status codes)
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                logger.error(errorMessage)
                completion(false, errorMessage)
                return
            }
        } catch (e: IOException) {
            logger.error("Network error!", e)
            inboxObject?.let {
                resetInboxObjectStatusAfterFailure(inboxObject)
            }
            completion(false, e.localizedMessage ?: "Network error while deleting inbox item!!")

        } catch (e: Exception) {
            logger.error("Unknown error!", e)
            inboxObject?.let {
                resetInboxObjectStatusAfterFailure(inboxObject)
            }
            completion(false, e.localizedMessage ?: "Unknown error while deleting inbox item!")
        }

    }

    fun resetInboxObjectStatusAfterFailure(inboxObject: Inbox) {
        inboxObject.isDeleted = false
        inboxDao.updateInboxItem(inboxObject)
    }

    suspend fun getCategoryTags(): List<String> {
        return inboxDao.getUniqueNonEmptyTags()
    }

    internal fun observeInboxLiveDataWithTag(tag: String): LiveData<List<Inbox>> {
        return inboxDao.getInboxItemsWith(tag)
    }

    suspend fun reportEvent(request: CastledInboxEventRequest) {
        networkWorkManager.apiCallWithRetry(request = request, apiCall = {
            return@apiCallWithRetry inboxApi.reportInboxEvent(
                CastledSharedStore.getAppId(), it as CastledInboxEventRequest
            )
        })
    }

    suspend fun reportEventNoRetry(request: CastledInboxEventRequest): Response<Void?> {
        return inboxApi.reportInboxEvent(CastledSharedStore.getAppId(), request)
    }

    internal fun changeTheStatusToRead(inboxItems: Set<Inbox>) {
        try {
            inboxItems.forEach {
                it.isRead = true
                inboxDao.updateInboxItem(it)
            }
        } catch (e: Exception) {
            // Handle any exceptions that may occur during database operations
            logger.error("Unknown error!", e)
        }

    }
}