package io.castled.android.notifications.inbox.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import io.castled.android.notifications.network.CastledRetrofitClient
import io.castled.android.notifications.inbox.model.InAppResponseConverter.toInbox
import io.castled.android.notifications.inbox.model.InboxResponse
import io.castled.android.notifications.logger.CastledLogger
import io.castled.android.notifications.logger.LogTags
import io.castled.android.notifications.store.CastledDbBuilder
import io.castled.android.notifications.store.CastledSharedStore
import io.castled.android.notifications.store.models.AppInbox
import io.castled.android.notifications.workmanager.CastledNetworkWorkManager
import java.io.IOException

internal class InboxRepository(context: Context) {

    private val inboxDao = CastledDbBuilder.getDbInstance(context).inboxDao()
    private val logger = CastledLogger.getInstance(LogTags.INBOX_REPOSITORY)
    private val inAppApi = CastledRetrofitClient.create(InboxApi::class.java)
    private val networkWorkManager = CastledNetworkWorkManager.getInstance(context)
    internal val cachedInboxItems = mutableListOf<AppInbox>()
    internal suspend fun refreshInbox() {
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

    private suspend fun insertInboxIntoDb(inboxItems: List<AppInbox>): LongArray {
        return inboxDao.dbInsertInbox(inboxItems)
    }

    private suspend fun deleteDbInbox(inboxItems: List<AppInbox>): Int {
        return inboxDao.dbDeleteAllInboxItems(inboxItems)
    }

    private suspend fun fetchLiveInbox(): List<InboxResponse>? {
        try {
            val response = inAppApi.fetchInboxItems(
                CastledSharedStore.getApiKey(), CastledSharedStore.getUserId()
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

    internal fun observeMovieLiveData(): LiveData<List<AppInbox>> {
          return inboxDao.getInboxitems()
    }

//    suspend fun reportEvent(request: CastledInAppEventRequest) {
//        networkWorkManager.apiCallWithRetry(
//            request = request,
//            apiCall = {
//                return@apiCallWithRetry inAppApi.reportEvent(
//                    CastledSharedStore.getApiKey(),
//                    it as CastledInAppEventRequest
//                )
//            }
//        )
//    }
//
//    suspend fun reportEventNoRetry(request: CastledInAppEventRequest): Response<Void?> {
//        return inAppApi.reportEvent(CastledSharedStore.getApiKey(), request)
//    }

}